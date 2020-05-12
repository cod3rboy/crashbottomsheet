/*
 * Copyright 2020 Dheeraj Kumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cod3rboy.crashbottomsheet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

/**
 * Class to store information about device on which crash occurs.
 */
public class DeviceInfo {
    private String mAppName; // Android application name
    private String mPackageName; // Application package name
    private String mAndroidCodeName; // Android OS code name e.g. Lollipop
    private String mAndroidVersion; // Android OS version name e.g. 5.1
    private String mManufacturer; // Device Manufacturer
    private String mModel; // Device Model
    private String mBrand; // Device Brand
    private String mProduct; // Device Product
    private String mAPKVersion; // Installed app APK version name

    /**
     * Constructor
     *
     * @param context application context object̥
     */
    DeviceInfo(Context context) {
        // Get app name
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        mAppName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        // Get app package name
        mPackageName = context.getPackageName();
        // Get android code name
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
        // Get android version
        mAndroidVersion = Build.VERSION.RELEASE;
        // Get manufacturer name
        mManufacturer = Build.MANUFACTURER;
        // Get device model
        mModel = Build.MODEL;
        // Get device brand
        mBrand = Build.BRAND;
        // Get device product
        mProduct = Build.PRODUCT;
        // Get install app apk version name
        mAPKVersion = getAPKVersionName(context);
    }

    /**
     * Returns Application name
     *
     * @return App Name
     */
    public String getAppName() {
        return mAppName;
    }

    /**
     * Returns package name of application
     *
     * @return App Package Name
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Returns code name of installed Android OS e.g. M, L, Q, etc.
     *
     * @return Android OS codename
     */
    public String getAndroidCodeName() {
        return mAndroidCodeName;
    }

    /**
     * Returns installed android os version e.g. 5.0, 6.0, 7.1.1, etc.
     *
     * @return Android OS version
     */
    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    /**
     * Returns name of manufacturer of device.
     *
     * @return Manufacturer name
     */
    public String getManufacturer() {
        return mManufacturer;
    }

    /**
     * Returns model name of device
     *
     * @return Model name
     */
    public String getModel() {
        return mModel;
    }

    /**
     * Returns brand name of device
     *
     * @return Brand name
     */
    public String getBrand() {
        return mBrand;
    }

    /**
     * Returns product name of device
     *
     * @return Product name
     */
    public String getProduct() {
        return mProduct;
    }

    /**
     * Returns app version name of installed APK
     *
     * @return APK version name
     */
    public String getAPKVersion() {
        return mAPKVersion;
    }

    /**
     * Returns formatted string with all device information.
     * Format :-
     * Application Name : [value]
     * Package Name : [value]
     * APK Version Name : [value]
     * Android OS : [android codename]-[android version]
     * Manufacturer : [value]
     * Model : [value]
     * Brand : [value]
     * Product : [value]
     *
     * @return Formatted device information
     */
    public String getFormattedInfo() {
        return "Application Name : " + mAppName +
                "\nPackage Name : " + mPackageName +
                "\nAPK Version Name : " + mAPKVersion +
                "\nAndroid OS : " + mAndroidCodeName + "-" + mAndroidVersion +
                "\nManufacturer : " + mManufacturer +
                "\nModel : " + mModel +
                "\nBrand : " + mBrand +
                "\nProduct : " + mProduct;
    }

    /**
     * Method to determine version name of installed APK.
     *
     * @param context A Context Object
     * @return Version Name or Unknown if fails
     */
    @NonNull
    private static String getAPKVersionName(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
