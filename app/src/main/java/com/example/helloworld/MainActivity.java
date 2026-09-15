package com.example.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView message = new TextView(this);
        message.setText("Hello, World!");
        message.setTextSize(32);
        message.setGravity(Gravity.CENTER);
        setContentView(message);
    }
}