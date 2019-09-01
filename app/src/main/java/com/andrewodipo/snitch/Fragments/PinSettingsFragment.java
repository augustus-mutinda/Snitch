package com.andrewodipo.snitch.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;

public class PinSettingsFragment extends Fragment {

    Button pinSet;

    TextInputEditText pinView, pinConfirmView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_settings, container, false);

        pinView = view.findViewById(R.id.pinEDT);
        pinConfirmView = view.findViewById(R.id.pinConfirmEDT);
        pinSet = view.findViewById(R.id.pinSet);
        pinSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPin();
            }
        });
        return view;
    }

    private void setPin() {
        if (pinView.getText().toString().equals(pinConfirmView.getText().toString())) {
            if (pinView.getText().toString().length() < 4) {
                Toast.makeText(getContext(), "Pin too short, use 4 characters", Toast.LENGTH_SHORT).show();
            } else if (pinView.getText().equals("1234")) {
                Toast.makeText(getContext(), "You cannot use 1234 as a pin", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
                editor.putString(Constants.PIN, pinView.getText().toString());
                editor.apply();
                Toast.makeText(getContext(), "Pin set", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.baseFragment, new BasePremiumFragment()).commit();
            }
        } else {
            Toast.makeText(getContext(), "Pins do not match!", Toast.LENGTH_SHORT).show();
        }
    }
}
