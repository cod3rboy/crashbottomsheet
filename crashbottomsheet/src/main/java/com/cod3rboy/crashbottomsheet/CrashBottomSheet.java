package com.cod3rboy.crashbottomsheet;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrashBottomSheet implements Thread.UncaughtExceptionHandler {
    static final String LOG_TAG = CrashBottomSheet.class.getSimpleName();

    static String EXTRA_STACK_TRACE = "extra_stack_trace";
    private static final int STACK_TRACE_CHARS_LIMIT = (127 * 1024) / 2; // 127 KB Limit

    public interface onCrashReport {
        void handleCrashReport(String stackTrace, DeviceInfo deviceInfo);
    }

    private static CrashBottomSheet mSingleton;

    private static Thread.UncaughtExceptionHandler mOldHandler;

    private static long MS_BETWEEN_CRASHES = 3000; // 3 seconds gap after any crash is needed
    private static final String PREFERENCE_FILE_NAME = "com.cod3rboy.crashbottomsheet";
    private static final String PREFERENCE_FIELD_NAME = "last_crash_time";

    public static void register(Application appContext, onCrashReport reportCallback) {
        if (mSingleton == null) {
            // Registered for first time
            mSingleton = new CrashBottomSheet(appContext, reportCallback);
            mOldHandler = Thread.getDefaultUncaughtExceptionHandler();

            if (mOldHandler != null && !mOldHandler.getClass().getName().startsWith("com.android.internal.os")) {
                Log.w(LOG_TAG, "WARNING! Your app has already registered some other custom UncaughtExceptionHandler and CrashBottomSheet is replacing it. You must register CrashBottomSheet before any other custom UncaughtExceptionHandler.");
            }
            Thread.setDefaultUncaughtExceptionHandler(mSingleton);
        } else {
            Log.w(LOG_TAG, "CrashBottomSheet is already registered. Nothing to do!");
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
    }

    onCrashReport getCallback() {
        return mCallback;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (isErrorLoopPossible()) {
            if (mOldHandler != null) mOldHandler.uncaughtException(t, e);
            return;
        }
        setCurrentCrashTimestamp();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(stream));
        String stackTrace = stream.toString();
        try {
            stream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Limit stacktrace content to avoid TransactionTooLargeException.
        // See : https://developer.android.com/reference/android/os/TransactionTooLargeException.html
        if (stackTrace.length() > STACK_TRACE_CHARS_LIMIT) {
            String marker = " <TRUNCATED! STACK TRACE IS TOO LARGE>";
            stackTrace = stackTrace.substring(0, STACK_TRACE_CHARS_LIMIT - marker.length()) + marker;
        }

        Intent i = new Intent(mAppContext, com.cod3rboy.crashbottomsheet.CrashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EXTRA_STACK_TRACE, stackTrace);
        mAppContext.startActivity(i);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private boolean isErrorLoopPossible() {
        long now = new Date().getTime();
        long lastCrash = getLastCrashTimestamp();
        return (now - lastCrash) <= MS_BETWEEN_CRASHES;
    }

    private void setCurrentCrashTimestamp() {
        Date currentTimeStamp = new Date();
        mAppContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(PREFERENCE_FIELD_NAME, currentTimeStamp.getTime())
                .apply();
    }

    private long getLastCrashTimestamp() {
        return mAppContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                .getLong(PREFERENCE_FIELD_NAME, 0);
    }
}
