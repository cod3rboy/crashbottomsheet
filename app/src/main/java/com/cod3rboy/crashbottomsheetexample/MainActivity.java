package com.cod3rboy.crashbottomsheetexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void crashMe(View view) throws NullPointerException {
        throw new NullPointerException("Manually threw NullPointerException");
    }
}
