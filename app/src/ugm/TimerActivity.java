package io.ugm;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class TimerActivity extends Activity {
    private static final String SETTINGS = "pomodoro_settings";
    private static final int DEFAULT_STUDY = 25;
    private static final int DEFAULT_BREAK = 5;
    private static final int DEFAULT_LONG_BREAK = 15;
    private static final int DEFAULT_INTERVAL = 3;

    private SharedPreferences preferences;
    private TextView phaseLabel;
    private TextView timerLabel;
    private TextView sessionLabel;
    private Button startButton;
    private CountDownTimer countdown;
    private long remainingMillis;
    private int sessionNumber = 1;
    private boolean studying = true;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        remainingMillis = durationForCurrentPhase();
        buildView();
    }

    private void buildView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(dp(24), dp(28), dp(24), dp(88));
        layout.setBackgroundColor(Palette.PAPER);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("FOCUS SESSION");
        eyebrow.setTextColor(Palette.ACCENT);
        eyebrow.setTextSize(12);
        eyebrow.setTypeface(Typeface.DEFAULT_BOLD);
        eyebrow.setLetterSpacing(0.12f);
        layout.addView(eyebrow, marginParams(0, 0, 0, 14));

        LinearLayout timerCard = new LinearLayout(this);
        timerCard.setOrientation(LinearLayout.VERTICAL);
        timerCard.setGravity(Gravity.CENTER_HORIZONTAL);
        timerCard.setPadding(dp(20), dp(24), dp(20), dp(24));
        timerCard.setBackground(roundedBackground(Color.argb(30, 0, 121, 112), 20));

        phaseLabel = new TextView(this);
        phaseLabel.setTextSize(18);
        phaseLabel.setTextColor(Palette.ACCENT);
        phaseLabel.setTypeface(Typeface.DEFAULT_BOLD);
        phaseLabel.setGravity(Gravity.CENTER);

        timerLabel = new TextView(this);
        timerLabel.setTextSize(56);
        timerLabel.setTextColor(Palette.INK);
        timerLabel.setTypeface(Typeface.DEFAULT_BOLD);
        timerLabel.setGravity(Gravity.CENTER);

        sessionLabel = new TextView(this);
        sessionLabel.setTextSize(15);
        sessionLabel.setTextColor(Palette.MUTED);
        sessionLabel.setGravity(Gravity.CENTER);

        startButton = new Button(this);
        styleButton(startButton, Palette.ACCENT);
        startButton.setOnClickListener(v -> toggleTimer());

        Button settingsButton = new Button(this);
        settingsButton.setText("Settings");
        styleButton(settingsButton, Palette.INK);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        Button resetButton = new Button(this);
        resetButton.setText("Reset session");
        styleButton(resetButton, Palette.MUTED);
        resetButton.setOnClickListener(v -> resetTimer());

        Button backButton = new Button(this);
        backButton.setText("Back");
        styleButton(backButton, Palette.INK);
        backButton.setOnClickListener(v -> finish());

        timerCard.addView(phaseLabel, marginParams(0, 0, 0, 8));
        timerCard.addView(timerLabel, marginParams(0, 0, 0, 4));
        timerCard.addView(sessionLabel, matchParentWrapContent());
        layout.addView(timerCard, marginParams(0, 0, 0, 18));
        layout.addView(startButton, marginParams(0, 0, 0, 8));
        layout.addView(settingsButton, marginParams(0, 0, 0, 8));
        layout.addView(resetButton, marginParams(0, 0, 0, 8));
        layout.addView(backButton, matchParentWrapContent());

        Button apiButton = new Button(this);
        apiButton.setText("Test API");
        styleButton(apiButton, Palette.MUTED);
        apiButton.setOnClickListener(v ->
            startActivity(new Intent(this, ApiTestActivity.class)));
        layout.addView(apiButton, marginParams(0, 8, 0, 0));
        setContentView(BottomNavigation.attach(this, layout, 0));
        updateLabels();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(int color, float radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(radius));
        return background;
    }

    private void styleButton(Button button, int color) {
        button.setTextSize(15);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight(dp(50));
        button.setPadding(dp(16), 0, dp(16), 0);
        button.setBackground(roundedBackground(color, 14));
    }

    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = matchParentWrapContent();
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams matchParentWrapContent() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void toggleTimer() {
        if (running) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        running = true;
        startButton.setText("Pause");
        countdown = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateLabels();
            }

            @Override
            public void onFinish() {
                remainingMillis = 0;
                running = false;
                advancePhase();
            }
        }.start();
    }

    private void stopTimer() {
        if (countdown != null) {
            countdown.cancel();
        }
        running = false;
        startButton.setText("Start");
    }

    private void resetTimer() {
        stopTimer();
        studying = true;
        sessionNumber = 1;
        remainingMillis = durationForCurrentPhase();
        updateLabels();
    }

    private void advancePhase() {
        if (studying) {
            studying = false;
        } else {
            studying = true;
            sessionNumber++;
        }
        remainingMillis = durationForCurrentPhase();
        updateLabels();
        if (preferences.getBoolean("auto_" + (studying ? "study" : "break"), false)) {
            startTimer();
        }
    }

    private long durationForCurrentPhase() {
        int minutes;
        if (studying) {
            minutes = preferences.getInt("study", DEFAULT_STUDY);
        } else {
            int interval = preferences.getInt("interval", DEFAULT_INTERVAL);
            boolean longBreak = sessionNumber % interval == 0;
            minutes = preferences.getInt(longBreak ? "long_break" : "break", longBreak
                    ? DEFAULT_LONG_BREAK : DEFAULT_BREAK);
        }
        return minutes * 60_000L;
    }

    private void updateLabels() {
        if (phaseLabel == null) {
            return;
        }
        boolean longBreak = !studying && sessionNumber
                % preferences.getInt("interval", DEFAULT_INTERVAL) == 0;
        phaseLabel.setText(studying ? "Focus" : (longBreak ? "Long break" : "Break"));
        timerLabel.setText(String.format(Locale.getDefault(), "%02d:%02d",
                remainingMillis / 60_000, (remainingMillis / 1_000) % 60));
        sessionLabel.setText(String.format(Locale.getDefault(), "Session %d", sessionNumber));
        startButton.setText(running ? "Pause" : "Start");
    }

    private void showSettingsDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), 0, dp(24), 0);

        EditText study = minutesField("Study time (minutes)", "study", DEFAULT_STUDY);
        EditText shortBreak = minutesField("Break time (minutes)", "break", DEFAULT_BREAK);
        EditText longBreak = minutesField("Long break time (minutes)", "long_break", DEFAULT_LONG_BREAK);
        EditText interval = minutesField("Long break interval (sessions)", "interval", DEFAULT_INTERVAL);
        Switch autoStudy = autoSwitch("Auto-start Pomodoro", "auto_study");
        Switch autoBreak = autoSwitch("Auto-start breaks", "auto_break");
        addSetting(form, "Study time (minutes)", study);
        addSetting(form, "Break time (minutes)", shortBreak);
        addSetting(form, "Long break time (minutes)", longBreak);
        addSetting(form, "Long break interval (sessions)", interval);
        form.addView(autoStudy);
        form.addView(autoBreak);

        new AlertDialog.Builder(this)
                .setTitle("Timer settings")
                .setView(form)
                .setPositiveButton("Save", (dialog, which) -> saveSettings(
                        study, shortBreak, longBreak, interval, autoStudy, autoBreak))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addSetting(LinearLayout form, String label, EditText field) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        form.addView(labelView);
        form.addView(field);
    }

    private EditText minutesField(String label, String key, int fallback) {
        EditText field = new EditText(this);
        field.setHint(label);
        field.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        field.setText(String.valueOf(preferences.getInt(key, fallback)));
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                try {
                    int value = Integer.parseInt(text.toString());
                    if (value > 0) {
                        preferences.edit().putInt(key, value).apply();
                        refreshCurrentDuration();
                    }
                } catch (NumberFormatException exception) {
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
        return field;
    }

    private Switch autoSwitch(String label, String key) {
        Switch toggle = new Switch(this);
        toggle.setText(label);
        toggle.setChecked(preferences.getBoolean(key, false));
        toggle.setOnCheckedChangeListener((buttonView, checked) ->
            preferences.edit().putBoolean(key, checked).apply());
        return toggle;
    }

    private void saveSettings(EditText study, EditText shortBreak, EditText longBreak,
            EditText interval, Switch autoStudy, Switch autoBreak) {
        preferences.edit()
                .putInt("study", positiveValue(study, DEFAULT_STUDY))
                .putInt("break", positiveValue(shortBreak, DEFAULT_BREAK))
                .putInt("long_break", positiveValue(longBreak, DEFAULT_LONG_BREAK))
                .putInt("interval", positiveValue(interval, DEFAULT_INTERVAL))
                .putBoolean("auto_study", autoStudy.isChecked())
                .putBoolean("auto_break", autoBreak.isChecked())
                .apply();
        refreshCurrentDuration();
    }

    private void refreshCurrentDuration() {
        if (!running) {
            remainingMillis = durationForCurrentPhase();
            updateLabels();
        }
    }

    private int positiveValue(EditText field, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(field.getText().toString()));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    @Override
    protected void onDestroy() {
        if (countdown != null) {
            countdown.cancel();
        }
        super.onDestroy();
    }
}