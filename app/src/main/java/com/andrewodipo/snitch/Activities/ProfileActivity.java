package com.andrewodipo.snitch.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewodipo.snitch.Adapters.SnitchAdapter;
import com.andrewodipo.snitch.Models.Profile;
import com.andrewodipo.snitch.Models.Snitch;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.andrewodipo.snitch.Utils.RecyclerTouchListener;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Field;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    CircleImageView profileImageView;
    TextView firstName, secondName, deviceName, androidVersion, deviceID, emptyView;
    RecyclerView recyclerView;
    FloatingActionButton refreshFAB;
    Uri profileUri;

    boolean profileExists = false;

    SnitchAdapter adapter;
    Profile defaultProfile = new Profile(), newProfile = new Profile();

    StorageReference storageRef;
    FirebaseStorage storage;
    FirebaseFirestore firestore;
    FirebaseUser user;
    FirebaseAuth auth;
    Query query;
    FirestoreRecyclerOptions<Snitch> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfProfileExists();
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        assert user != null;
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void checkIfProfileExists() {
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        firestore.collection(Constants.PROFILES).document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    defaultProfile = documentSnapshot.toObject(Profile.class);
                    assert defaultProfile != null;
                    initViews(defaultProfile);
                    loadRecyclerView();
                    profileExists = true;
                } else {
                    initViews(null);
                    profileExists = false;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "problem fetching your profile, ensure you have a connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void initViews(final Profile profile) {
        profileImageView = findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(this);
        firstName = findViewById(R.id.profileFirstName);
        firstName.setOnClickListener(this);
        secondName = findViewById(R.id.profileSecondName);
        secondName.setOnClickListener(this);
        deviceName = findViewById(R.id.profileDeviceName);
        deviceName.setText(Build.MODEL);
        androidVersion = findViewById(R.id.profileDeviceVersion);
        Field[] fields = Build.VERSION_CODES.class.getFields();
        androidVersion.setText(fields[Build.VERSION.SDK_INT + 1].getName());
        deviceID = findViewById(R.id.profileDeviceID);
        deviceID.setText(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        recyclerView = findViewById(R.id.profileRecyclerView);
        emptyView = findViewById(R.id.emptyTextView);
        refreshFAB = findViewById(R.id.profileFAB);
        refreshFAB.setOnClickListener(this);

        if (profile != null) {
            firstName.setText(profile.getFirstName());
            secondName.setText(profile.getSecondName());
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(getImage(Objects.requireNonNull(profile.getProfilePicture()))).into(profileImageView);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            loadRecyclerView();
        }
    }

    private void loadRecyclerView() {
        recyclerView = findViewById(R.id.profileRecyclerView);
        emptyView = findViewById(R.id.emptyTextView);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        query = firestore.collection(Constants.SNITCHS).whereEqualTo("userID", user.getUid())
                .orderBy("dateOfEvent", Query.Direction.DESCENDING);
        options = new FirestoreRecyclerOptions.Builder<Snitch>().setQuery(query, Snitch.class).build();
        adapter = new SnitchAdapter(this, firestore, options);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Uri gmmIntentUri = Uri.parse("google.streetview:cbll="+adapter.getItem(position).getDeviceLatitude()+","+adapter.getItem(position).getDeviceLongitude());

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                startActivity(mapIntent);
            }

            @Override
            public void onLongClick(View view, int position) {
                popUp(adapter.getItem(position));
            }
        }));

        adapter.startListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileImageView:
                setProfilePicture();
                break;
            case R.id.profileFirstName:
                setNames("Enter a first name");
                break;
            case R.id.profileSecondName:
                setNames("Enter a second name");
                break;
            case R.id.profileFAB:
                uploadChanges();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.IMAGE && resultCode == RESULT_OK && data != null) {
            Uri userUri = data.getData();
            this.profileUri = userUri;

            Glide.with(this).load(userUri).into(profileImageView);

            String fileName = "";
            assert userUri != null;
            if (Objects.equals(userUri.getScheme(), "file")) {
                fileName = userUri.getLastPathSegment();
            } else {
                try (Cursor cursor = this.getContentResolver().query(userUri, new String[]{
                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                }, null, null, null)) {

                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    }
                }
            }
            if (fileName != null && fileName.indexOf(".") > 0)
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            if (profileExists)
                defaultProfile.setProfilePicture(fileName);
            else
                newProfile.setProfilePicture(fileName);
        } else {
            Toast.makeText(this, "Cannot use that image, please pick another", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void setProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Constants.IMAGE);
    }

    public void setNames(final String header) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(header);
        @SuppressLint("InflateParams") View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_profile_names, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (profileExists) {
                    if (header.equals("Enter a first name")) {
                        defaultProfile.setFirstName(input.getText().toString());
                        firstName.setText(input.getText().toString());
                    } else {
                        defaultProfile.setSecondName(input.getText().toString());
                        firstName.setText(input.getText().toString());
                    }
                } else {
                    if (header.equals("Enter a first name")) {
                        newProfile.setFirstName(input.getText().toString());
                        firstName.setText(input.getText().toString());
                    } else {
                        newProfile.setSecondName(input.getText().toString());
                        firstName.setText(input.getText().toString());
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void uploadChanges() {
        if (!profileExists) {
            if (newProfile.getFirstName().equals("")) {
                setNames("Enter a first name");
                return;
            } else if (newProfile.getSecondName().equals("")) {
                setNames("Enter a second name");
                return;
            } else if (newProfile.getProfilePicture().equals("")) {
                setProfilePicture();
                return;
            } else {
                newProfile.setAlphaDeviceID(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                newProfile.setEmail(user.getEmail());
                uploadData(newProfile);
                uploadProfilePicture(newProfile);
            }
        } else {
            defaultProfile.setFirstName(firstName.getText().toString());
            defaultProfile.setSecondName(secondName.getText().toString());
            defaultProfile.setAlphaDeviceID(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            uploadData(defaultProfile);
            uploadProfilePicture(defaultProfile);
        }
    }

    private void uploadData(Profile profile) {
        firestore.collection(Constants.PROFILES).document(user.getUid()).set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                if (adapter == null)
                    loadRecyclerView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "...your connection might be off or slow.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfilePicture(Profile profile) {
        if (profileUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageRef.child("ProfilePictures/" + profile.getProfilePicture());
            ref.putFile(profileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed to upload picture, try later" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    private StorageReference getImage(String name) {
        return storageRef.child("ProfilePictures" + "/" + name);
    }

    private void popUp(Snitch snitch) {
        firestore.collection(Constants.SNITCHS).document(snitch.getSnitchID()).delete();
    }
}
