package com.vrnitsolution.healthapp.Admin.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Doctors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class DoctorAdapterNew extends RecyclerView.Adapter<DoctorAdapterNew.DoctorViewHolder> {
    Context context;
    ArrayList<Doctors> doctors;
    private OnDoctorProfileClickListner onDoctorProfileClickListner;
    private Geocoder geocoder;

    public DoctorAdapterNew(Context context, ArrayList<Doctors> doctors, OnDoctorProfileClickListner onDoctorProfileClickListner) {
        this.context = context;
        this.doctors = doctors;
        this.onDoctorProfileClickListner = onDoctorProfileClickListner;
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    @NonNull
    @Override
    public DoctorAdapterNew.DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.item_doctor_admin,parent,false);
        return new DoctorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorAdapterNew.DoctorViewHolder holder, int position) {
        Doctors doctors1=doctors.get(position);
        holder.doctorName.setText(doctors1.getDoctorName());
        holder.docotorOccupation.setText(doctors1.getOccupation());
        holder.accountstatus.setText(doctors1.getAccount_status());

        // Extract latitude and longitude from the coordinates map
        Map<String, String> coordinates = doctors1.getCoordinates();

        if (coordinates != null && coordinates.containsKey("latitude") && coordinates.containsKey("longitude")) {
            String latitude = coordinates.get("latitude");
            String longitude = coordinates.get("longitude");

            // Convert latitude and longitude to an address
            try {
                double lat = Double.parseDouble(latitude);
                double lng = Double.parseDouble(longitude);
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && addresses.size() > 0) {
                    String state = addresses.get(0).getAdminArea();
                    String city = addresses.get(0).getLocality();
                    holder.geoAddress.setText(city + ", " + state);
                } else {
                    holder.geoAddress.setText("Address not found");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Glide.with(context)
                .load(doctors1.getPhotoUrl())
                .into(holder.doctorImage);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDoctorProfileClickListner != null) {
                    onDoctorProfileClickListner.onDoctorProfileClick(position,doctors1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, docotorOccupation, geoAddress,accountstatus;
        CircleImageView doctorImage;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorname);
            docotorOccupation = itemView.findViewById(R.id.doctoroccuption);
            geoAddress = itemView.findViewById(R.id.geoAddress);
            doctorImage = itemView.findViewById(R.id.doctorImage);
            accountstatus=itemView.findViewById(R.id.accountstatus);
        }
    }



    public interface OnDoctorProfileClickListner{
        void onDoctorProfileClick(int position,Doctors Doctor);
    }
}

