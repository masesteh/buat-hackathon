package com.example.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(this);
        button.setText("Click Me");

        button.setOnClickListener(v -> {
            TextView textView = new TextView(this);
            textView.setText("Hello, World!");
            textView.setGravity(Gravity.CENTER);
            layout.addView(textView);
        });
        layout.addView(button);
        setContentView(layout);
    }
}