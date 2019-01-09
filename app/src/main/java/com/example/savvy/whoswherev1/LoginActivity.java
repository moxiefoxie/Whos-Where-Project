package com.example.savvy.whoswherev1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;


public class LoginActivity extends AppCompatActivity {

    DynamoDBMapper dynamoDBMapper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

    }
            public void newUserOnClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), CreateUser.class);
                startActivity(intent);
                // = (Button) findViewById(R.id.button_createUsername);
            }
            public void logInOnCLick(View v){
                EditText email = (EditText) findViewById(R.id.et_email);
                EditText password = (EditText) findViewById(R.id.et_password);

                if(email.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter an email!", Toast.LENGTH_LONG).show();
                } else if(password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter a password!", Toast.LENGTH_LONG).show();
                } else{
                    readUser(email.getText().toString());
                }
            }


    public void readUser(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        email);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                logInUser(userItem);
            }
        }).start();
    }

    public void logInUser(final User_DB userItem){

        EditText password = (EditText) findViewById(R.id.et_password);
        String pass = password.getText().toString();

        if(userItem == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "NOT A VALID EMAIL", Toast.LENGTH_LONG).show();
                }
            });
        }else if(pass.equals(userItem.getPassword())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(getApplicationContext(), Map.class);
                    intent.putExtra("User", userItem.getUserId());
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "Golden", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "INCORRECT PASSWORD", Toast.LENGTH_LONG).show();
                }
            });
        }
    }





}//end class