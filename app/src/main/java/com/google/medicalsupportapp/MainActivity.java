package com.google.medicalsupportapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    WebView webView;
    LinearLayoutManager mLinearLayoutManager;
    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseRecyclerAdapter<Model, ViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Model> options;

    List<Model> modelList = new ArrayList<>();

    BottomNavigationView bottomNavigationView;

    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkInternetConnection();
        // DOC COUNTER

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        return true;
                        /*Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("Activity", "Main");
                        startActivity(intent);
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        //finish();
                        return true;*/
                    case R.id.profile:
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("Activity", "Main");
                        startActivity(intent);
                        //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        //finish();
                        return true;
                }
                return false;
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(this);
        //mLinearLayoutManager.setReverseLayout(true);
        //mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView = findViewById(R.id.recyclerView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        final ImageButton searchButton = (ImageButton) findViewById(R.id.imageButton);
        final MaterialEditText searchText = (MaterialEditText) findViewById(R.id.search_field);
        // keyboard done button
        searchText.setOnEditorActionListener(new DoneOnEditorActionListener());
        searchText.setCursorVisible(false);
        // capitalize the first char
        searchText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchText.setCursorVisible(true);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String field = searchText.getText().toString();
                Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
                Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
                searchText.setCursorVisible(false);
                hideSoftKeyboard(MainActivity.this);
                if (field.isEmpty()) {
                    if (spinner1.getSelectedItemPosition() == 0) {
                        Toast.makeText(MainActivity.this,
                                "Zadajte meno lekára alebo vyberte odbor", Toast.LENGTH_LONG).show();
                    } else if (spinner2.getSelectedItemPosition() == 0) {
                        showDoctors(spinner1.getSelectedItem().toString());
                        //Toast.makeText(MainActivity.this, modelList.size(), Toast.LENGTH_SHORT).show();
                    } else {
                        firebaseSearch(spinner1.getSelectedItem().toString(), "Region", spinner2.getSelectedItem().toString());
                    }
                } else {
                    spinner1.setSelection(0);
                    spinner2.setSelection(0);
                    String field_temp = field.toLowerCase();
                    String cap = field_temp.substring(0, 1).toUpperCase() + field_temp.substring(1);
                    firebaseSearch("Špecialisti", "Priezvisko", cap);
                }
                hideSoftKeyboard(MainActivity.this);
            }
        });

        Spinner specialistsList = (Spinner) findViewById(R.id.spinner1);
        specialistsList.setOnItemSelectedListener(this);

        Spinner regionList = (Spinner) findViewById(R.id.spinner2);
        regionList.setOnItemSelectedListener(this);


        // DROP DOWN LIST - SPECIALISTS


        List<String> specialists = Arrays.asList(getResources().getStringArray(R.array.specialists_array));

        final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(
                this, R.layout.spinner_item, specialists) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return true;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                return view;
            }
        };
        //spinnerArrayAdapter1.setDropDownViewResource(R.layout.spinner_item);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        specialistsList.setAdapter(spinnerArrayAdapter1);


        // DROP DOWN LIST - REGION


        List<String> regions = Arrays.asList(getResources().getStringArray(R.array.region));

        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                this, R.layout.spinner_item, regions) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return true;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                return view;
            }
        };
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        regionList.setAdapter(spinnerArrayAdapter2);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void checkInternetConnection() {
        webView = findViewById(R.id.web_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.network_alert_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            Button btnAgain = dialog.findViewById(R.id.try_again);
            btnAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
            dialog.show();
        }
    }

    private void showDoctors(String reference) {

        mDatabaseReference = mFirebaseDatabase.getReference(reference);

        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(mDatabaseReference, Model.class).build();
        modelList.clear();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Model model) {

                holder.setDetails(getApplicationContext(), model.getMeno(), model.getPriezvisko(), model.getAdresa());
                modelList.add(model);
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);

                ViewHolder viewHolder = new ViewHolder(itemView);
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        showDoctorInfo(position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        Toast.makeText(MainActivity.this, "Long Click", Toast.LENGTH_SHORT).show();

                    }
                });
                return viewHolder;
            }
        };

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void firebaseSearch(String reference, String orderChild, String searchText) {

        mDatabaseReference = mFirebaseDatabase.getReference(reference);

        String query = searchText;
        Query firebsaeSearchQuery = mDatabaseReference.orderByChild(orderChild).startAt(query).endAt(query + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(firebsaeSearchQuery, Model.class).build();

        modelList.clear();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Model model) {
                holder.setDetails(getApplicationContext(), model.getMeno(), model.getPriezvisko(), model.getAdresa());
                modelList.add(model);
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {

                final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);

                ViewHolder viewHolder = new ViewHolder(itemView);
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showDoctorInfo(position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "Long Click", Toast.LENGTH_SHORT).show();
                    }
                });
                return viewHolder;
            }
        };

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    protected void onStart() {
        super.onStart();
//        if (firebaseRecyclerAdapter != null) {
//            firebaseRecyclerAdapter.startListening();
//        }
    }

    private void showDoctorInfo(int position) {

        String name = "";
        if (modelList.get(position).Meno.equals("*")) {
            name = modelList.get(position).Priezvisko;
        } else {
            name = modelList.get(position).Meno + " " + modelList.get(position).Priezvisko;
        }

        Intent intent = new Intent(this, DoctorProfile.class);
        intent.putExtra("NAME", name);
        intent.putExtra("ADDRESS", modelList.get(position).Adresa);
        intent.putExtra("TEL", modelList.get(position).Tel);
        intent.putExtra("MOBIL", modelList.get(position).Mobil);
        intent.putExtra("UID", modelList.get(position).UID);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void hideSoftKeyboard(Activity activity) {

        RelativeLayout mainLayout;
        mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    mainLayout.getWindowToken(), 0);
        }
    }

    /*public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //hideSoftKeyboard(MainActivity.this);
        bottomNavigationView.setSelectedItemId(R.id.home);
        final MaterialEditText searchText = (MaterialEditText) findViewById(R.id.search_field);
        searchText.clearFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        final MaterialEditText searchText = (MaterialEditText) findViewById(R.id.search_field);
        String field = Objects.requireNonNull(searchText.getText()).toString();
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        int i1 = spinner1.getSelectedItemPosition();
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        int i2 = spinner2.getSelectedItemPosition();
        savedInstanceState.putInt("Spinner1", i1);
        savedInstanceState.putInt("Spinner2", i2);
        savedInstanceState.putString("EditText", field);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final MaterialEditText searchText = (MaterialEditText) findViewById(R.id.search_field);
        searchText.setText(savedInstanceState.getString("EditText"));
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setSelection(savedInstanceState.getInt("Spinner1"));
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner2.setSelection(savedInstanceState.getInt("Spinner2"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        String act = intent.getStringExtra("Activity");
        if (Objects.requireNonNull(act).equals("Login")) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        return super.onCreateOptionsMenu(menu);
//    }
}
