package com.cod3rboy.crashbottomsheet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeviceInfo {
    private String mAppName;
    private String mPackageName;
    private String mAndroidCodeName;
    private String mAndroidVersion;
    private String mManufacturer;
    private String mModel;
    private String mBrand;
    private String mProduct;
    private String mAPKBuildDate;
    private String mAPKVersion;

    public DeviceInfo(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        mAppName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        mPackageName = context.getPackageName();
        String codeName = "UNKNOWN";
        for (Field field : Build.VERSION_CODES.class.getFields()) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    codeName = field.getName();
                    break;
                }
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        mAndroidCodeName = codeName;
        mAndroidVersion = Build.VERSION.RELEASE;
        mManufacturer = Build.MANUFACTURER;
        mModel = Build.MODEL;
        mBrand = Build.BRAND;
        mProduct = Build.PRODUCT;
        mAPKBuildDate = getAPKBuildDateAsString(context, new SimpleDateFormat("EEE, dd-MMM-yyyy, HH:mm:ss", Locale.getDefault()));
        mAPKVersion = getAPKVersionName(context);
    }

    public String getAppName() {
        return mAppName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getAndroidCodeName() {
        return mAndroidCodeName;
    }

    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    public String getManufacturer() {
        return mManufacturer;
    }

    public String getModel() {
        return mModel;
    }

    public String getBrand() {
        return mBrand;
    }

    public String getProduct() {
        return mProduct;
    }

    public String getAPKBuildDate() {
        return mAPKBuildDate;
    }

    public String getAPKVersion() {
        return mAPKVersion;
    }

    public String getFormattedInfo() {
        return "Application Name : " + mAppName +
                "\nPackage name : " + mPackageName +
                "\nAPK Build Date " + mAPKBuildDate +
                "\nAPK Version Name : " + mAPKVersion +
                "\nAndroid OS : " + mAndroidCodeName + "-" + mAndroidVersion +
                "\nManufacturer : " + mManufacturer +
                "\nModel : " + mModel +
                "\nBrand : " + mBrand +
                "\nProduct : " + mProduct;
    }

    /**
     * INTERNAL method that returns the build date of the current APK as a string, or null if unable to determine it.
     *
     * @param context    A valid context. Must not be null.
     * @param dateFormat DateFormat to use to convert from Date to String
     * @return The formatted date, or "Unknown" if unable to determine it.
     */
    @Nullable
    private static String getAPKBuildDateAsString(@NonNull Context context, @NonNull DateFormat dateFormat) {
        long buildDate;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);

            //If this failed, try with the old zip method
            ZipEntry ze = zf.getEntry("classes.dex");
            buildDate = ze.getTime();


            zf.close();
        } catch (Exception e) {
            buildDate = 0;
        }

        if (buildDate > 312764400000L) {
            return dateFormat.format(new Date(buildDate));
        } else {
            return null;
        }
    }

    /**
     * INTERNAL method that returns the version name of the current app, or null if unable to determine it.
     *
     * @param context A valid context. Must not be null.
     * @return The version name, or "Unknown if unable to determine it.
     */
    @NonNull
    private static String getAPKVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
