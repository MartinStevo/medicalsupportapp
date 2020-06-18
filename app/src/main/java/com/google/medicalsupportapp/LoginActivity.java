package com.google.medicalsupportapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button loginButton, regButton;
    TextView reset;

    FirebaseAuth auth;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //start nav bar

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("Activity", "Login");
                        startActivity(intent);
                        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        //finish();
                        return true;
                    case R.id.profile:
                        /*startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();*/
                        return true;
                }
                return false;
            }
        });

        //end nav bar

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        regButton = findViewById(R.id.regButton);
        reset = findViewById(R.id.reset_password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_email = email.getText().toString();
                String text_password = password.getText().toString();

                if(TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)) {
                    Toast.makeText(LoginActivity.this, "Zadajte email a heslo pre prihlásenie", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(text_email, text_password).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Chyba prihlásenia", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hideSoftKeyboard(MainActivity.this);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        final MaterialEditText searchText1 = (MaterialEditText) findViewById(R.id.email);
        final MaterialEditText searchText2 = (MaterialEditText) findViewById(R.id.password);
        searchText1.clearFocus();
        searchText2.clearFocus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        String act = intent.getStringExtra("Activity");
        if (act != null && act.equals("Profile")) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
