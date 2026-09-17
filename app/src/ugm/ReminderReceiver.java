package io.ugm;

import android.app.*;
import android.content.*;
import android.os.*;

public final class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "calendar_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String date = intent.getStringExtra("date");
        String title = intent.getStringExtra("title");
        if (title == null || title.trim().isEmpty()) {
            title = "Calendar reminder";
        }

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Calendar reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        Intent openCalendar = new Intent(context, CalendarActivity.class);
        openCalendar.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                date == null ? 0 : date.hashCode(),
                openCalendar,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText("Reminder for " + (date == null ? "your calendar" : date))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(title)
                    .setContentText("Reminder for " + (date == null ? "your calendar" : date))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }

        try {
            manager.notify(date == null ? 0 : date.hashCode(), notification);
        } catch (SecurityException exception) {
            // Notifications can be denied on Android 13 and newer.
        }
    }
}
