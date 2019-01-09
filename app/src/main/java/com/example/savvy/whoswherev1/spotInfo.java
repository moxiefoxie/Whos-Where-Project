package com.example.savvy.whoswherev1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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

import android.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.renderscript.Double2;

import java.io.IOException;
import java.util.Locale;

public class spotInfo extends AppCompatActivity implements OnMapReadyCallback {

    @Nullable

    private static final String TAG = "CreateSpotActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION__REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;

    TextView spotLat;
   TextView spotLong;
   TextView spotName;
    String userId;
   String locationId;
   Double lt;
   Double lg;
   Double radi;
   List<String> memberListText = new ArrayList<>();
   DynamoDBMapper dynamoDBMapper;
   String newUserId;

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(spotInfo.this);

        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");

        }else{
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_info_layout);

        userId = getIntent().getStringExtra("User");
        locationId = getIntent().getStringExtra("Location");

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

        readLocation(locationId);

        spotLat = findViewById(R.id.spotLat);
        spotLong = findViewById(R.id.spotLong);
        spotName = findViewById(R.id.spotName);


        double lat = getSpotLat();
        String latStrng = ("" + lat);
        String longStrng = (""+getSpotLong());
        spotName.setText(getSpotName());
        spotLat.setText(latStrng);
        spotLong.setText(longStrng);

        final Switch checkIn = (Switch) findViewById(R.id.checkIn);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkIn.isChecked()){
                    Toast.makeText(getApplicationContext(), "You are checked out of the spot", Toast.LENGTH_LONG).show();
                    checkOutReadUser(userId);
                }else {
                    checkInSpot(100, lt, lg);
                }
            }
        });


    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting current device location");

        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            //moveCamera(new LatLng(lt, lg), DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(spotInfo.this, "unable to get current location", Toast.LENGTH_SHORT).show();

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

        mapFragment.getMapAsync(spotInfo.this);
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MANUAL";
            String description = "Manual check in";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("MANUAL", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void checkInSpot(final double r, final double lat, final double lng){
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        final Intent intent = new Intent(this, NotifyMessage.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MANUAL")
                .setSmallIcon(R.drawable.icon_background)
                .setContentTitle("Who's Where?")
                .setContentText("You have been manually checked in to a spot.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                //onLocationChanged(location);
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                                double cX = lat;
                                double cY = lng;
                                double pX = latLng.latitude;
                                double pY = latLng.longitude;

                                float[] results = new float[1];

                                Location.distanceBetween(cX, cY, pX, pY, results);

                                if(results[0] < r) {
                                    Toast.makeText(getApplicationContext(), "You are checked in to the spot", Toast.LENGTH_LONG).show();
                                    checkInReadUser(userId);
                                    createNotificationChannel();

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


// notificationId is a unique int for each notification that you must define
                                    notificationManager.notify(0, builder.build());
                                } else {
                                    Toast.makeText(getApplicationContext(), "You are NOT in the spot", Toast.LENGTH_LONG).show();
                                    Switch checkIn = (Switch) findViewById(R.id.checkIn);
                                    checkIn.setChecked(!checkIn.isChecked());
                                }
                                //lat = location.getLatitude();
                                //longi = location.getLongitude();

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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);
    }

    public void readLocation(final String id) {
        memberListText = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());

                writeInfo(locationItem);
            }
        }).start();
    }

    public void writeInfo(final location_db locationItem){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if(locationItem.getType().equals("public")){
                    EditText addUserET = (EditText) findViewById(R.id.addUserET);
                    addUserET.setVisibility(View.GONE);
                    addUserET.setInputType(InputType.TYPE_NULL);
                    Button addUserBtn = (Button) findViewById(R.id.addUserBtn);
                    addUserBtn.setVisibility(View.GONE);
                }

                spotLat = findViewById(R.id.spotLat);
                spotLong = findViewById(R.id.spotLong);
                spotName = findViewById(R.id.spotName);

                lt = locationItem.getLatitude();
                lg = locationItem.getLongitude();
                radi = locationItem.getRadius();

                moveCamera(new LatLng(lt, lg), DEFAULT_ZOOM);

                String latStrng = ("" + locationItem.getLatitude());
                String longStrng = ("" + locationItem.getLongitude());
                spotName.setText(locationItem.getName());
                spotLat.setText(latStrng);
                spotLong.setText(longStrng);

                List<String> membList = locationItem.getUsers();
                Object[] membIds = membList.toArray();
                String[] mem;
                for(int i = 0; i < membIds.length; i++){
                    MemberListReadUser(membIds[i].toString());
                }

            }
        });
    }

    public void MemberListReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                setMemberList(userItem);
            }
        }).start();
    }

    public void setMemberList(User_DB userItem){
        String name = userItem.getFirst_name() + " " + userItem.getLast_name();
        if(userItem.getCurrent_location() == null){
            name += " is not here.";
        }else if(userItem.getCurrent_location().equals(locationId)){
            name += " is here!";
            if(userItem.getUserId().equals(userId)){
                final Switch checkIn = (Switch) findViewById(R.id.checkIn);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        checkIn.setChecked(true);

                    }
                });
            }
        }else {
            name += " is not here.";
        }
        memberListText.add(name);
        final ListView members = findViewById(R.id.mySpotsList);
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, memberListText);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                members.setAdapter(arrayAdapter);

            }
        });
    }

    public void checkInReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                checkInUpdateUser(userItem);

            }
        }).start();
    }

    public void checkInUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());
        userItem.setLocations(user.getLocations());
        userItem.setCurrent_location(locationId);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated

                readLocation(locationId);
            }
        }).start();
    }

    public void checkOutReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                checkOutUpdateUser(userItem);

            }
        }).start();
    }

    public void checkOutUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());
        userItem.setLocations(user.getLocations());

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated

                readLocation(locationId);
            }
        }).start();
    }

    public void addUserOnClick(View v) {

        EditText addUser = (EditText) findViewById(R.id.addUserET);
        if(addUser.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "You need to enter an email!", Toast.LENGTH_LONG).show();
        } else {
            addUserReadUser(addUser.getText().toString());
        }
    }

    public void addUserReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());

                if(userItem != null){
                    newUserId = userItem.getUserId();
                    addUserUpdateUser(userItem);
                }else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Not a valid email", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        }).start();
    }

    public void addUserUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        List<String> locations = new ArrayList();
        locations = user.getLocations();
        if(locations == null){
            List<String> location = new ArrayList();
            location.add(locationId);
            userItem.setLocations(location);
        }else{
            locations.add(locationId);
            userItem.setLocations(locations);
        }

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated
                addUserReadLocation(locationId);
            }
        }).start();
    }

    public void addUserReadLocation(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());
                addUserUpdateLocation(locationItem);

            }
        }).start();
    }

    public void addUserUpdateLocation(location_db location) {
        final location_db locationItem = new location_db();

        List<String> users = new ArrayList();
        users = location.getUsers();
        users.add(newUserId);

        locationItem.setLocationId(location.getLocationId());

        locationItem.setName(location.getName());
        locationItem.setLatitude(location.getLatitude());
        locationItem.setLongitude(location.getLongitude());
        locationItem.setRadius(location.getRadius());
        locationItem.setUsers(users);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(locationItem);

                // Item updated
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), "New user added", Toast.LENGTH_LONG).show();

                    }
                });
                readLocation(locationId);
            }
        }).start();
    }

    public String getSpotName()
    {
        String name = "Name";
        //this needs to get the name of the spot selected from the database
        return name;
    }

    public double getSpotLat()
    {
        double lat = 0;
        //this needs to get the latitude of the spot selected from the database
        return lat;
    }

    public double getSpotLong()
    {
        double lng = 0;
        //this needs to get longitude of the spot selected from the database
        return lng;
    }


}
