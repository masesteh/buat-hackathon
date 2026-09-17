package io.ugm;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class TimerActivity extends Activity {
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
        layout.setPadding(40, 48, 40, 40);

        phaseLabel = new TextView(this);
        phaseLabel.setTextSize(24);
        phaseLabel.setGravity(Gravity.CENTER);

        timerLabel = new TextView(this);
        timerLabel.setTextSize(56);
        timerLabel.setGravity(Gravity.CENTER);

        sessionLabel = new TextView(this);
        sessionLabel.setTextSize(18);
        sessionLabel.setGravity(Gravity.CENTER);

        startButton = new Button(this);
        startButton.setOnClickListener(v -> toggleTimer());

        Button settingsButton = new Button(this);
        settingsButton.setText("Settings");
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        Button resetButton = new Button(this);
        resetButton.setText("Reset session");
        resetButton.setOnClickListener(v -> resetTimer());

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(v -> finish());

        layout.addView(phaseLabel, matchParentWrapContent());
        layout.addView(timerLabel, matchParentWrapContent());
        layout.addView(sessionLabel, matchParentWrapContent());
        layout.addView(startButton, matchParentWrapContent());
        layout.addView(settingsButton, matchParentWrapContent());
        layout.addView(resetButton, matchParentWrapContent());
        layout.addView(backButton, matchParentWrapContent());
        setContentView(layout);
        updateLabels();
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
        form.setPadding(48, 0, 48, 0);

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