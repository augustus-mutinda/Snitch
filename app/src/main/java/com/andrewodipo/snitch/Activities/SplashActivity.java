package com.andrewodipo.snitch.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.andrewodipo.snitch.Fragments.SplashFragment;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferences = getSharedPreferences(Constants.APP, MODE_PRIVATE);

        FirebaseApp.initializeApp(this);
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            loadSplash();
            setPreferences(Constants.FREE);
        } else {
            startActivity(new Intent(this, BaseActivity.class));
            setPreferences(Constants.PREMIUM);
            finish();
        }
    }

    private void loadSplash() {
        assert getFragmentManager() != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.splashFragment, new SplashFragment()).commit();
    }

    private void setPreferences(String preferences){
        SharedPreferences.Editor editor = getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.USERTYPE, preferences);
        editor.apply();
    }
}
