package com.shenll.operrquiz;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * create class and extends from service makes works in background and for interacting with user
 */
public class NotificationTimerService extends Service {
    public static boolean isServiceEnable = false;
    private int totalSeconds, minute, minutes, seconds;
    private Handler handler;
    private NotificationManagerCompat notificationManagerCompat;
    private Runnable runnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceEnable = true;
        // creating handler object
        handler = new Handler();
        minute = 5;
        // when build type dev then set minute one
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("dev")) {
            minute = 1;
        }
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create notification channel
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }
        // call notification method
        notification();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * create notification object then adding required content for displaying in notification then start foreground service
     */
    private void notification() {
        // prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // build notification by setting required values
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, getString(R.string.app_name))
                .setContentTitle(getString(R.string.operr_driver))
                .setContentText(getString(R.string.time_remaining))
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_icon))
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setChannelId(getString(R.string.app_name));
        // start foreground service with notification id and notification object
        startForeground(100, notification.build());

        // calling startTimer by providing notification object
        startTimer(notification);
    }

    /**
     * startTimer runs countdown time based on number of minutes and it take notification object for updating data to
     * notification view continuously.
     *
     * @param notification {@link NotificationCompat.Builder}
     */
    private void startTimer(final NotificationCompat.Builder notification) {
        totalSeconds = (minute * 60);
        // runnable work with delay 1 sec and handler used for set changed time in runtime to notification user interface
        runnable = new Runnable() {
            @Override
            public void run() {
                minutes = totalSeconds / 60;
                seconds = totalSeconds % 60;
                // update time whenever change of minutes and seconds
                notification.setSubText(getString(R.string.timer, minutes, seconds));
                notificationManagerCompat.notify(100, notification.build());
                handler.postDelayed(this, 1000);
                if (totalSeconds == 0) {
                    // remove runnable after time over
                    handler.removeCallbacks(runnable);
                    // call stop foreground service method for stop service
                    stopForegroundService(notification);
                }
                --totalSeconds;
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // when service destroy need to change running flag.
        isServiceEnable = false;
    }

    /**
     * when time complete change notification properties and stop the foreground service
     *
     * @param notification {@link NotificationCompat.Builder}
     */
    private void stopForegroundService(NotificationCompat.Builder notification) {
        stopForeground(true);
        // stop service when task complete
        stopSelf();
        // change notification properties when time complete
        notification.setOngoing(false);
        notification.setAutoCancel(true);
        notification.setContentText(getString(R.string.break_end));
        notification.setSubText(getString(R.string.time_end));
        notificationManagerCompat.notify(100, notification.build());
    }
}
