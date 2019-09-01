package com.andrewodipo.snitch.Fragments;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.andrewodipo.snitch.R;

import java.util.Objects;

public class BaseFreeFragment extends Fragment implements View.OnClickListener {

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_premium_base, container, false);

        startListening = view.findViewById(R.id.startButton);
        startListening.setOnClickListener(this);

        doBindService();
        return view;
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
            snitchService.stopAlarm();
            snitchService.stopSnitch();
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
}
