package com.example.savvy.whoswherev1;

import android.Manifest;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationService extends JobService {
    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;
    String userId;
    String locationId;
    DynamoDBMapper dynamoDBMapper;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job Started");

        // AWSMobileClient enables AWS user credentials to access your table
        //AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        userId = params.getExtras().getString("userId");
        //String json = params.getExtras().getString("dynamoDBMapper");
        //Gson g = new Gson();
        //dynamoDBMapper = g.fromJson(json, DynamoDBMapper.class);
        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(final JobParameters params){
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*for (int i = 0; i < 10; i++) {
                    Log.d(TAG, "run: " + i);

                    if(jobCancelled){
                        return;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/

                if(jobCancelled){
                    return;
                }

                readUser(userId);

                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();
    }

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

                checkInSpot(locationItem.getLatitude(),locationItem.getLongitude(),locationItem.getRadius(), locationItem.getLocationId());
            }
        }).start();
    }

    public void checkInSpot(final double lat, final double lng, final double r, final String id){
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        //final Intent intent = new Intent(this, NotifyMessage.class);
        //final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /*final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MANUAL")
                .setSmallIcon(R.drawable.icon_background)
                .setContentTitle("Who's Where?")
                .setContentText("You have been manually checked in to a spot.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);*/
        //final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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
                                    locationId = id;
                                    checkInReadUser(userId);
                                    //createNotificationChannel();

                                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


                                    // notificationId is a unique int for each notification that you must define
                                    //notificationManager.notify(0, builder.build());
                                } else {
                                    Toast.makeText(getApplicationContext(), "You are NOT in the spot", Toast.LENGTH_LONG).show();
                                    //Switch checkIn = findViewById(R.id.checkIn);
                                    //checkIn.setChecked(!checkIn.isChecked());
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
            Toast.makeText(getApplicationContext(), "Need Permissions", Toast.LENGTH_LONG).show();
        }
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

        userItem.setFirst_name(User_DB.getFirst_name());
        userItem.setLast_name(User_DB.getLast_name());
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

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;

        return true;
    }
}
