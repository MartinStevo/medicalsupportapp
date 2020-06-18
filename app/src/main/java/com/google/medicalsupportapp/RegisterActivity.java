package com.google.medicalsupportapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button registerButton;
    Button backButton;
    FirebaseAuth auth;
    DatabaseReference reference;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // VIDEO 2 MINUTA 16
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Register");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        // NED OF BOTTOM NAV BAR

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_username = username.getText().toString();
                String text_email = email.getText().toString();
                String text_password = password.getText().toString();

                if (TextUtils.isEmpty(text_username) || TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password)) {
                    Toast.makeText(RegisterActivity.this, "Zadajte meno, email aj heslo"  , Toast.LENGTH_SHORT).show();
                } else if (text_password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Vaše heslo je príliš krátke"  , Toast.LENGTH_SHORT).show();
                } else
                register(text_username, text_email, text_password);
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
    }


    private void register(final String username, String email, String password) {

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                assert firebaseUser != null;
                                String userid = firebaseUser.getUid();

                                reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("username", username);
                                hashMap.put("doc", "0");
                                //hashMap.put("imageURL", "default");

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Chyba prihlásenia", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String act = intent.getStringExtra("Activity");
        if (act != null && act.equals("Main")) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

}
