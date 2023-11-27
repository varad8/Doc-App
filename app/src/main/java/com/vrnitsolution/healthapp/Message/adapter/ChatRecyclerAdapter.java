package com.vrnitsolution.healthapp.Message.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vrnitsolution.healthapp.Message.model.ChatMessageModel;
import com.vrnitsolution.healthapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.UserModelViewHolder> {
    private Context context;
    ArrayList<ChatMessageModel>chatMessageModels;

    public ChatRecyclerAdapter(Context context, ArrayList<ChatMessageModel> chatMessageModels) {
        this.context = context;
        this.chatMessageModels = chatMessageModels;
    }

    @NonNull
    @Override
    public ChatRecyclerAdapter.UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_adapter_layout, parent, false);
        return new ChatRecyclerAdapter.UserModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRecyclerAdapter.UserModelViewHolder holder, int position) {
        ChatMessageModel chatMessageModel=chatMessageModels.get(position);
        String activeuser=getActiveUser();


        if (!activeuser.isEmpty())
        {
            if (activeuser.equals(chatMessageModel.getSenderId()))
            {
                holder.oppoLayout.setVisibility(View.GONE);
                holder.myLayout.setVisibility(View.VISIBLE);
                holder.myMessage.setText(chatMessageModel.getMessage());
                holder.myMsgTime.setText(formatTimestamp(chatMessageModel.getTimestamp()));

            }
            else
            {
                holder.myLayout.setVisibility(View.GONE);
                holder.oppoLayout.setVisibility(View.VISIBLE);
                holder.oppoMessage.setText(chatMessageModel.getMessage());
                holder.oppoMsgTime.setText(formatTimestamp(chatMessageModel.getTimestamp()));

            }
        }

    }

    @Override
    public int getItemCount() {
        return chatMessageModels.size();
    }



    class UserModelViewHolder extends RecyclerView.ViewHolder {
        LinearLayout oppoLayout,myLayout;
        TextView oppoMessage,myMessage;
        TextView myMsgTime,oppoMsgTime;
        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);

            oppoLayout=itemView.findViewById(R.id.oppoLayout);
            myLayout=itemView.findViewById(R.id.myLayout);

            //for set text message
            oppoMessage=itemView.findViewById(R.id.oppoMessage);
            myMessage=itemView.findViewById(R.id.myMessage);

            //set time
            myMsgTime=itemView.findViewById(R.id.myMsgTime);
            oppoMsgTime=itemView.findViewById(R.id.oppoMsgTime);
        }

    }

    public interface OnSearchUserClickListner {
        void onUserClickListner(int position);
    }


    /**
     * Here we get active user that return userid
     * @return
     */

    public String getActiveUser() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = context.getSharedPreferences("DoctorPrefs", MODE_PRIVATE);
        if (currentUser != null) {
            return currentUser.getUid();
        } else if (preferences != null) {
            // Retrieve doctor data from SharedPreferences
            String userid = preferences.getString("doctorId", "");
            if (!userid.isEmpty()) {
                return userid;
            }

        }

        return null;
    }


    /**
     * Here Passed the timestamp and format when message is latest today's sent then set today if previous day yesterdy other wise set full date and time
     * @param timestamp
     * @return
     */
    private String formatTimestamp(Timestamp timestamp) {
        long now = System.currentTimeMillis();
        long timestampMillis = timestamp.toDate().getTime();

        if (isToday(now, timestampMillis)) {
            return "Today " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate());
        } else if (isYesterday(now, timestampMillis)) {
            return "Yesterday " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate());
        } else {
            return new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(timestamp.toDate());
        }
    }

    private boolean isToday(long now, long timestampMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTimeInMillis(timestampMillis);

        return calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(long now, long timestampMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTimeInMillis(timestampMillis);

        return calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR);
    }


}
