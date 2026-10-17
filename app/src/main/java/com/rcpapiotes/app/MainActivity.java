package com.rcpapiotes.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import java.util.Calendar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.*;

public class MainActivity extends Activity {
    WebView web;
    ValueCallback<Uri[]> picker;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 2);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        web.addJavascriptInterface(new Object() {
            PendingIntent pi(String id, String t, String x) {
                Intent i = new Intent(MainActivity.this, AlarmReceiver.class);
                int n = (int) (Long.parseLong(id) % 1000000007L);
                i.putExtra("n", n); i.putExtra("t", t); i.putExtra("x", x);
                return PendingIntent.getBroadcast(MainActivity.this, n, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            }
            AlarmManager am() { return (AlarmManager) getSystemService(Context.ALARM_SERVICE); }
            @JavascriptInterface public void schedule(String id, String end, String t, String x) {
                long at = Long.parseLong(end); PendingIntent p = pi(id, t, x);
                if (Build.VERSION.SDK_INT >= 31 && !am().canScheduleExactAlarms())
                    am().setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
                else am().setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
            }
            @JavascriptInterface public void cancel(String id) { am().cancel(pi(id, "", "")); }
            @JavascriptInterface public void daily(String id, String h, String m, String t, String x) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h)); c.set(Calendar.MINUTE, Integer.parseInt(m));
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
                if (c.getTimeInMillis() < System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1);
                am().setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi(id, t, x));
            }
            @JavascriptInterface public void share(String name, String text) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, name);
                i.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(i, name));
            }
        }, "AndroidBridge");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                picker = cb;
                try { startActivityForResult(p.createIntent(), 1); }
                catch (Exception e) { picker = null; return false; }
                return true;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && picker != null) {
            picker.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            picker = null;
        }
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
