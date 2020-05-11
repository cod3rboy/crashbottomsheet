package com.cod3rboy.crashbottomsheet;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

public class CrashBottomSheet implements Thread.UncaughtExceptionHandler {
    static String EXTRA_STACK_TRACE = "extra_stack_trace";

    public interface onCrashReport {
        void handleCrashReport(String stackTrace, DeviceInfo deviceInfo);
    }

    private static CrashBottomSheet mSingleton;

    public static void register(Application appContext, onCrashReport reportCallback) {
        if (mSingleton == null) {
            // Registered for first time
            mSingleton = new CrashBottomSheet(appContext, reportCallback);
        } else {
            // Already registered
        }
    }

    public static void register(Application appContext) {
        register(appContext, null);
    }

    public static void sendCrashEmail(Context context, String stackTrace, DeviceInfo deviceInfo) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", context.getString(R.string.cbs_report_email_to), null));

        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos.size() == 0) {
            // Toast No Email App Found
            Toast.makeText(context, context.getString(R.string.cbs_toast_no_email_app), Toast.LENGTH_SHORT)
                    .show();
        } else {
            int i = 0;
            for (; i < resolveInfos.size(); i++) {
                ResolveInfo resolveInfo = resolveInfos.get(i);
                String packageName = resolveInfo.activityInfo.packageName;
                String name = resolveInfo.activityInfo.name;
                if (resolveInfo.activityInfo.applicationInfo.enabled && !packageName.equals("com.android.fallback")) {
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setDataAndType(Uri.parse("mailto:"), "text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getString(R.string.cbs_report_email_to)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.cbs_report_email_subject));
                    intent.putExtra(Intent.EXTRA_TEXT,
                            String.format(Locale.getDefault(), context.getString(R.string.cbs_email_body_format),
                                    context.getString(R.string.app_name),
                                    deviceInfo.getFormattedInfo(),
                                    stackTrace));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setComponent(new ComponentName(packageName, name));
                    context.startActivity(intent);
                    break;
                }
            }
            if (i >= resolveInfos.size()) {
                // Toast No Email App found
                Toast.makeText(context, context.getString(R.string.cbs_toast_no_email_app), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    static CrashBottomSheet getInstance() {
        return mSingleton;
    }

    private Application mAppContext;
    private onCrashReport mCallback;

    private CrashBottomSheet(Application appContext, onCrashReport callback) {
        mAppContext = appContext;
        mCallback = callback;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    onCrashReport getCallback() {
        return mCallback;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(stream));
        String stackTrace = stream.toString();
        try {
            stream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Intent i = new Intent(mAppContext, com.cod3rboy.crashbottomsheet.CrashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EXTRA_STACK_TRACE, stackTrace);
        mAppContext.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
