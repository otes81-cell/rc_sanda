package com.rcpapiotes.app;

import android.app.*;
import android.content.*;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        String ch = "rcpapi_alarm";
        long[] vib = {0, 400, 200, 400, 200, 400};
        Uri snd = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel nc = new NotificationChannel(ch, "Timer & Pengingat", NotificationManager.IMPORTANCE_HIGH);
            nc.enableVibration(true); nc.setVibrationPattern(vib);
            nc.setSound(snd, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            nm.createNotificationChannel(nc);
        }
        PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, ch) : new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
         .setContentTitle(i.getStringExtra("t")).setContentText(i.getStringExtra("x"))
         .setStyle(new Notification.BigTextStyle().bigText(i.getStringExtra("x")))
         .setContentIntent(pi).setAutoCancel(true);
        if (Build.VERSION.SDK_INT < 26) { b.setPriority(Notification.PRIORITY_HIGH); b.setSound(snd); b.setVibrate(vib); }
        nm.notify(i.getIntExtra("n", 0), b.build());
    }
}
