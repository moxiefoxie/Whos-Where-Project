package com.example.savvy.whoswherev1;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.gson.Gson;

public class UserInfo extends MainActivity {

    private static final String TAG = "MainActivity";
    TextView first;
    TextView last;
    TextView emailV;
    String userId;
    User_DB userItem;
    DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userId = getIntent().getStringExtra("User");

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userId = getIntent().getStringExtra("User");

        readUser(userId);
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                //this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        updateFields(userItem);


    }

    public void scheduleJob(View v) {
        ComponentName componentName = new ComponentName(this, LocationService.class);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("userId", userId);
        //Gson g = new Gson();
        //String json = g.toJson(dynamoDBMapper);
        //bundle.putString("dynamoDBMapper", json);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setExtras(bundle)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public void cancelJob(View v) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job Cancelled");
    }

    public void readUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

            }
        }).start();
    }

    public void updateFields(User_DB userItem){
        String firstN = User_DB.getFirst_name();
        String lastN = User_DB.getLast_name();
        String email = userId;


       first = findViewById(R.id.firstNameView);
       last = findViewById(R.id.lastNameView);
       emailV = findViewById(R.id.emailView);
        first.setText(firstN);
        last.setText(lastN);
        emailV.setText(email);
    }


    public void deleteOnClick(View v){

        setContentView(R.layout.delete_check);
    }

    public void backOnClick(View v){
        Intent intent = new Intent(UserInfo.this, Map.class);
        intent.putExtra("User", userId);
        startActivity(intent);
    }
    public void logOutOnClick(View v){
        Intent intent = new Intent(UserInfo.this, LoginActivity.class);
        startActivity(intent);
    }

}
