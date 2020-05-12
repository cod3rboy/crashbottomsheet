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

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;


/**
 * Crash Activity which hosts the BottomSheetDialog.
 */
public class CrashActivity extends AppCompatActivity {
    private static final String LOG_TAG = CrashActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onCreate() is called");
        String stackTrace = getIntent().getStringExtra(CrashBottomSheet.EXTRA_STACK_TRACE);
        View dialogView = getLayoutInflater().inflate(R.layout.bottomsheet_dialog, null);
        MaterialButton btnPositive = dialogView.findViewById(R.id.btn_positive);
        MaterialButton btnNegative = dialogView.findViewById(R.id.btn_negative);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setDismissWithAnimation(true);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(dialog1 -> CrashActivity.this.finish()); // Finish activity on dialog dismiss
        btnNegative.setOnClickListener((view) -> dialog.dismiss());
        btnPositive.setOnClickListener((view) -> {
            if (CrashBottomSheet.getInstance() != null
                    && CrashBottomSheet.getInstance().getCallback() != null) {
                // Invoking registered callback
                Log.d(LOG_TAG, "Invoking registered reportCallback()");
                CrashBottomSheet.getInstance().getCallback().handleCrashReport(stackTrace, new DeviceInfo(this.getApplicationContext()));
            } else {
                // Performing default action
                Log.d(LOG_TAG, "No registered reportCallback(). Performing default action to open Email app with crash report.");
                CrashBottomSheet.sendCrashEmail(this.getApplicationContext(), stackTrace, new DeviceInfo(this.getApplicationContext()));
            }
            dialog.dismiss();
        });
        dialog.show();
    }

}
