package com.cod3rboy.crashbottomsheetexample;

import android.app.Application;
import android.util.Log;

import com.cod3rboy.crashbottomsheet.CrashBottomSheet;

public class CustomApplication extends Application{
    public CustomApplication() {
        super();
        CrashBottomSheet.register(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("CustomApplication", "onCreate() invoked");
    }
}
