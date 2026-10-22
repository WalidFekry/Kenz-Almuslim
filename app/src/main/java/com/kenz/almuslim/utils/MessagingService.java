package com.kenz.almuslim.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kenz.almuslim.R;
import com.kenz.almuslim.ui.main.MainActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    public static final String RANDOM_KEY = "OpenName";
    private static final String CHANNEL_ID = "Remote_notification_fire_base_new";
    private static final String CHANNEL_NAME = "إشعارات كنز المسلم";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;
        String imageUrl = null;
        Class<?> targetActivity = MainActivity.class;

        // 1. Extract from Notification payload if available
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            if (remoteMessage.getNotification().getImageUrl() != null) {
                imageUrl = remoteMessage.getNotification().getImageUrl().toString();
            }
        }

        // 2. Fallback to Data payload if title or body is missing
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            if (title == null || title.isEmpty()) {
                title = data.get("title");
            }
            if (body == null || body.isEmpty()) {
                body = data.get("message");
                if (body == null || body.isEmpty()) {
                    body = data.get("body");
                }
            }
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = data.get("image");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = data.get("image_url");
                }
            }

            String targetClassName = data.get(RANDOM_KEY);
            if (targetClassName != null && !targetClassName.isEmpty()) {
                try {
                    targetActivity = Class.forName(targetClassName);
                } catch (Exception ignored) {
                }
            }
        }

        // Fallbacks if title or body is still null
        if (title == null || title.isEmpty()) {
            title = getString(R.string.app_name);
        }
        if (body == null || body.isEmpty()) {
            body = "رسالة جديدة من " + getString(R.string.app_name);
        }

        // 3. Build & Send Notification
        if (imageUrl != null && !imageUrl.isEmpty()) {
            sendNotificationWithImage(title, body, imageUrl, targetActivity);
        } else {
            sendNotification(title, body, null, targetActivity);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("إشعارات ورسائل تطبيق كنز المسلم");
                channel.enableLights(true);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(String title, String body, Bitmap imageBitmap, Class<?> targetActivity) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        createNotificationChannel(notificationManager);

        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        int requestCode = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, pendingIntentFlags);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        if (imageBitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(body));
        }

        int notificationId = (int) (System.currentTimeMillis() & 0x7fffffff);
        notificationManager.notify(notificationId, builder.build());
    }

    private void sendNotificationWithImage(String title, String body, String imageUrl, Class<?> targetActivity) {
        try {
            Picasso.get().load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    sendNotification(title, body, bitmap, targetActivity);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    sendNotification(title, body, null, targetActivity);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });
        } catch (Exception e) {
            sendNotification(title, body, null, targetActivity);
        }
    }
}
