package com.vrnitsolution.healthapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vrnitsolution.healthapp.DashboardProfile.DashboardProfile;
import com.vrnitsolution.healthapp.GetNotification.AllNotification;

import java.util.Map;

public class FCMNotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);


        if (message.getData().size() > 0) {
            // Handle data payload
            Map<String, String> data = message.getData();

            Log.d("MESSAGEDATA",""+data);

            // Create an Intent to open the target activity
            Intent intent = new Intent(this, AllNotification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear other activities on top of the stack
//            intent.putExtra("key", "value"); // You can add extra data to the intent if needed

            // Create a PendingIntent for the target activity
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            // Create the notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                    .setContentTitle("Notification Title")
                    .setContentText("Notification Message")
                    .setAutoCancel(true) // Dismiss the notification when tapped
                    .setContentIntent(pendingIntent); // Set the content intent

            // Get the NotificationManager and notify the notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder.build());
        }

    }
}
