package com.andrewodipo.snitch.Support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.andrewodipo.snitch.Models.Snitch;
import com.andrewodipo.snitch.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Snitcher {

    private Context context;
    private FirebaseFirestore firestore;
    private Snitch snitch;

    public Snitcher(Context context, FirebaseFirestore firestore, Snitch snitch){
        this.context = context;
        this.firestore = firestore;
        this.snitch = snitch;
    }

    public void uploadSnitch(){
        firestore.collection(Constants.SNITCHS).add(snitch).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                DocumentReference reference = task.getResult();
                assert reference != null;
                Toast.makeText(context, "Location updated!", Toast.LENGTH_SHORT).show();
                reference.update("SnitchID", reference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Snitch not reported!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
