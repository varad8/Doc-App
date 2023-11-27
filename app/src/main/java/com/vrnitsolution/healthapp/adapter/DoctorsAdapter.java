package com.vrnitsolution.healthapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vrnitsolution.healthapp.DoctorDeatailsPage;
import com.vrnitsolution.healthapp.R;
import com.vrnitsolution.healthapp.model.Doctors;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.DoctorViewHolder> {
    private Context context;
    private List<Doctors> doctorsList;
    private Geocoder geocoder;

    public DoctorsAdapter() {
    }

    public DoctorsAdapter(Context context, List<Doctors> doctorsList) {
        this.context = context;
        this.doctorsList = doctorsList;
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctors doctor = doctorsList.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctorsList.size();
    }

    class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, docotorOccupation, geoAddress,distanceinKm;
        CircleImageView doctorImage;

        DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorname);
            docotorOccupation = itemView.findViewById(R.id.doctoroccuption);
            geoAddress = itemView.findViewById(R.id.geoAddress);
            doctorImage = itemView.findViewById(R.id.doctorImage);
            distanceinKm=itemView.findViewById(R.id.distanceinKm);
        }

        void bind(Doctors doctor) {
            String image = doctor.getPhotoUrl();
            doctorName.setText(doctor.getDoctorName());
            docotorOccupation.setText(doctor.getOccupation());
            distanceinKm.setText(doctor.getDistance()+""+" km");

            // Extract latitude and longitude from the coordinates map
            Map<String, String> coordinates = doctor.getCoordinates();

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
                        geoAddress.setText(city + ", " + state);
                    } else {
                        geoAddress.setText("Address not found");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Glide.with(context)
                    .load(image)
                    .into(doctorImage);

            // Handle item click events if needed
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle item click event, if necessary
                    Intent intent=new Intent(context, DoctorDeatailsPage.class);
                    intent.putExtra("dcname",doctor.getDoctorName());
                    intent.putExtra("dcId",doctor.getDoctorId());
                    intent.putExtra("dcemail",doctor.getEmail());
                    intent.putExtra("dcPhoto",doctor.getPhotoUrl());

                    // Pass availableServices map to the next activity
                    intent.putStringArrayListExtra("availableServices", doctor.getAvailableservices());
                    intent.putExtra("dcoccupation",doctor.getOccupation());
                    intent.putExtra("coordinates",new HashMap<>(doctor.getCoordinates()));
                    intent.putExtra("serviceHours",new HashMap<>(doctor.getServicehours()));
                    intent.putExtra("dcSpecialistIn",doctor.getSpecialistIn());

                    context.startActivity(intent);
                }
            });

        }
    }
}
