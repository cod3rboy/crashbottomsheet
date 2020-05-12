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

package com.cod3rboy.crashbottomsheetexample;

import android.app.Application;

import com.cod3rboy.crashbottomsheet.CrashBottomSheet;
import com.cod3rboy.crashbottomsheet.DeviceInfo;

public class CustomApplication extends Application {
    public CustomApplication() {
        super();
        // Register without custom report action callback
        // CrashBottomSheet.register(this);
        // Register with custom report action callback
        CrashBottomSheet.register(this, new CrashBottomSheet.onCrashReport() {
            @Override
            public void handleCrashReport(String stackTrace, DeviceInfo deviceInfo) {
                // Write your custom action here to handle crash report e.g. send report to your server or log it in the file or whatever.
                CrashBottomSheet.sendCrashEmail(CustomApplication.this, stackTrace, deviceInfo); // Using default email action for now.
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // You can also register CrashBottomSheet here
    }
}
