package com.andrewodipo.snitch.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.andrewodipo.snitch.Activities.BaseActivity;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInFragment extends Fragment {

    SharedPreferences preferences;

    TextInputEditText emailEDT;
    Button continueBTN;

    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        emailEDT = view.findViewById(R.id.signInEmailEDT);
        continueBTN = view.findViewById(R.id.contButton);

        mAuth = FirebaseAuth.getInstance();

        continueBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailIsValid(Objects.requireNonNull(emailEDT.getText()).toString())) {
                    checkIfUserExists(emailEDT.getText().toString());
                } else {
                    emailEDT.setError("mmmh... this seems wrong");
                }
            }
        });

        return view;
    }

    private static boolean emailIsValid(String email) {
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void checkIfUserExists(final String email) {
        mAuth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                boolean isRegistered = !Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getProviders()).isEmpty();
                if (!isRegistered)
                    signUserUp(email);
                else
                    signUserIn(email);
            }
        });
    }

    @SuppressLint("InflateParams")
    private void signUserIn(final String email) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sign_in, null);

        final TextInputEditText password = dialogView.findViewById(R.id.signInEmailEDT);
        final Button signInButton = dialogView.findViewById(R.id.skipDialogCreateAccountButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(password.getText()).toString().length() > 5) {
                    mAuth.signInWithEmailAndPassword(email, password.getText().toString())
                            .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getContext(), BaseActivity.class));
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Welcome back...", Toast.LENGTH_SHORT).show();
                                        setPremiumUser();
                                        Objects.requireNonNull(getActivity()).finish();
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

        dialog.setView(dialogView);
        dialog.show();

    }

    @SuppressLint("InflateParams")
    private void signUserUp(final String email) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_snitcher, null);

        final TextInputEditText password = dialogView.findViewById(R.id.signInEmailEDT);
        final TextInputEditText confirmPassword = dialogView.findViewById(R.id.repeatPasswordEDT);
        final Button signInButton = dialogView.findViewById(R.id.skipDialogCreateAccountButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(password.getText()).toString().equals(Objects.requireNonNull(confirmPassword.getText()).toString())) {
                    mAuth.createUserWithEmailAndPassword(email, confirmPassword.getText().toString())
                            .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getContext(), BaseActivity.class));
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Welcome " + email, Toast.LENGTH_SHORT).show();
                                        setPremiumUser();
                                        Objects.requireNonNull(getActivity()).finish();
                                    } else {
                                        Toast.makeText(getContext(), "Sign up failed, try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    confirmPassword.setError("Passwords don't match");
                }
            }
        });

        dialog.setView(dialogView);
        dialog.show();
    }

    private void setPremiumUser(){
        SharedPreferences.Editor editor = Objects.requireNonNull(getActivity()).getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.USERTYPE, Constants.PREMIUM);
        editor.apply();
    }
}
