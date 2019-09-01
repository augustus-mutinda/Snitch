package com.andrewodipo.snitch.Fragments;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.andrewodipo.snitch.Models.Snitch;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Support.Detector;
import com.andrewodipo.snitch.Support.MyLocation;
import com.andrewodipo.snitch.Utils.Constants;
import com.andrewodipo.snitch.Utils.SensorListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class SnitchService extends Service implements SensorListener {

    SharedPreferences preferences;
    static MediaPlayer mp;
    boolean isCounting = false, canSnitch = true;
    Vibrator vibrator;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseUser user;
    Camera camera;


    CountDownTimer snitchTimer = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            canSnitch = false;
        }

        @Override
        public void onFinish() {
            canSnitch = true;
            Toast.makeText(SnitchService.this, "Can snitch", Toast.LENGTH_SHORT).show();
        }
    };

    private MyBinder binder = new MyBinder();

    class MyBinder extends Binder {
        SnitchService getService() {
            return SnitchService.this;
        }
    }

    BroadcastReceiver onChargerUnPlugged;
    BroadcastReceiver onChargerPlugged;
    Detector detector;
    Location mlocation;


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
        return Service.START_NOT_STICKY;
    }

    MyLocation myLocation;

    @Override
    public void onCreate() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        super.onCreate();
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                mlocation = new Location(LocationManager.GPS_PROVIDER);
                mlocation.setLatitude(22.932);
                mlocation.setLongitude(35.654);
                mlocation = location;
            }
        };
        myLocation = new MyLocation(SnitchService.this);
        myLocation.getLocation(this, locationResult);
    }

    @Override
    public void onSensorMovement(float movement) {
        if (isDetecting()) {
            raiseAlarm(Constants.MOTIONTRIGGERED);
        }
    }

    private void registerReceivers() {
        onChargerUnPlugged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Charger Unplugged!", Toast.LENGTH_SHORT).show();
                if (isDetecting()) {
                    raiseAlarm(Constants.CHARGERUNPLUGGED);
                }
            }
        };
        onChargerPlugged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Charger plugged!", Toast.LENGTH_SHORT).show();
                if (isDetecting()) {
                    raiseAlarm(Constants.CHARGERPLUGGED);
                }
            }
        };
        this.registerReceiver(onChargerPlugged, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        this.registerReceiver(onChargerUnPlugged, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
    }

    private void unRegisterReceivers() {
        if (onChargerPlugged != null) {
            this.unregisterReceiver(onChargerPlugged);
            onChargerPlugged = null;
        } else if (onChargerUnPlugged != null) {
            this.unregisterReceiver(onChargerUnPlugged);
            onChargerUnPlugged = null;
        }
    }

    public void startSnitch() {
        if (preferences.getBoolean(getResources().getString(R.string.charger_detection_key), true)) {
            registerReceivers();
        }
        if (preferences.getBoolean(getResources().getString(R.string.motion_detection_key), true)) {
            detector = new Detector(this, this);
            detector.start();
        }

        /*if (preferences.getBoolean(getResources().getString(R.string.key_upload_over_wifi), true)) {
            Toast.makeText(this, "Upload over wifi", Toast.LENGTH_SHORT).show();
        }
        if (preferences.getBoolean(getResources().getString(R.string.proximity_preference_key), true)) {
            Toast.makeText(this, "Proximity detection", Toast.LENGTH_SHORT).show();
        }*/
        Toast.makeText(this, "Snitch started!", Toast.LENGTH_SHORT).show();

    }

    public void stopSnitch() {
        if (detector != null) {
            detector.stop();
            detector = null;
        }
        unRegisterReceivers();
        Toast.makeText(this, "Snitch stopped!", Toast.LENGTH_SHORT).show();
    }

    public boolean isDetecting() {
        return detector != null || onChargerPlugged != null || onChargerUnPlugged != null;
    }

    public void raiseAlarm(final String trigger) {
        updateLocation(trigger);
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isCounting = true;
            }

            @Override
            public void onFinish() {
                if (isCounting)
                    updateLocation(trigger);
                isCounting = false;
            }
        }.start();

        if (isCounting && trigger.equals(Constants.CHARGERPLUGGED)) {
            stopAlarm();
        }

        if (preferences.getBoolean(getResources().getString(R.string.notifications_new_message), true)) {
            initRingtone();
        }
        if (preferences.getBoolean(getResources().getString(R.string.key_vibrate), true)) {
            initVibration();
        }

        if (preferences.getBoolean(getResources().getString(R.string.key_flash_light), true)) {
            initFlashlight();
        }
    }

    private void initRingtone() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (mp == null) {
            mp = MediaPlayer.create(this, uri);
        }
        if (!mp.isPlaying()) {
            mp.start();
        }
    }

    private void initVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        vibrator.vibrate(pattern, -1);
    }

    private void initFlashlight() {
        if (camera == null) {
            camera = Camera.open();
            Camera.Parameters p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        }
    }

    public void stopAlarm() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }

        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        isCounting = false;
        Toast.makeText(this, "Alarm stopped!", Toast.LENGTH_SHORT).show();
        stopSnitch();
    }

    @SuppressLint("HardwareIds")
    private void updateLocation(String trigger) {
        if (canSnitch) {
            snitchTimer.start();
        }
        if (auth.getCurrentUser() != null && canSnitch) {
            final Snitch snitch = new Snitch();
            snitch.setDeviceID(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            snitch.setUserID(user.getUid());
            snitch.setDeviceName(Build.MODEL);
            snitch.setDateOfEvent(Calendar.getInstance().getTime());
            snitch.setSnitchTrigger(trigger);
            snitch.setDeviceLatitude("22.932");
            snitch.setDeviceLongitude("35.654");
            uploadSnitch(snitch);
        }
    }

    public void uploadSnitch(Snitch snitch) {
        firestore.collection(Constants.SNITCHS).add(snitch).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                DocumentReference reference = task.getResult();
                assert reference != null;
                Toast.makeText(SnitchService.this, "Location updated!", Toast.LENGTH_SHORT).show();
                reference.update("snitchID", reference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SnitchService.this, "Snitch not reported!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
