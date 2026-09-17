package io.ugm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class CalendarActivity extends Activity {

    private static final int DAYS_IN_WEEK = 7;
    private static final int VIEW_MONTH = 0;
    private static final int VIEW_WEEK = 1;
    private static final int VIEW_DAY = 2;
    private static final String PREFS_NAME = "calendar_notes";

    private final Calendar displayedMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar today = Calendar.getInstance();

    private int viewMode = VIEW_MONTH;
    private GridLayout calendarGrid;
    private TextView monthTitle;
    private TextView selectedDateTitle;
    private EditText noteInput;
    private EditText todoInput;
    private Button saveButton;
    private SharedPreferences notesPrefs;

    private int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    private GradientDrawable roundedBackground(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notesPrefs = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate.setTimeInMillis(today.getTimeInMillis());

        buildInterface();
        renderCalendar();
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(16),
                dp(18),
                dp(16),
                dp(16)
        );
        root.setBackgroundColor(
                Color.rgb(247, 249, 250)
        );

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(12));

        monthTitle = new TextView(this);
        monthTitle.setTextColor(
                Color.rgb(34, 40, 49)
        );
        monthTitle.setTextSize(22);
        monthTitle.setTypeface(
                Typeface.DEFAULT_BOLD
        );
        monthTitle.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1f
                )
        );

        Button previous = new Button(this);
        previous.setText("<");
        previous.setOnClickListener(v -> {
            if (viewMode == VIEW_DAY) {
                selectedDate.add(
                        Calendar.DAY_OF_MONTH,
                        -1
                );
            } else if (viewMode == VIEW_WEEK) {
                selectedDate.add(
                        Calendar.WEEK_OF_YEAR,
                        -1
                );
            } else {
                displayedMonth.add(
                        Calendar.MONTH,
                        -1
                );
            }

            renderCalendar();
        });

        Button next = new Button(this);
        next.setText(">");
        next.setOnClickListener(v -> {
            if (viewMode == VIEW_DAY) {
                selectedDate.add(
                        Calendar.DAY_OF_MONTH,
                        1
                );
            } else if (viewMode == VIEW_WEEK) {
                selectedDate.add(
                        Calendar.WEEK_OF_YEAR,
                        1
                );
            } else {
                displayedMonth.add(
                        Calendar.MONTH,
                        1
                );
            }

            renderCalendar();
        });

        header.addView(monthTitle);
        header.addView(previous);
        header.addView(next);

        root.addView(header);

        LinearLayout modeBar = new LinearLayout(this);
        modeBar.setOrientation(
                LinearLayout.HORIZONTAL
        );
        modeBar.setGravity(Gravity.CENTER);
        modeBar.setPadding(
                0,
                0,
                0,
                dp(10)
        );

        Button monthButton = new Button(this);
        monthButton.setText("Month");
        monthButton.setOnClickListener(v -> {
            viewMode = VIEW_MONTH;

            displayedMonth.setTimeInMillis(
                    selectedDate.getTimeInMillis()
            );

            displayedMonth.set(
                    Calendar.DAY_OF_MONTH,
                    1
            );

            renderCalendar();
        });

        Button weekButton = new Button(this);
        weekButton.setText("Week");
        weekButton.setOnClickListener(v -> {
            viewMode = VIEW_WEEK;
            renderCalendar();
        });

        Button dayButton = new Button(this);
        dayButton.setText("Day");
        dayButton.setOnClickListener(v -> {
            viewMode = VIEW_DAY;
            renderCalendar();
        });

        modeBar.addView(monthButton);
        modeBar.addView(weekButton);
        modeBar.addView(dayButton);

        root.addView(modeBar);

        LinearLayout weekdayRow = new LinearLayout(this);
        weekdayRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] weekDays = {
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat",
                "Sun"
        };

        for (String dayName : weekDays) {
            TextView weekday = new TextView(this);
            weekday.setText(dayName);
            weekday.setGravity(Gravity.CENTER);
            weekday.setTextColor(
                    Color.rgb(95, 98, 103)
            );
            weekday.setTextSize(12);
            weekday.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            -2,
                            1f
                    )
            );

            weekdayRow.addView(weekday);
        }

        root.addView(weekdayRow);

        calendarGrid = new GridLayout(this);
        calendarGrid.setColumnCount(DAYS_IN_WEEK);
        calendarGrid.setUseDefaultMargins(false);

        root.addView(
                calendarGrid,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        selectedDateTitle = new TextView(this);
        selectedDateTitle.setTextSize(18);
        selectedDateTitle.setTextColor(
                Color.rgb(31, 41, 55)
        );
        selectedDateTitle.setPadding(
                0,
                dp(12),
                0,
                dp(8)
        );

        root.addView(selectedDateTitle);

        noteInput = new EditText(this);
        noteInput.setHint(
                "Plain text note for this day"
        );
        noteInput.setMinLines(4);
        noteInput.setGravity(
                Gravity.TOP | Gravity.START
        );
        noteInput.setBackground(
                roundedBackground(
                        Color.WHITE,
                        12f
                )
        );
        noteInput.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        root.addView(noteInput);

        TextView todoTitle = new TextView(this);
        todoTitle.setText(
                "To-do list for this day"
        );
        todoTitle.setTextSize(16);
        todoTitle.setTypeface(
                Typeface.DEFAULT_BOLD
        );
        todoTitle.setPadding(
                0,
                dp(12),
                0,
                dp(8)
        );

        root.addView(todoTitle);

        todoInput = new EditText(this);
        todoInput.setHint("..."
        );
        todoInput.setMinLines(5);
        todoInput.setGravity(
                Gravity.TOP | Gravity.START
        );
        todoInput.setBackground(
                roundedBackground(
                        Color.WHITE,
                        12f
                )
        );
        todoInput.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        root.addView(todoInput);

        saveButton = new Button(this);
        saveButton.setText(
                "Save note & to-do"
        );

        saveButton.setOnClickListener(v -> {
            saveNoteForSelectedDate();

            Toast.makeText(
                    this,
                    "Saved for " +
                            dateLabel(selectedDate),
                    Toast.LENGTH_SHORT
            ).show();
        });

        root.addView(saveButton);

        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(
                v -> finish()
        );

        root.addView(backButton);

        setContentView(root);
    }

    private void renderCalendar() {
        monthTitle.setText(getHeaderTitle());

        selectedDateTitle.setText(
                "Selected date: " +
                        DateFormat.getDateInstance(
                                DateFormat.FULL,
                                Locale.getDefault()
                        ).format(
                                selectedDate.getTime()
                        )
        );

        calendarGrid.removeAllViews();

        if (viewMode == VIEW_DAY) {
            calendarGrid.setVisibility(
                    android.view.View.GONE
            );

            noteInput.setText(
                    loadNoteForDate(selectedDate)
            );

            todoInput.setText(
                    loadTodoForDate(selectedDate)
            );

            return;
        }

        calendarGrid.setVisibility(
                android.view.View.VISIBLE
        );

        noteInput.setText(
                loadNoteForDate(selectedDate)
        );

        todoInput.setText(
                loadTodoForDate(selectedDate)
        );

        if (viewMode == VIEW_WEEK) {
            calendarGrid.setRowCount(1);

            Calendar startOfWeek =
                    (Calendar) selectedDate.clone();

            int dayOfWeek =
                    startOfWeek.get(
                            Calendar.DAY_OF_WEEK
                    );

            int diffToMonday =
                    (dayOfWeek + 5) % DAYS_IN_WEEK;

            startOfWeek.add(
                    Calendar.DAY_OF_MONTH,
                    -diffToMonday
            );

            for (int i = 0; i < DAYS_IN_WEEK; i++) {
                Calendar cellDate =
                        (Calendar) startOfWeek.clone();

                cellDate.add(
                        Calendar.DAY_OF_MONTH,
                        i
                );

                Button cell = new Button(this);

                cell.setLayoutParams(
                        createCellLayoutParams(i)
                );

                cell.setText(
                        String.valueOf(
                                cellDate.get(
                                        Calendar.DAY_OF_MONTH
                                )
                        )
                );

                styleCalendarCell(
                        cell,
                        cellDate,
                        false
                );

                cell.setOnClickListener(v -> {
                    selectedDate.setTimeInMillis(
                            cellDate.getTimeInMillis()
                    );

                    renderCalendar();
                });

                calendarGrid.addView(cell);
            }

            return;
        }

        // month view
        Calendar cursor =
                (Calendar) displayedMonth.clone();

        cursor.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        int firstDayIndex =
                (cursor.get(Calendar.DAY_OF_WEEK) + 5)
                        % DAYS_IN_WEEK;

        int daysInMonth =
                displayedMonth.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );

        int rowCount =
                (int) Math.ceil(
                        (daysInMonth + firstDayIndex)
                                / (double) DAYS_IN_WEEK
                );

        calendarGrid.setRowCount(rowCount);

        // create exactly enough cells for the rows
        int cellCount = rowCount * DAYS_IN_WEEK;

        for (int index = 0;
             index < cellCount;
             index++) {

            final int day =
                    index - firstDayIndex + 1;

            Button cell = new Button(this);

            cell.setLayoutParams(
                    createCellLayoutParams(index)
            );

            cell.setPadding(0, 0, 0, 0);
            cell.setMinWidth(0);
            cell.setMinimumWidth(0);

            if (day > 0 && day <= daysInMonth) {
                final Calendar cellDate =
                        (Calendar) displayedMonth.clone();

                cellDate.set(
                        Calendar.DAY_OF_MONTH,
                        day
                );

                cell.setText(
                        String.valueOf(day)
                );

                styleCalendarCell(
                        cell,
                        cellDate,
                        true
                );

                cell.setOnClickListener(v -> {
                    selectedDate.setTimeInMillis(
                            cellDate.getTimeInMillis()
                    );

                    renderCalendar();
                });
            } else {
                cell.setText("");
                cell.setEnabled(false);
                cell.setAlpha(0.35f);

                cell.setBackground(
                        roundedBackground(
                                Color.argb(
                                        255,
                                        245,
                                        245,
                                        245
                                ),
                                10f
                        )
                );
            }

            calendarGrid.addView(cell);
        }
    }

    private String getHeaderTitle() {
        if (viewMode == VIEW_DAY) {
            return DateFormat.getDateInstance(
                    DateFormat.LONG,
                    Locale.getDefault()
            ).format(selectedDate.getTime());
        }

        if (viewMode == VIEW_WEEK) {
            Calendar startOfWeek =
                    (Calendar) selectedDate.clone();

            int dayOfWeek =
                    startOfWeek.get(
                            Calendar.DAY_OF_WEEK
                    );

            int diffToMonday =
                    (dayOfWeek + 5) % DAYS_IN_WEEK;

            startOfWeek.add(
                    Calendar.DAY_OF_MONTH,
                    -diffToMonday
            );

            Calendar endOfWeek =
                    (Calendar) startOfWeek.clone();

            endOfWeek.add(
                    Calendar.DAY_OF_MONTH,
                    6
            );

            return DateFormat.getDateInstance(
                    DateFormat.MEDIUM,
                    Locale.getDefault()
            ).format(startOfWeek.getTime())
                    + " - " +
                    DateFormat.getDateInstance(
                            DateFormat.MEDIUM,
                            Locale.getDefault()
                    ).format(endOfWeek.getTime());
        }

        return DateFormat.getDateInstance(
                DateFormat.LONG,
                Locale.getDefault()
        ).format(displayedMonth.getTime())
                .replaceAll(" 1,?", "");
    }

    private GridLayout.LayoutParams createCellLayoutParams(
            int index
    ) {
        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

        params.width = 0;
        params.height = dp(44);

        params.columnSpec = GridLayout.spec(
                index % DAYS_IN_WEEK,
                1,
                1f
        );

        params.rowSpec = GridLayout.spec(
                index / DAYS_IN_WEEK
        );

        params.setMargins(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        return params;
    }

    private void styleCalendarCell(
            Button cell,
            Calendar cellDate,
            boolean includeMonthContext
    ) {
        boolean isToday =
                sameDate(cellDate, today);

        boolean isSelected =
                sameDate(cellDate, selectedDate);

        boolean hasNote =
                !TextUtils.isEmpty(
                        loadNoteForDate(cellDate)
                );

        if (isSelected && isToday) {
            GradientDrawable background =
                    roundedBackground(
                            Color.rgb(84, 162, 154),
                            10f
                    );

            background.setStroke(
                    dp(3),
                    Color.rgb(255, 175, 84)
            );

            cell.setBackground(background);
            cell.setTextColor(Color.WHITE);

        } else if (isSelected) {
            cell.setBackground(
                    roundedBackground(
                            Color.rgb(84, 162, 154),
                            10f
                    )
            );

            cell.setTextColor(Color.WHITE);

        } else if (isToday) {
            cell.setBackground(
                    roundedBackground(
                            Color.rgb(255, 183, 96),
                            10f
                    )
            );

            cell.setTextColor(
                    Color.rgb(30, 30, 30)
            );

        } else {
            cell.setBackground(
                    roundedBackground(
                            Color.WHITE,
                            10f
                    )
            );

            cell.setTextColor(
                    Color.rgb(45, 52, 60)
            );
        }

        if (hasNote && !isSelected && !isToday) {
            cell.setText(
                    cell.getText() + " •"
            );
        }

        if (!includeMonthContext &&
                cellDate.get(Calendar.MONTH)
                        != displayedMonth.get(Calendar.MONTH)) {

            cell.setAlpha(0.5f);
        }
    }

    private boolean sameDate(
            Calendar first,
            Calendar second
    ) {
        return first.get(Calendar.YEAR)
                == second.get(Calendar.YEAR)

                && first.get(Calendar.MONTH)
                == second.get(Calendar.MONTH)

                && first.get(Calendar.DAY_OF_MONTH)
                == second.get(Calendar.DAY_OF_MONTH);
    }

    private void saveNoteForSelectedDate() {
        String note =
                noteInput.getText() == null
                        ? ""
                        : noteInput.getText().toString();

        String todo =
                todoInput.getText() == null
                        ? ""
                        : todoInput.getText().toString();

        String key = dateKey(selectedDate);

        notesPrefs.edit()
                .putString(
                        key + "_note",
                        note
                )
                .putString(
                        key + "_todo",
                        todo
                )
                .apply();
    }

    private String loadNoteForDate(
            Calendar date
    ) {
        return notesPrefs.getString(
                dateKey(date) + "_note",
                ""
        );
    }

    private String loadTodoForDate(
            Calendar date
    ) {
        return notesPrefs.getString(
                dateKey(date) + "_todo",
                ""
        );
    }

    private String dateKey(Calendar date) {
        return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH)
        );
    }

    private String dateLabel(Calendar date) {
        return DateFormat.getDateInstance(
                DateFormat.MEDIUM,
                Locale.getDefault()
        ).format(date.getTime());
    }
}