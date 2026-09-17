// MainActivity.java

package io.ugm;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("ugm");
        title.setTextSize(40);
        title.setGravity(Gravity.CENTER);

        Button button = new Button(this);
        button.setText("Open Pomodoro timer");

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, TimerActivity.class);
            startActivity(intent);
        });

        layout.addView(title);
        layout.addView(button);

        Button openCalendarButton = new Button(this);
        openCalendarButton.setText("Open calendar");
        openCalendarButton.setTextSize(20);
        openCalendarButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CalendarActivity.class)));

        layout.addView(openCalendarButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(layout);
    }
}