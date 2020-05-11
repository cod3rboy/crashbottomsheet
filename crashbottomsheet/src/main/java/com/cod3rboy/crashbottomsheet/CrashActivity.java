package com.cod3rboy.crashbottomsheet;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.transition.Slide;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;


public class CrashActivity extends AppCompatActivity {
    private String mStackTrace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slideUpTransition = new Slide(Gravity.BOTTOM);
            getWindow().setEnterTransition(slideUpTransition);
        }
        Log.e("CrashActivity", "onCreate()");
        mStackTrace = getIntent().getStringExtra(CrashBottomSheet.EXTRA_STACK_TRACE);

        View dialogView = getLayoutInflater().inflate(R.layout.bottomsheet_dialog, null);
        MaterialButton btnPositive = dialogView.findViewById(R.id.btn_positive);
        MaterialButton btnNegative = dialogView.findViewById(R.id.btn_negative);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setDismissWithAnimation(true);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(dialog1 -> CrashActivity.this.finish());
        btnNegative.setOnClickListener((view) -> dialog.dismiss());
        btnPositive.setOnClickListener((view) -> {
            if (CrashBottomSheet.getInstance() != null
                    && CrashBottomSheet.getInstance().getCallback() != null) {
                CrashBottomSheet.getInstance().getCallback().handleCrashReport(mStackTrace, new DeviceInfo(this.getApplicationContext()));
            } else {
                CrashBottomSheet.sendCrashEmail(this.getApplicationContext(), mStackTrace, new DeviceInfo(this.getApplicationContext()));
            }
            dialog.dismiss();
        });
        dialog.show();
    }

}
