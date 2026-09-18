package io.ugm;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public final class BottomNavigation {
    private BottomNavigation() {
    }

    public static void add(Activity activity, LinearLayout parent, int selected) {
        LinearLayout tabs = new LinearLayout(activity);
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(0, activity.getResources().getDisplayMetrics().densityDpi / 8, 0, 0);
        tabs.setBackgroundColor(Palette.PAPER);

        addTab(activity, tabs, "Timer", selected == 0,
                () -> open(activity, MainActivity.class, selected == 0));
        addTab(activity, tabs, "Calendar", selected == 1,
                () -> open(activity, CalendarActivity.class, selected == 1));
        addTab(activity, tabs, "Camera", selected == 2,
                () -> open(activity, CameraActivity.class, selected == 2));

        parent.addView(tabs, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private static void addTab(Activity activity, LinearLayout parent, String label,
            boolean selected, Runnable action) {
        Button tab = new Button(activity);
        tab.setText(label);
        tab.setTextSize(14);
        tab.setAllCaps(false);
        tab.setTextColor(selected ? Palette.ACCENT : Palette.MUTED);
        tab.setBackgroundColor(Color.TRANSPARENT);
        tab.setElevation(0f);
        tab.setStateListAnimator(null);
        tab.setOnClickListener(v -> action.run());
        parent.addView(tab, new LinearLayout.LayoutParams(0, dp(activity, 52), 1f));
    }

    private static void open(Activity activity, Class<?> target, boolean selected) {
        if (selected) {
            return;
        }
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    private static int dp(Activity activity, float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
