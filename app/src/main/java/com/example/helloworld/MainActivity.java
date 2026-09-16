package com.example.helloworld;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.lang.*;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(this);
        button.setText("click me");

        button.setOnClickListener(v -> {
            button.setText("hello world");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                button.setText("click me");
            }, 3000);
        });

        setContentView(button);
    }
}