package com.google.medicalsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.medicalsupportapp.Fragments.ChatsFragment;
import com.google.medicalsupportapp.Fragments.UsersFragment;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    DatabaseReference reference2;
    Button logoutButton;
    TextView adminContact;

    BottomNavigationView bottomNavigationView;

    private boolean checkUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.putExtra("Activity", "Profile");
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        boolean temp = checkUser();
        if (temp) {

            logoutButton = findViewById(R.id.logoutButton);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.profile);

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.profile:
                            return true;
                        case R.id.home:
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("Activity", "Login");
                            startActivity(intent);
                            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            //finish();
                            return true;
                        /*case R.id.chat:
                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent2.putExtra("Activity", "Main");
                            startActivity(intent2);
                            //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            //finish();
                            return true;*/
                    }
                    return false;
                }
            });

            adminContact = findViewById(R.id.contactAdminButton);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    finish();
                }
            });

            adminContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MessageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("userid", "M8xHNPCUwXg3nff6RW5KF6iSETz1");
                    startActivity(intent);
                    finish();
                }
            });
        }

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);


        if (temp) {
            reference2 = FirebaseDatabase.getInstance().getReference("Chats");
            reference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    int unread = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isSeen()) {
                            unread++;
                        }
                    }

                    if (unread == 0) {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Správy");
                    } else {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Správy (" + unread + ")");
                    }

                    //viewPagerAdapter.addFragment(new UsersFragment(), "Doktori");
                    viewPager.setAdapter(viewPagerAdapter);

                    tabLayout.setupWithViewPager(viewPager);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hideSoftKeyboard(MainActivity.this);
        bottomNavigationView.setSelectedItemId(R.id.profile);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        String act = intent.getStringExtra("Activity");
        if (Objects.requireNonNull(act).equals("Main")) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
