package com.andrewodipo.snitch.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andrewodipo.snitch.Fragments.BaseFreeFragment;
import com.andrewodipo.snitch.Fragments.BasePremiumFragment;
import com.andrewodipo.snitch.Fragments.PinSettingsFragment;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.Objects;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;

    SharedPreferences preferences;

    @Override
    protected void onStart() {
        super.onStart();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        preferences = getSharedPreferences(Constants.APP, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (isFreeUser()) {
            setFreeView();
            Toast.makeText(this, "Free version", Toast.LENGTH_SHORT).show();
        } else if (!isFreeUser()) {
            setPremiumView();
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            Toast.makeText(this, "Premium version", Toast.LENGTH_SHORT).show();
        }

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Snitch");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_sign_in:
                startActivity(new Intent(this, SplashActivity.class));
                finish();
                break;
            case R.id.nav_sign_out:
                auth.signOut();
                startActivity(new Intent(this, SplashActivity.class));
                finish();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_about:
                loadAbout();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                break;
        }
    }

    private boolean isFreeUser() {
        SharedPreferences preferences = getSharedPreferences(Constants.APP, Context.MODE_PRIVATE);
        return Objects.requireNonNull(preferences.getString(Constants.USERTYPE, Constants.FREE)).equals(Constants.FREE);
    }

    private void setPremiumView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.baseFragment, new BasePremiumFragment()).commit();
        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_base_drawer_active);
        checkAddDevice();
    }

    private void setFreeView() {
        getSupportFragmentManager().beginTransaction().replace(R.id.baseFragment, new BaseFreeFragment()).commit();
        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_base_drawer);
    }

    @SuppressLint("InflateParams")
    private void loadAbout() {
        Toast.makeText(this, "Snitcher Version 1", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HardwareIds")
    private void checkAddDevice() {
        final Field[] fields = Build.VERSION_CODES.class.getFields();
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
            checkAddDevice();
        } else {
            firestore.collection(Constants.DEVICES).document(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (task.isSuccessful()) {
                                assert documentSnapshot != null;
                                if (!documentSnapshot.exists()) {
                                    //do something
                                }
                            } else {
                                Toast.makeText(BaseActivity.this, "Couldn't add this device, ensure you have a connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BaseActivity.this, "Couldn't add this device, ensure you have a connection", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
