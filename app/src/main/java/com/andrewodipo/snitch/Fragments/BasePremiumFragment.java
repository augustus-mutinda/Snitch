package com.andrewodipo.snitch.Fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class BasePremiumFragment extends Fragment implements View.OnClickListener {

    AlertDialog countDownDialog;
    SnitchService snitchService;
    boolean snitchServiceIsBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            snitchService = ((SnitchService.MyBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            snitchService = null;
            snitchServiceIsBound = false;
        }
    };
    Button startListening;
    SharedPreferences preferences;
    FirebaseAuth mAuth;
    String pin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_premium_base, container, false);

        startListening = view.findViewById(R.id.startButton);
        startListening.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        preferences = getActivity().getSharedPreferences(Constants.APP, Context.MODE_PRIVATE);
        pin = preferences.getString(Constants.PIN, "1234");

        if (preferences.getString(Constants.PIN, "1234").equals("1234")) {
            Toast.makeText(getActivity(), "The default pin is 1234, please change it", Toast.LENGTH_SHORT).show();
            setPin();
        }

        doBindService();
        return view;
    }

    private void setPin() {
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.baseFragment, new PinSettingsFragment()).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                startStopSnitch();
                break;
        }
    }

    @Override
    public void onStop() {
        if (snitchService != null) {
            doUnbindService();
        }
        super.onStop();
    }

    @SuppressLint("SetTextI18n")
    private void startStopSnitch() {
        if (snitchService != null && !snitchService.isDetecting()) {
            countDownDialog = new android.support.v7.app.AlertDialog.Builder(Objects.requireNonNull(getActivity())).create();
            countDownDialog.setTitle("Snitch will be activated in");
            countDownDialog.setMessage("00:10");

            new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countDownDialog.setMessage("00:" + (millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    countDownDialog.hide();
                    startListening.setText("Stop Snitch");
                    snitchService.startSnitch();
                }
            }.start();
            countDownDialog.show();
            countDownDialog.setCancelable(false);
        } else if (snitchService != null && snitchService.isDetecting()) {
            stopSnitch();
            startListening.setText("Start Snitch");
        }
    }

    void doBindService() {
        Objects.requireNonNull(getActivity()).bindService(new Intent(getActivity(),
                SnitchService.class), mConnection, Context.BIND_AUTO_CREATE);
        snitchServiceIsBound = true;
    }

    void doUnbindService() {
        if (snitchServiceIsBound) {
            Objects.requireNonNull(getActivity()).unbindService(mConnection);
            snitchServiceIsBound = false;
        }
    }

    private void stopSnitch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("To cancel, choose");
        builder.setCancelable(false);

        builder.setNeutralButton("Use password", new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                final android.app.AlertDialog mDialog = new android.app.AlertDialog.Builder(getContext()).create();
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sign_in, null);

                final TextView textView = dialogView.findViewById(R.id.splashSkipInfo);
                textView.setText("Enter password for " + mAuth.getCurrentUser().getEmail());
                final TextInputEditText password = dialogView.findViewById(R.id.signInEmailEDT);
                final Button signInButton = dialogView.findViewById(R.id.skipDialogCreateAccountButton);
                signInButton.setText("Stop alarm");
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.requireNonNull(password.getText()).toString().length() > 5) {
                            mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), password.getText().toString())
                                    .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                stopAlarm();
                                                mDialog.dismiss();
                                                dialog.cancel();
                                            } else {
                                                Toast.makeText(getContext(), "Sign in failed, try again", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            password.setError("Please use more than 6 characters");
                        }
                    }
                });

                mDialog.setView(dialogView);
                mDialog.show();
            }
        });

        builder.setPositiveButton("Use pin", new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                final android.app.AlertDialog mDialog = new android.app.AlertDialog.Builder(getContext()).create();
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sign_in, null);

                final TextView textView = dialogView.findViewById(R.id.splashSkipInfo);
                textView.setText("Enter your pin ");
                final TextInputEditText password = dialogView.findViewById(R.id.signInEmailEDT);
                password.setHint("Your pin");
                final Button signInButton = dialogView.findViewById(R.id.skipDialogCreateAccountButton);
                signInButton.setText("Stop alarm");
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Objects.requireNonNull(password.getText()).toString().length() > 3) {
                            if (Objects.requireNonNull(password.getText()).toString().equals(pin)) {
                                stopAlarm();
                                mDialog.dismiss();
                                dialog.dismiss();
                            } else {
                                password.setError("Naah, wrong pin");
                            }
                        } else {
                            password.setError("Please use more than 4 characters");
                        }
                    }
                });

                mDialog.setView(dialogView);
                mDialog.show();
            }
        });

        builder.show();
    }

    private void stopAlarm() {
        snitchService.stopAlarm();
        snitchService.stopSnitch();
    }
}