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
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class to register custom DefaultUncaughtExceptionHandler.
 * Use either {@link CrashBottomSheet#register(Application, onCrashReport)} method or
 * {@link CrashBottomSheet#register(Application)} method if you do not want your custom report action.
 */
public class CrashBottomSheet implements Thread.UncaughtExceptionHandler {
    private static final String LOG_TAG = CrashBottomSheet.class.getSimpleName();

    /**
     * Key used in intent to forward stack trace to {@link CrashActivity} class.
     */
    static String EXTRA_STACK_TRACE = "extra_stack_trace";
    /**
     * Max Limit on numbers of characters in stack trace to forward to {@link CrashActivity}
     * to prevent {@link android.os.TransactionTooLargeException}.
     */
    private static final int STACK_TRACE_CHARS_LIMIT = (127 * 1024) / 2; // 127 KB Limit

    /**
     * Callback interface used to allow a custom report action.
     */
    public interface onCrashReport {
        void handleCrashReport(String stackTrace, DeviceInfo deviceInfo);
    }

    public static final long MIN_MS_BETWEEN_CRASHES = 3000; // 3 seconds gap is needed after any crash.
    // Name of Preference file
    private static final String PREFERENCE_FILE_NAME = "com.cod3rboy.crashbottomsheet";
    // Name of Preference field key
    private static final String PREFERENCE_FIELD_NAME = "last_crash_time";


    // Minimum no of seconds allowed between any two crashes to detect CrashLoop.
    private static long mMinCrashIntervalMs = MIN_MS_BETWEEN_CRASHES;
    // Singleton instance of registered custom DefaultUncaughtExceptionHandler
    private static CrashBottomSheet mSingleton;
    // Holds old DefaultUncaughtExceptionHandler before we register ours.
    private static Thread.UncaughtExceptionHandler mOldHandler;

    /**
     * Method to register {@link CrashBottomSheet} with the application.
     * This method is needed to be called as early as possible during app startup.
     * You must invoke this method in the constructor of Application Class.
     *
     * @param appContext     Application Context object
     * @param reportCallback Your custom action to invoke when report button is pressed by user in bottom sheet.
     *                       or null if you want to use default email report action.
     */
    public static void register(@NonNull Application appContext, @Nullable onCrashReport reportCallback) {
        if (mSingleton == null) {
            // Registered for first time
            mSingleton = new CrashBottomSheet(appContext, reportCallback);
            // Store previous handler
            mOldHandler = Thread.getDefaultUncaughtExceptionHandler();

            if (mOldHandler != null && !mOldHandler.getClass().getName().startsWith("com.android.internal.os")) {
                Log.w(LOG_TAG, "WARNING! Your app has already registered some other custom UncaughtExceptionHandler and CrashBottomSheet is replacing it. You must register CrashBottomSheet before any other custom UncaughtExceptionHandler.");
            }
            Thread.setDefaultUncaughtExceptionHandler(mSingleton);
            Log.d(LOG_TAG, "CrashBottomSheet registered successfully with the application!");
        } else {
            // Already registered
            Log.w(LOG_TAG, "CrashBottomSheet is already registered. Nothing to do!");
        }
    }

    /**
     * Method to register {@link CrashBottomSheet} with the application.
     * This method is needed to be called as early as possible during app startup.
     * You must invoke this method in the constructor of Application Class.
     *
     * @param appContext Application Context object
     */
    public static void register(@NonNull Application appContext) {
        register(appContext, null);
    }

    /**
     * Set allowed minimum number of milliseconds to pass before next crash is to be considered as a valid crash
     * in order to prevent CrashLoop. Keep this value low and close to {@link CrashBottomSheet#MIN_MS_BETWEEN_CRASHES}.
     * You cannot set this value less than value of {@link CrashBottomSheet#MIN_MS_BETWEEN_CRASHES} constant.
     * Default value is perfect in most cases.
     *
     * @param ms milliseconds
     */
    public static void setMinCrashIntervalMs(long ms) {
        if (ms < MIN_MS_BETWEEN_CRASHES) {
            Log.w(LOG_TAG, "WARNING! Called setMinCrashIntervalMs() with value less than MIN_MS_BETWEEN_CRASHES=" + MIN_MS_BETWEEN_CRASHES + " so it is ignored and MIN_MS_BETWEEN_CRASHES value is used.");
        } else {
            mMinCrashIntervalMs = ms;
        }
    }

    /**
     * This is the default email report action which launches email app with crash information loaded
     * when user presses report button in {@link CrashBottomSheet} or displays a toast message if no
     * email app is installed in the device.
     * You can also call this method from your custom action callback registered with
     * {@link CrashBottomSheet#register(Application, onCrashReport)} method.
     *
     * @param context    Any Context Object
     * @param stackTrace String containing stack trace of crash
     * @param deviceInfo instance of {@link DeviceInfo} class holds information about device on which crash occurs
     */
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
                                    new SimpleDateFormat("EEE, dd-MMM-yyyy, HH:mm:ss", Locale.getDefault()).format(new Date()),
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

    /**
     * Returns instance of {@link CrashBottomSheet} class registered as DefaultUncaughtExceptionHandler.
     *
     * @return {@link CrashBottomSheet} instance
     */
    static CrashBottomSheet getInstance() {
        return mSingleton;
    }

    // Application context object
    private Application mAppContext;
    // User registered custom report action callback
    private onCrashReport mCallback;

    /**
     * Constructor
     *
     * @param appContext Application context object
     * @param callback   custom report action callback to register
     */
    private CrashBottomSheet(Application appContext, onCrashReport callback) {
        mAppContext = appContext;
        mCallback = callback;
    }

    /**
     * Returns user registered custom report action callback
     *
     * @return custom report action callback
     */
    onCrashReport getCallback() {
        return mCallback;
    }

    /**
     * This method is registered as DefaultUncaughtExceptionHandler and is invoked when an exception
     * remains unhandled within application.
     * It replaces the default system provided default exception handler.
     *
     * @param t Thread in which exception occurred
     * @param e Exception object̥
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (isErrorLoopPossible()) { // CrashLoop Possible
            Log.w(LOG_TAG, "WARNING! Possibility of triggering a CrashLoop. So keeping CrashBottomSheet silent.");
            if (mOldHandler != null) mOldHandler.uncaughtException(t, e);
            return;
        }
        // Save crash timestamp
        setCurrentCrashTimestamp();
        // Get stack trace of crash
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
            Log.w(LOG_TAG, "WARNING! Stack trace size exceeds maximum limit of 127KB so it is truncated.");
            String marker = " <TRUNCATED! STACK TRACE IS TOO LARGE>";
            stackTrace = stackTrace.substring(0, STACK_TRACE_CHARS_LIMIT - marker.length()) + marker;
        }

        // Start CrashActivity and forward Stack Trace to it.
        Intent i = new Intent(mAppContext, com.cod3rboy.crashbottomsheet.CrashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EXTRA_STACK_TRACE, stackTrace);
        mAppContext.startActivity(i);
        // Kill current process of application
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    /**
     * Determines whether there is a possibility to trigger CrashLoop.
     * It is possible to trigger CrashLoop if next crash occurs earlier than
     * previous crash time + {@link CrashBottomSheet#mMinCrashIntervalMs} value.
     *
     * @return true if CrashLoop can occur otherwise false
     */
    private boolean isErrorLoopPossible() {
        long now = new Date().getTime();
        long lastCrash = getLastCrashTimestamp();
        return (now - lastCrash) <= mMinCrashIntervalMs;
    }

    /**
     * Saves the timestamp of current crash in preferences.
     */
    private void setCurrentCrashTimestamp() {
        Date currentTimeStamp = new Date();
        mAppContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(PREFERENCE_FIELD_NAME, currentTimeStamp.getTime())
                .apply();
    }

    /**
     * Retrieves the timestamp of last crash from preferences.
     *
     * @return last crash timestamp
     */
    private long getLastCrashTimestamp() {
        return mAppContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
                .getLong(PREFERENCE_FIELD_NAME, 0);
    }
}
