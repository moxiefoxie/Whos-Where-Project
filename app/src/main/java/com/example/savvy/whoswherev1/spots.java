package com.example.savvy.whoswherev1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.List;
import java.util.UUID;

import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import java.util.ArrayList;

public class spots extends AppCompatActivity implements OnMapReadyCallback {

    View myView;
    @Nullable

    private static final String TAG = "CreateSpotActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION__REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private LocationRequest mLocationRequest;
    Button getCrntBtn;
    Button createBtn;
    String locName;
    double lat;
    double longi;
    double radi;
    EditText locNameView;
    DynamoDBMapper dynamoDBMapper;
    String loc;
    String locID;
    String type;

    String user;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public spots(String locName, double lat, double longi)
    {
        this.locName = locName;
        this.lat = lat;
        this.longi=longi;
        //this.user = user;

    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(spots.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            //Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FirstFragment.this, available, ERROR_DIALOG_REQUEST);
            // dialog.show();
        }else{
            //Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready");

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);
        startLocationUpdates();

        user = getIntent().getStringExtra("User");
        type = "private";

        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        getLocationPermission();

        final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView radius = findViewById(R.id.radius);
                radius.setText(seekBar.getProgress() + " meters");
                LatLng center = new LatLng(lat,longi);
                mMap.clear();
                chartSpot(center,seekBar.getProgress());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                radi = seekBar.getProgress();
            }
        });


    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting current device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(spots.this, "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        chartSpot(latLng, radi);

        //Displaying Spot
        //double radius = 100;
        //chartSpot(latLng, radius);
        //readUser(userId);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {FINE_LOCATION,
                COURSE_LOCATION};

        if (isServicesOK()==true) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                    initMap();
                } else {
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION__REQUEST_CODE);
                }

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION__REQUEST_CODE);
            }
        }
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);

        mapFragment.getMapAsync(spots.this);
    }

    private void chartSpot(final LatLng test, final double radius){
        //final LatLng test = new LatLng(latitude,longitude);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Circle spot = mMap.addCircle(new CircleOptions().center(test).radius(radius).strokeColor(Color.BLUE).fillColor(Color.CYAN));
                //spot.setClickable(true);
                //spot.setTag(id);
                /*mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                    @Override
                    public void onCircleClick(Circle circle) {
                        Intent intent = new Intent(getApplicationContext(), spotInfo.class);
                        intent.putExtra("User", userId);
                        intent.putExtra("Location", circle.getTag().toString());
                        startActivity(intent);
                    }
                });*/
                //withinSpot(spot, latLng);
            }
        });

    }


    public void currentOnClick(View v){

        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                onLocationChanged(location);
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                lat = location.getLatitude();
                                longi = location.getLongitude();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } else {
            requestPermissions();
        }
    }
    public void backOnClick(View v){
        Intent intent = new Intent(spots.this, Map.class);
        intent.putExtra("User", user);
        startActivity(intent);
    }

    public void changeTypeOnClick(View v){
        Switch types = (Switch) findViewById(R.id.locationType);
        if(types.isChecked()){
            type = "public";
        }else {
            type = "private";
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TEST";
            String description = "Testing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("TEST", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void createOnClick(View v){
        EditText name = findViewById(R.id.locNameView);
        spots location = new spots(name.getText().toString(), lat, longi);
        setContentView(R.layout.map_layout);


        createNotificationChannel();
        Intent intent = new Intent(this, NotifyMessage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "TEST")
                .setSmallIcon(R.drawable.icon_background)
                .setContentTitle("Who's Where?")
                .setContentText("You have created a new spot.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());





        final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        createLocationDB(location.lat, location.longi, name.getText().toString(), user, radi, type);

        Intent intent1 = new Intent(spots.this, Map.class);
        intent1.putExtra("User", user);
        startActivity(intent1);
    }
    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }
    public void onLocationChanged(Location location) {
        // New location has now been determined
        /*String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        lat = location.getLatitude();
        longi = location.getLongitude();
        final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
    }
    public double getLastLocationLat() {

        // Get last known recent location using new Google Play Services SDK (v11+)
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            double lat = location.getLatitude();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        return lat;
    }
    public void getLastLocationLong() {

        // Get last known recent location using new Google Play Services SDK (v11+)
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            double lat = location.getLongitude();

                            setLocation(latLng);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        //return lat;
    }

    public void setLocation(LatLng latLng){
        lat = latLng.latitude;
        longi = latLng.longitude;
    }


    /*public void onMapReady(GoogleMap googleMap) {

        if(checkPermissions()) {
            googleMap.setMyLocationEnabled(true);
        }
    }*/

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);
    }

    public spots() {

    }


    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void createLocationDB(double lat, double lng, String name, String email, double radius, String t) {

        UUID locationID = UUID.randomUUID();
        List<String> users = new ArrayList();
        users.add(email);
        //Toast.makeText(this, "Location ID: " + locationID.toString(), Toast.LENGTH_LONG).show();

        final location_db newLocation = new location_db();

        newLocation.setLocationId(locationID.toString());

        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setName(name);
        newLocation.setRadius(radius);
        if(t.equals("private")){
            newLocation.setUsers(users);
        }
        newLocation.setType(t);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newLocation);
                // Item saved
            }
        }).start();

        locID = locationID.toString();
        loc = locationID.toString();
        if(t.equals("private")){
            readUser(email);
        }
        //updateUser(u.id,u.firstName,u.lastName,u.spots,u.password,locationID.toString());
    }

    /*public void readUser(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        email);
                updateUser(userItem.getUserId(),userItem.getFirst_name(),userItem.getLast_name(),userItem.getLocations(),userItem.getPassword());

                // Item read
                Log.d("User Item", userItem.getFirst_name());
                //u = new user(userItem.getUserId(), userItem.getLocations(), userItem.getFirst_name(), userItem.getLast_name(),  userItem.getPassword());

            }
        }).start();

        //updateUser(u.id,u.firstName,u.lastName,u.spots,u.password,locationID);
    }*/

    public void readUser(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User_DB userItem = new User_DB();
                try {
                    userItem = dynamoDBMapper.load(

                            User_DB.class,
                            email);
                }
                catch (Exception e) {
                    System.out.println("Problem with db: " + e);
                }

                updateUser(userItem.getUserId(),userItem.getFirst_name(),userItem.getLast_name(),userItem.getLocations(),userItem.getPassword());
                // Item read
                Log.d("User Item", userItem.getFirst_name());
            }
        }).start();
    }

    public void updateUser(String email, String firstName, String lastName, List<String> locations, String password) {
        final User_DB userItem = new User_DB();

        if(locations == null){
            List<String> location = new ArrayList();
            location.add(loc);
            userItem.setLocations(location);
        }else{
            locations.add(loc);
            userItem.setLocations(locations);
        }


        userItem.setUserId(email);

        userItem.setFirst_name(firstName);
        userItem.setLast_name(lastName);
        userItem.setPassword(password);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated
            }
        }).start();
    }
}
