package com.google.medicalsupportapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND;
import static android.view.View.GONE;

public class DoctorProfile extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CALL = 1;
    private static Model model;
    List<Address> list_search = new ArrayList<>();
    List<Address> list_position = new ArrayList<>();
    String searchString;
    String docUID;
    Button msg_button;
    FirebaseUser firebaseUser;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double latitude;
    Double longitude;
    DatabaseReference reference;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        //location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(DoctorProfile.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(DoctorProfile.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
		}

        final TextView name = findViewById(R.id.doc_name_text);
        final TextView address = findViewById(R.id.doc_address_text);
        final TextView tel = findViewById(R.id.doc_tel_text);
        final TextView mobil = findViewById(R.id.doc_mobil_text);
        TextView error = findViewById(R.id.error_message);

        final Intent intent = getIntent();

            name.setText(intent.getStringExtra("NAME"));
            address.setText("Adresa: " + intent.getStringExtra("ADDRESS"));
            if (intent.getStringExtra("TEL") != null) {
                final String telTxt = intent.getStringExtra("TEL");
                tel.setText("Tel: " + telTxt);
                tel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + telTxt));
                            if (ContextCompat.checkSelfPermission(DoctorProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(DoctorProfile.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                            } else {
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            System.out.println(e.toString());
                            Toast.makeText(DoctorProfile.this, "Nie je možné vytočiť číslo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                tel.setVisibility(GONE);
            }
            if (intent.getStringExtra("MOBIL") != null) {
                final String mobilTxt = intent.getStringExtra("MOBIL");
                mobil.setText("Mobil: " + mobilTxt);
                mobil.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + mobilTxt));
                            if (ContextCompat.checkSelfPermission(DoctorProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(DoctorProfile.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                            } else {
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            System.out.println(e.toString());
                            Toast.makeText(DoctorProfile.this, "Nie je možné vytočiť číslo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                mobil.setVisibility(GONE);
            }

        firebaseUser = FirebaseAuth.getInstance().

                getCurrentUser();

        msg_button =

                findViewById(R.id.contactDoctorButton);
        msg_button.setText(

                getResources().

                        getText(R.string.type_a_message));
        if (firebaseUser == null) {
            if (intent.getStringExtra("UID") != null) {
                error.setText("Pre kontaktovanie lekára sa prihláste");
            }
            msg_button.setVisibility(GONE);
        } else {
            if (intent.getStringExtra("UID") != null) {
                startMessaging(intent.getStringExtra("UID"));
            } else {
                msg_button.setVisibility(GONE);
            }
        }

        searchString = intent.getStringExtra("ADDRESS");


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getLocation();
    }

    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location = task.getResult();

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    geoLocate();

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(DoctorProfile.this);
                } else {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getView().setVisibility(GONE);
                }
            }
        });
    }

    private void startMessaging(final String userid) {

        msg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("userid", userid);
                startActivity(intent);
                //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Address address_search = list_search.get(0);

        LatLng search = new LatLng(address_search.getLatitude(), address_search.getLongitude());
        LatLng pos = new LatLng(latitude, longitude);

        Marker marker1 = googleMap.addMarker(new MarkerOptions().position(search)
                .title("Špecialista"));
        Marker marker2 = googleMap.addMarker(new MarkerOptions().position(pos)
                .title("Vaša pozícia"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 10; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 800, 400, padding);

        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.moveCamera(cu);

    }

    private void geoLocate() {

        Geocoder geocoder = new Geocoder(DoctorProfile.this);

        try {
            list_search = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            //Log.e(MapsInitializer, "geoLocate: IOException: " + e.getMessage() );
        }

        if (list_search.size() > 0) {
            Address address_search = list_search.get(0);
            // Log.d(TAG, "geoLocate: found a location: " + address.toString());
        }
    }
}
