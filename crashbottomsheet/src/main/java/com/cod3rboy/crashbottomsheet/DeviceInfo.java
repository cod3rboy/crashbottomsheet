package com.cod3rboy.crashbottomsheet;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.lang.reflect.Field;

public class DeviceInfo {
    private String mAppName;
    private String mPackageName;
    private String mAndroidCodeName;
    private String mAndroidVersion;
    private String mManufacturer;
    private String mModel;
    private String mBrand;
    private String mProduct;

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

    public String getFormattedInfo() {
        return "Application Name : " + mAppName +
                "\nPackage name : " + mPackageName +
                "\nAndroid OS : " + mAndroidCodeName + "-" + mAndroidVersion +
                "\nManufacturer : " + mManufacturer +
                "\nModel : " + mModel +
                "\nBrand : " + mBrand +
                "\nProduct : " + mProduct;
    }
}
