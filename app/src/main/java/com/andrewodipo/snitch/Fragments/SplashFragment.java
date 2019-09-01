package com.andrewodipo.snitch.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.andrewodipo.snitch.Activities.BaseActivity;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;

import java.util.Objects;

public class SplashFragment extends Fragment implements View.OnClickListener {

    Button signIn, skip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        signIn = view.findViewById(R.id.signInButton);
        signIn.setOnClickListener(this);
        skip = view.findViewById(R.id.skipButton);
        skip.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInButton:
                signIn();
                break;
            case R.id.skipButton:
                skipSignUp();
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void skipSignUp() {
        final AlertDialog dialog = new AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_splash_skip, null);

        final Button createAcc = dialogView.findViewById(R.id.skipDialogCreateAccountButton);
        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                dialog.dismiss();
            }
        });
        final Button skip = dialogView.findViewById(R.id.skipDialogSkipButton);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skip();
                dialog.dismiss();
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(dialogView);
        dialog.show();
    }

    private void skip(){
        startActivity(new Intent(getContext(), BaseActivity.class));
        Objects.requireNonNull(getActivity()).finish();
    }

    private void signIn() {
        assert getFragmentManager() != null;
        Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                .beginTransaction().replace(R.id.splashFragment, new SignInFragment()).commit();
    }
}
