package io.ugm;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {

    private static final int INK = Color.rgb(27, 38, 49);
    private static final int MUTED = Color.rgb(91, 105, 115);
    private static final int PAPER = Color.rgb(247, 249, 250);
    private static final int TEAL = Color.rgb(0, 121, 112);
    private static final int CORAL = Color.rgb(221, 91, 76);

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(int color, float radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(radius));
        return background;
    }

    private TextView text(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private LinearLayout.LayoutParams fullWidth(int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private Button actionButton(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(54));
        button.setPadding(dp(18), 0, dp(18), 0);
        button.setBackground(roundedBackground(color, 14));
        return button;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(PAPER);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(PAPER);
        scrollView.setFillViewport(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(28), dp(24), dp(24));

        TextView eyebrow = text("YOUR QUIET WORKSPACE", 12, TEAL);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLetterSpacing(0.12f);
        layout.addView(eyebrow, fullWidth(0, 12));

        TextView title = text("Make room\nfor good work.", 38, INK);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLineSpacing(0, 0.92f);
        layout.addView(title, fullWidth(0, 12));

        TextView subtitle = text("Focus on what matters, then give your day somewhere to land.",
                17, MUTED);
        subtitle.setLineSpacing(0, 1.15f);
        layout.addView(subtitle, fullWidth(0, 26));

        LinearLayout featureStrip = new LinearLayout(this);
        featureStrip.setGravity(Gravity.CENTER_VERTICAL);
        featureStrip.setPadding(dp(18), dp(16), dp(18), dp(16));
        featureStrip.setBackground(roundedBackground(Color.rgb(226, 240, 237), 16));

        TextView mark = text("●", 24, CORAL);
        mark.setGravity(Gravity.CENTER);
        featureStrip.addView(mark, new LinearLayout.LayoutParams(dp(34), dp(34)));

        LinearLayout featureCopy = new LinearLayout(this);
        featureCopy.setOrientation(LinearLayout.VERTICAL);
        featureCopy.setPadding(dp(12), 0, 0, 0);
        TextView featureTitle = text("A simple rhythm", 16, INK);
        featureTitle.setTypeface(Typeface.DEFAULT_BOLD);
        featureCopy.addView(featureTitle);
        featureCopy.addView(text("Small sessions. Clear days. Less friction.", 13, MUTED));
        featureStrip.addView(featureCopy, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(featureStrip, fullWidth(0, 28));

        TextView chooseTitle = text("Choose your next step", 14, MUTED);
        chooseTitle.setTypeface(Typeface.DEFAULT_BOLD);
        layout.addView(chooseTitle, fullWidth(0, 10));

        Button timerButton = actionButton("Start a focus session   ›", TEAL);
        timerButton.setOnClickListener(v -> startActivity(new Intent(this, TimerActivity.class)));
        layout.addView(timerButton, fullWidth(0, 10));

        Button calendarButton = actionButton("Plan the day   ›", CORAL);
        calendarButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CalendarActivity.class)));
        layout.addView(calendarButton, fullWidth(0, 0));

        TextView footer = text("ugm  /  focus, plan, repeat", 12, MUTED);
        footer.setGravity(Gravity.CENTER);
        layout.addView(footer, fullWidth(34, 0));

        scrollView.addView(layout);
        setContentView(scrollView);
    }
}