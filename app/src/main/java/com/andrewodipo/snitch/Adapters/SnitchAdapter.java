package com.andrewodipo.snitch.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andrewodipo.snitch.Models.Snitch;
import com.andrewodipo.snitch.R;
import com.andrewodipo.snitch.Utils.Constants;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SnitchAdapter extends FirestoreRecyclerAdapter<Snitch, SnitchAdapter.SnitchViewHolder> {

    private Context context;
    private FirebaseFirestore firestore;

    public SnitchAdapter(Context context, FirebaseFirestore firestore, @NonNull FirestoreRecyclerOptions<Snitch> options) {
        super(options);
        this.context = context;
        this.firestore = firestore;
    }

    @SuppressLint({"HardwareIds", "SetTextI18n"})
    @Override
    protected void onBindViewHolder(@NonNull final SnitchViewHolder holder, int position, @NonNull Snitch snitch) {
        if (this.getItemCount() == (position + 1)) {
            holder.divider.setVisibility(View.GONE);
        }

        holder.deviceName.setText(snitch.getDeviceName());
        switch (snitch.getSnitchTrigger()) {
            case Constants.CHARGERUNPLUGGED:
                holder.snitchType.setText("Charger was unplugged");
                break;
            case Constants.MOTIONTRIGGERED:
                holder.snitchType.setText("Device was moved");
                break;
            case Constants.PROXIMITYTRIGGERED:
                holder.snitchType.setText("Device was removed from a bag, pocket...");
                break;
        }

        holder.co_ordinates.setText(snitch.getDeviceLatitude()
                + " , "
                + snitch.getDeviceLongitude());
        holder.date.setText(getDate(snitch.getDateOfEvent()));
    }

    @NonNull
    @Override
    public SnitchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SnitchViewHolder(LayoutInflater.from(context).inflate(R.layout.model_snitch_view, viewGroup, false));
    }

    class SnitchViewHolder extends RecyclerView.ViewHolder {

        TextView divider, deviceName, snitchType, date, co_ordinates;

        SnitchViewHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.modelSnitchDivider);
            deviceName = itemView.findViewById(R.id.modelSnitchDeviceName);
            snitchType = itemView.findViewById(R.id.modelSnitchType);
            date = itemView.findViewById(R.id.modelSnitchDate);
            co_ordinates = itemView.findViewById(R.id.modelSnitchCo_ordinates);
        }
    }


    @SuppressLint("SimpleDateFormat")
    private String getDate(Date date) {
        return new SimpleDateFormat("HH:mm").format(date);
    }
}
