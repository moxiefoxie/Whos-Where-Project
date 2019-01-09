/*package com.example.savvy.whoswherev1;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.content.Intent;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.example.savvy.whoswherev1.User_DB;
import com.example.savvy.whoswherev1.autoCheckIn;
import com.example.savvy.whoswherev1.location_db;
import com.example.savvy.whoswherev1.spotInfo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Random;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by deepshikha on 24/11/16.
 */

/*import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.savvy.whoswherev1.User_DB;
import com.example.savvy.whoswherev1.autoCheckIn;
import com.example.savvy.whoswherev1.location_db;
import com.example.savvy.whoswherev1.spotInfo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service implements LocationListener{

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude,longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent myIntent;
    DynamoDBMapper dynamoDBMapper;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String userId;
    String locationId;


    public LocationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent myIntent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        userId = extras.getString("User");
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //userId = getIntent().getStringExtra("User");

        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(),5,notify_interval);
        myIntent = new Intent(str_receiver);
//        fn_getlocation();
        AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AUTO";
            String description = "Auto check in";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("AUTO", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        Intent intent = new Intent(this, autoCheckIn.class);
        Bundle extras = new Bundle();
        extras.putString("user",userId);
        extras.putDouble("lat", latitude);
        extras.putDouble("longi", longitude);
        intent.putExtras(extras);
        startService(intent);

        if(userId != null){
            readUser(userId);
        }
        //locations needs to get a users locations
        /*List locations = user.getLocations();
        final Object[] loc = locations.toArray();
        final Intent intent = new Intent(this, NotifyMessage.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, intent, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "AUTO")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Who's Where?")
                .setContentText("You have checked in to a location.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);



        for(int i=0; i<loc.length; i++){
            readLocation(loc[i].toString());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int j = 0;
                while(j < loc.length - 1)
                {
                    //need to have variables that get the radius, lat, and long for the inRadius class to work

                    if (readLocation(loc[j].getLocations()) == true)
                    {
                        //check in
                        checkInReadUser(userId);
                        createNotificationChannel();

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(0, builder.build());
                    }
                    else
                    {
                        j++;
                    }
                }

            }
        }).start();*/

/*import android.location.Location;
import android.support.annotation.NonNull;}import com.example.savvy.whoswherev1.User_DB;

import java.util.TimerTask;

public void readUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                if(userItem.getLocations() != null){
                    getSpots(userItem);
                }
            }
        }).start();
    }

    private void getSpots(User_DB user){
        List locations = user.getLocations();
        Object[] loc = locations.toArray();
        for(int i=0; i<loc.length; i++){
            readLocation(loc[i].toString());
        }
    }



    public void readLocation(final String id) {


        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());

                inRadius(locationItem.getRadius(), locationItem.getLatitude(), locationItem.getLongitude());
            }
        }).start();
        /*location_db locationItem = dynamoDBMapper.load(
                location_db.class,
                id);

        if(inRadius(locationItem.getRadius(), locationItem.getLatitude(),locationItem.getLongitude()) == true){
            return true;
        }
        else
            return false;*/

/*import android.location.Location;
import android.support.annotation.NonNull;}
    boolean inSpot;import com.example.savvy.whoswherev1.User_DB;

import java.util.TimerTask;

public  void inRadius(final double r, final double lat, final double lng) {
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

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

                                if (results[0] < r) {
                                    Toast.makeText(getApplicationContext(), "You are in the spot", Toast.LENGTH_LONG).show();
                                    checkInReadUser(userId);

                                } //else {
                                    //Toast.makeText(getApplicationContext(), "You are NOT in the spot", Toast.LENGTH_LONG).show();
                                    //Switch checkIn = (Switch) findViewById(R.id.checkIn);
                                    //checkIn.setChecked(!checkIn.isChecked());
                                    //inSpot = false;
                                //}
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
            Toast.makeText(getApplicationContext(), "Permissions required.", Toast.LENGTH_LONG).show();
        }
    }
    /*private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);
    }*/

/*import android.location.Location;

import com.example.savvy.whoswherev1.User_DB;

import java.util.TimerTask;

private void chartSpot(double latitude, double longitude, final double radius, final String id){
        final LatLng test = new LatLng(latitude,longitude);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Circle spot = mMap.addCircle(new CircleOptions().center(test).radius(radius).strokeColor(Color.BLUE).fillColor(Color.CYAN));
                spot.setClickable(true);
                spot.setTag(id);
                mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                    @Override
                    public void onCircleClick(Circle circle) {
                        Intent myIntent = new Intent(getApplicationContext(), spotInfo.class);
                        myIntent.putExtra("User", userId);
                        myIntent.putExtra("Location", circle.getTag().toString());
                        startActivity(myIntent);
                    }
                });
                //withinSpot(spot, latLng);
            }
        });

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void fn_getlocation(){
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable){

        }else {

            if (isNetworkEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location!=null){

                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }

            }


            if (isGPSEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }
            }


        }

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void fn_update(Location location){

        myIntent.putExtra("latutide",location.getLatitude()+"");
        myIntent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(myIntent);
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




}*/
