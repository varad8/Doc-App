package com.vrnitsolution.healthapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.vrnitsolution.healthapp.R;

import java.util.Map;

public class ServiceHoursAdapter extends RecyclerView.Adapter<ServiceHoursAdapter.ServiceHoursViewHolder> {
    private Map<String, String> serviceHoursMap;

    public ServiceHoursAdapter(Map<String, String> serviceHoursMap) {
        this.serviceHoursMap = serviceHoursMap;
    }

    @NonNull
    @Override
    public ServiceHoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.consultation_item, parent, false);
        return new ServiceHoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceHoursViewHolder holder, int position) {
        String day = (String) serviceHoursMap.keySet().toArray()[position];
        String hours = serviceHoursMap.get(day);

        if (hours.equals("Closed"))
        {
            holder.constraintLayout.setBackgroundResource(R.drawable.button_shape_inactive);
        }
        else
        {
            holder.constraintLayout.setBackgroundResource(R.drawable.button_shape3);
        }


        holder.dayTextView.setText(day.substring(0,3));
        holder.hoursTextView.setText(hours);
    }

    @Override
    public int getItemCount() {
        return serviceHoursMap.size();
    }

    public static class ServiceHoursViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView dayTextView;
        TextView hoursTextView;

        public ServiceHoursViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.daytext);
            hoursTextView = itemView.findViewById(R.id.timingText);
            constraintLayout=itemView.findViewById(R.id.constraint);
        }
    }
}
