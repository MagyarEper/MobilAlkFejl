package com.example.stronger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class NotificationHelper {
    static final String CHANNEL_ID = "workout_channel_id";
    private static final String CHANNEL_NAME = "Workout Notifications";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showWorkoutSavedNotification(Context context, String workoutName) {
        createNotificationChannel(context); // Ensure channel exists

        Intent intent = new Intent(context, WorkoutListActivity.class); // Open list on tap
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fitness_notification) // You'll need an icon
                .setContentTitle("Workout Saved!")
                .setContentText("Your workout '" + workoutName + "' has been successfully saved.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // For API 33+, you need to request this permission.
            // Handle this appropriately, perhaps by asking for permission earlier.
            Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted.");
            // You might skip showing the notification or inform the user.
            return;
        }
        notificationManager.notify(1, builder.build()); // Unique ID for this notification
    }
}
