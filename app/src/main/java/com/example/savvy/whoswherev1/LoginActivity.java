package com.example.savvy.whoswherev1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResponse;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;


public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "LOGIN";
    DynamoDBMapper dynamoDBMapper;
    CredentialsClient credentialsClient;


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

        this.credentialsClient = Credentials.getClient(getApplicationContext());
        attemptAutoLogin();

    }

    private void attemptAutoLogin() {
        credentialsClient.request(new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build())
                    .addOnCompleteListener(
                        new OnCompleteListener<CredentialRequestResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Logging in automatically using SmartLock...", Toast.LENGTH_LONG).show();
                                    Credential credential = task.getResult().getCredential();
                                    readUserFromEmailPass(credential.getName(), credential.getPassword());
                                    return;
                                }

                                Exception e = task.getException();
                                Log.d("LOGIN", "Credentials could not be retrieved for auto-login using smartlock " + e);
                                attemptAutoLoginBackupMethod();
                            }
                        }
                );

    }

    private void attemptAutoLoginBackupMethod() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        String userEmail = settings.getString("userEmail", null);
        String userPass = settings.getString("userPass", null);
        if (userEmail == null || userPass == null) {
            Log.d("LOGIN", "No data found for auto-login backup method");
            return;
        }

        EditText userTextBox = findViewById(R.id.et_email);
        userTextBox.setText(userEmail);
        EditText passwordTextBox = findViewById(R.id.et_password);
        passwordTextBox.setText("just a placeholder");
        Toast.makeText(getApplicationContext(), "Logging in automatically...", Toast.LENGTH_LONG).show();
        readUserFromEmailPass(userEmail, userPass);
    }

    public void newUserOnClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), CreateUser.class);
                startActivity(intent);
                // = (Button) findViewById(R.id.button_createUsername);
            }
            public void logInOnCLick(View v){
                EditText email = findViewById(R.id.et_email);
                EditText password = findViewById(R.id.et_password);

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
                logInUser(userItem, email);
            }
        }).start();
    }

    private void readUserFromEmailPass(final String userEmail, final String userPass) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        userEmail);

                if (userPass.equals(userItem.getPassword())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Intent intent = new Intent(getApplicationContext(), Map.class);
                            intent.putExtra("User", userItem.getUserId());
                            startActivity(intent);
                        }
                    });
                }
            }
        }).start();
    }


    public void logInUser(final User_DB userItem, final String email){

        EditText password = findViewById(R.id.et_password);
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
                    saveUserPass(userItem, email);
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

    private void saveUserPass(final User_DB userItem, final String email) {
        Credential credential = new Credential.Builder(email)
                .setName(email)
                .setPassword(userItem.getPassword())
                .build();
        credentialsClient.save(credential);

        // try to retrieve credentials just saved
        credentialsClient.request(new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build())
                .addOnCompleteListener(
                        new OnCompleteListener<CredentialRequestResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
                                if (task.isSuccessful()) {
                                    Credential credential = task.getResult().getCredential();
                                    return;
                                }

                                Log.d("LOGIN", "Failed to use smartlock. Using backup autologin method.");
                                saveUserPassBackupMethod(userItem, email);
                            }
                        }
                );
    }

    // A backup is necessary for devices which don't comply with smartlock
    private void saveUserPassBackupMethod(User_DB userItem, String email) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("userEmail", email);
        editor.putString("userPass", userItem.getPassword());
        editor.apply();
    }

}//end class