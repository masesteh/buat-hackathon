package io.ugm;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public final class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("second page");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);

        Button backButton = new Button(this);
        backButton.setText("go back");

        backButton.setOnClickListener(v -> {
            finish();
        });

        layout.addView(title);
        layout.addView(backButton);

        setContentView(layout);
    }
}