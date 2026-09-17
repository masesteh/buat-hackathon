package io.ugm;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.text.*;
import java.util.*;
import java.nio.charset.*;

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
        private EditText todoEntry;
        private LinearLayout todoList;
        private TextView reminderStatus;
    private Button saveButton;
    private SharedPreferences notesPrefs;

        private static final class TodoItem {
                private String text;
                private boolean checked;

                private TodoItem(String text, boolean checked) {
                        this.text = text;
                        this.checked = checked;
                }
        }

    private int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    private GradientDrawable roundedBackground(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private void styleButton(Button button, int color, int width) {
        button.setTextSize(14);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight(dp(46));
        button.setPadding(dp(10), 0, dp(10), 0);
        button.setBackground(roundedBackground(color, 12));
        if (width > 0) {
            button.setMinWidth(dp(width));
        }
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
                dp(16),
                dp(16),
                dp(16)
        );
        root.setBackgroundColor(Palette.PAPER);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(6));

        monthTitle = new TextView(this);
        monthTitle.setTextColor(Palette.INK);
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
        styleButton(previous, Palette.INK, 44);
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
        styleButton(next, Palette.INK, 44);
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
                dp(4)
        );

        Button monthButton = new Button(this);
        monthButton.setText("Month");
        styleButton(monthButton, Palette.ACCENT, 0);
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
        styleButton(weekButton, Palette.ACCENT, 0);
        weekButton.setOnClickListener(v -> {
            viewMode = VIEW_WEEK;
            renderCalendar();
        });

        Button dayButton = new Button(this);
        dayButton.setText("Day");
        styleButton(dayButton, Palette.ACCENT, 0);
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
            weekday.setTextColor(Palette.MUTED);
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
        selectedDateTitle.setTextColor(Palette.INK);
        selectedDateTitle.setTypeface(Typeface.DEFAULT_BOLD);
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
                        Palette.PAPER,
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

        todoList = new LinearLayout(this);
        todoList.setOrientation(LinearLayout.VERTICAL);
        root.addView(todoList);

        LinearLayout addTodoRow = new LinearLayout(this);
        addTodoRow.setGravity(Gravity.CENTER_VERTICAL);
        addTodoRow.setPadding(dp(12), dp(4), dp(6), dp(4));
        addTodoRow.setBackground(roundedBackground(Color.argb(20, 0, 121, 112), 12));

        todoEntry = new EditText(this);
        todoEntry.setSingleLine(true);
        todoEntry.setHint("Add a task");
        todoEntry.setTextSize(15);
        todoEntry.setBackgroundColor(Color.TRANSPARENT);
        addTodoRow.addView(todoEntry, new LinearLayout.LayoutParams(
                0, dp(50), 1f));

        Button addTodoButton = new Button(this);
        addTodoButton.setText("Add");
        styleButton(addTodoButton, Palette.ACCENT, 0);
        addTodoButton.setOnClickListener(v -> addTodoFromEntry());
        addTodoRow.addView(addTodoButton, new LinearLayout.LayoutParams(
                dp(72), dp(46)));
        root.addView(addTodoRow);

        saveButton = new Button(this);
        saveButton.setText(
                "Save note & to-do"
        );
        styleButton(saveButton, Palette.ACCENT, 0);

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
        styleButton(backButton, Palette.INK, 0);
        backButton.setOnClickListener(
                v -> finish()
        );

        root.addView(backButton);

        TextView reminderTitle = new TextView(this);
        reminderTitle.setText("Reminder");
        reminderTitle.setTextSize(16);
        reminderTitle.setTextColor(Palette.INK);
        reminderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        reminderTitle.setPadding(0, dp(18), 0, dp(6));
        root.addView(reminderTitle);

        LinearLayout reminderRow = new LinearLayout(this);
        reminderRow.setGravity(Gravity.CENTER_VERTICAL);
        reminderStatus = new TextView(this);
        reminderStatus.setTextColor(Palette.MUTED);
        reminderStatus.setTextSize(14);
        reminderRow.addView(reminderStatus, new LinearLayout.LayoutParams(
                0, dp(46), 1f));

        Button reminderButton = new Button(this);
        reminderButton.setText("Set time");
        styleButton(reminderButton, Palette.ACCENT, 0);
        reminderButton.setOnClickListener(v -> chooseReminderTime());
        reminderRow.addView(reminderButton, new LinearLayout.LayoutParams(
                dp(112), dp(46)));

        Button clearReminderButton = new Button(this);
        clearReminderButton.setText("Clear");
        styleButton(clearReminderButton, Palette.INK, 0);
        clearReminderButton.setOnClickListener(v -> clearReminder());
        reminderRow.addView(clearReminderButton, new LinearLayout.LayoutParams(
                dp(84), dp(46)));
        root.addView(reminderRow);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Palette.PAPER);
        scrollView.addView(root);
        setContentView(scrollView);
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

            renderTodos();
            updateReminderStatus();

            return;
        }

        calendarGrid.setVisibility(
                android.view.View.VISIBLE
        );

        noteInput.setText(
                loadNoteForDate(selectedDate)
        );

        renderTodos();
        updateReminderStatus();

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
                                        247,
                                        249,
                                        250
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
        params.height = dp(34);

        params.columnSpec = GridLayout.spec(
                index % DAYS_IN_WEEK,
                1,
                1f
        );

        params.rowSpec = GridLayout.spec(
                index / DAYS_IN_WEEK
        );

        params.setMargins(
                dp(1),
                dp(1),
                dp(1),
                dp(1)
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
                            Palette.ACCENT,
                            0f
                    );

            background.setStroke(
                    dp(3),
                    Palette.MUTED
            );

            cell.setBackground(background);
            cell.setTextColor(Color.WHITE);

        } else if (isSelected) {
            cell.setBackground(
                    roundedBackground(
                            Palette.ACCENT,
                            0f
                    )
            );

            cell.setTextColor(Color.WHITE);

        } else if (isToday) {
            cell.setBackground(
                    roundedBackground(
                            Palette.MUTED,
                            0f
                    )
            );

            cell.setTextColor(
                    Palette.INK
            );

        } else {
            cell.setBackground(
                    roundedBackground(
                            Palette.PAPER,
                            0f
                    )
            );

            cell.setTextColor(
                    Palette.INK
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

        String key = dateKey(selectedDate);

        notesPrefs.edit()
                .putString(
                        key + "_note",
                        note
                )
                .putString(
                        key + "_todo",
                                                serializeTodos()
                )
                .apply();
    }

        private void addTodoFromEntry() {
                String text = todoEntry.getText().toString().trim();
                if (text.isEmpty()) {
                        return;
                }
                addTodoRow(new TodoItem(text, false));
                todoEntry.setText("");
                saveNoteForSelectedDate();
        }

        private void renderTodos() {
                todoList.removeAllViews();
                for (TodoItem item : loadTodoItems(selectedDate)) {
                        addTodoRow(item);
                }
                todoList.setOnDragListener(this::handleTodoDrag);
        }

        private void addTodoRow(TodoItem item) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(6), dp(3), dp(6), dp(3));
                row.setTag(item);
                row.setBackground(roundedBackground(Color.argb(20, 0, 121, 112), 12));

                CheckBox checkbox = new CheckBox(this);
                checkbox.setChecked(item.checked);
                checkbox.setOnCheckedChangeListener((button, checked) -> {
                        item.checked = checked;
                        saveNoteForSelectedDate();
                });
                row.addView(checkbox, new LinearLayout.LayoutParams(dp(48), dp(52)));

                EditText task = new EditText(this);
                task.setText(item.text);
                task.setTextSize(15);
                task.setSingleLine(true);
                task.setTextColor(Palette.INK);
                task.setBackgroundColor(Color.TRANSPARENT);
                task.setOnLongClickListener(v -> startTodoDrag(row));
                row.addView(task, new LinearLayout.LayoutParams(0, dp(52), 1f));

                Button remove = new Button(this);
                remove.setText("×");
                styleButton(remove, Palette.INK, 42);
                remove.setOnClickListener(v -> {
                        todoList.removeView(row);
                        saveNoteForSelectedDate();
                });
                row.addView(remove, new LinearLayout.LayoutParams(dp(48), dp(46)));

                row.setOnLongClickListener(v -> startTodoDrag(row));
                row.setOnDragListener(this::handleTodoDrag);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, dp(58));
                params.setMargins(0, 0, 0, dp(6));
                todoList.addView(row, params);
        }

        private boolean startTodoDrag(View row) {
                ClipData data = ClipData.newPlainText("todo", "todo");
                row.startDragAndDrop(data, new View.DragShadowBuilder(row), row, 0);
                return true;
        }

        private boolean handleTodoDrag(View target, DragEvent event) {
                View dragged = (View) event.getLocalState();
                if (dragged == null || dragged.getParent() != todoList) {
                        return true;
                }

                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED && target != dragged) {
                        target.setAlpha(0.65f);
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                        target.setAlpha(1f);
                } else if (event.getAction() == DragEvent.ACTION_DROP && target != dragged) {
                        int targetIndex = target == todoList
                                        ? todoList.getChildCount()
                                        : todoList.indexOfChild(target);
                        int draggedIndex = todoList.indexOfChild(dragged);
                        todoList.removeView(dragged);
                        if (draggedIndex < targetIndex) {
                                targetIndex--;
                        }
                        todoList.addView(dragged, Math.max(0, targetIndex));
                        target.setAlpha(1f);
                        saveNoteForSelectedDate();
                }
                return true;
        }

        private String serializeTodos() {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < todoList.getChildCount(); i++) {
                        LinearLayout row = (LinearLayout) todoList.getChildAt(i);
                        TodoItem item = (TodoItem) row.getTag();
                        EditText task = (EditText) row.getChildAt(1);
                        item.text = task.getText().toString();
                        if (item.text.trim().isEmpty()) {
                                continue;
                        }
                        if (result.length() > 0) {
                                result.append('\n');
                        }
                        result.append(item.checked ? "1" : "0").append('\t');
                            result.append(android.util.Base64.encodeToString(
                                    item.text.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP));
                }
                return result.toString();
        }

        private List<TodoItem> loadTodoItems(Calendar date) {
                List<TodoItem> items = new ArrayList<>();
                String saved = loadTodoForDate(date);
                if (saved.isEmpty()) {
                        return items;
                }
                for (String line : saved.split("\\n")) {
                        String[] parts = line.split("\\t", 2);
                        if (parts.length != 2) {
                                if (!line.trim().isEmpty()) {
                                        items.add(new TodoItem(line.trim(), false));
                                }
                                continue;
                        }
                        try {
                                String text = new String(android.util.Base64.decode(
                                        parts[1], android.util.Base64.DEFAULT),
                                                StandardCharsets.UTF_8);
                                items.add(new TodoItem(text, "1".equals(parts[0])));
                        } catch (IllegalArgumentException exception) {
                                // Ignore malformed legacy task entries.
                        }
                }
                return items;
        }

        private void chooseReminderTime() {
                Calendar initial = Calendar.getInstance();
                long savedTime = notesPrefs.getLong(reminderKey(selectedDate), 0L);
                if (savedTime > System.currentTimeMillis()) {
                        initial.setTimeInMillis(savedTime);
                } else {
                        initial.set(Calendar.HOUR_OF_DAY, 9);
                        initial.set(Calendar.MINUTE, 0);
                }

                new TimePickerDialog(this, (view, hour, minute) ->
                                scheduleReminder(hour, minute), initial.get(Calendar.HOUR_OF_DAY),
                                initial.get(Calendar.MINUTE), false).show();
        }

        private void scheduleReminder(int hour, int minute) {
                Calendar trigger = (Calendar) selectedDate.clone();
                trigger.set(Calendar.HOUR_OF_DAY, hour);
                trigger.set(Calendar.MINUTE, minute);
                trigger.set(Calendar.SECOND, 0);
                trigger.set(Calendar.MILLISECOND, 0);
                if (trigger.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(this, "Choose a future time", Toast.LENGTH_SHORT).show();
                        return;
                }

                requestNotificationPermission();
                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(),
                                reminderPendingIntent(true));
                notesPrefs.edit().putLong(reminderKey(selectedDate), trigger.getTimeInMillis()).apply();
                updateReminderStatus();
                Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show();
        }

        private void clearReminder() {
                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.cancel(reminderPendingIntent(false));
                notesPrefs.edit().remove(reminderKey(selectedDate)).apply();
                updateReminderStatus();
        }

        private PendingIntent reminderPendingIntent(boolean includeExtras) {
                String key = dateKey(selectedDate);
                Intent intent = new Intent(this, ReminderReceiver.class);
                if (includeExtras) {
                        intent.putExtra("date", dateLabel(selectedDate));
                        intent.putExtra("title", "Calendar reminder");
                }
                return PendingIntent.getBroadcast(this, key.hashCode(), intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        private String reminderKey(Calendar date) {
                return dateKey(date) + "_reminder_time";
        }

        private void updateReminderStatus() {
                long savedTime = notesPrefs.getLong(reminderKey(selectedDate), 0L);
                if (savedTime <= System.currentTimeMillis()) {
                        reminderStatus.setText("No reminder set");
                        return;
                }
                reminderStatus.setText("Set for " + DateFormat.getTimeInstance(DateFormat.SHORT,
                                Locale.getDefault()).format(new Date(savedTime)));
        }

        private void requestNotificationPermission() {
                if (Build.VERSION.SDK_INT >= 33
                                && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                                != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 42);
                }
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