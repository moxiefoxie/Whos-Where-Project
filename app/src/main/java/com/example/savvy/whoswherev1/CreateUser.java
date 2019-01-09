package com.example.savvy.whoswherev1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;


public class CreateUser extends AppCompatActivity {


    DynamoDBMapper dynamoDBMapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

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

        final Button newUser = (Button) findViewById(R.id.button_CreateUser);

        final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText firstName = (EditText) findViewById(R.id.firstName);
                EditText lastName = (EditText) findViewById(R.id.lastName);
                EditText email = (EditText) findViewById(R.id.email);
                EditText password = (EditText) findViewById(R.id.password);
                Switch autoCheck = (Switch) findViewById(R.id.autoCheck);

                if(firstName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need a first name!", Toast.LENGTH_LONG).show();
                } else if(lastName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need a last name!", Toast.LENGTH_LONG).show();
                } else if(email.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need an email!", Toast.LENGTH_LONG).show();
                } else if(password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need a password!", Toast.LENGTH_LONG).show();
                } else{
                    boolean check;
                    if(autoCheck.isChecked()){
                        check = true;
                    } else {
                        check = false;
                    }
                    createUserDB(email.getText().toString(), firstName.getText().toString(), lastName.getText().toString(), password.getText().toString(), check);
                    Toast.makeText(getApplicationContext(), "User successfully created", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            }
        });


    }

    public void createUserDB(String email, String firstName, String lastName, String password, boolean autoCheck) {

        final User_DB newUser = new User_DB();

        newUser.setUserId(email);

        newUser.setFirst_name(firstName);
        newUser.setLast_name(lastName);
        newUser.setPassword(password);
        newUser.setAuto_check_in(autoCheck);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newUser);
                // Item saved
            }
        }).start();
    }



}
