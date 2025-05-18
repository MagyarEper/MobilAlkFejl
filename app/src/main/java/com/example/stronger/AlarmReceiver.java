package com.example.stronger;

// Removed the incorrect Manifest import

import android.Manifest; // Import this for permission strings
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build; // Import for SDK version check
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String workoutName = intent.getStringExtra("WORKOUT_NAME");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", (int) System.currentTimeMillis()); // Get unique ID

        String message = (workoutName != null && !workoutName.isEmpty())
                ? "Time for your '" + workoutName + "' workout!"
                : "Time for your workout!";

        // Ensure your NotificationHelper class exists and is correctly implemented
        NotificationHelper.createNotificationChannel(context); // Ensure channel exists

        Intent mainIntent = new Intent(context, WorkoutListActivity.class);
        // Add FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP if you want to control activity stack
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId, // Use the unique notification ID for the request code too
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID) // Use CHANNEL_ID from NotificationHelper
                .setSmallIcon(R.drawable.ic_fitness_notification) // Ensure this drawable exists
                .setContentTitle("Workout Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Reminders are important
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Permission check for POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API 33
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("AlarmReceiver", "POST_NOTIFICATIONS permission not granted for reminder.");
                // In a BroadcastReceiver, you typically cannot request permissions directly.
                // The permission should have been granted by the user in an Activity beforehand.
                // If not granted, the notification simply won't show on API 33+.
                // You might log this or handle it in a way that your app knows the reminder wasn't shown.
                return; // Exit if permission is not granted
            }
        }

        // If permission is granted (or not needed for older OS versions), show the notification.
        notificationManager.notify(notificationId, builder.build()); // Use the unique notification ID
        Log.d("AlarmReceiver", "Reminder notification shown for: " + (workoutName != null ? workoutName : "Generic Workout"));
    }
}