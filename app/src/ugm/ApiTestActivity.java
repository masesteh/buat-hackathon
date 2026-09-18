package io.ugm;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.concurrent.*;

public final class ApiTestActivity extends Activity {
    private static final String API_URL = "https://zvkt-api.vercel.app/users";
    private static final String API_KEY = "my-super-secret-key";

    private TextView result;
    private Button testButton;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        buildView();
    }

    private void buildView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(20));
        root.setBackgroundColor(Palette.PAPER);

        TextView title = new TextView(this);
        title.setText("API test");
        title.setTextSize(24);
        title.setTextColor(Palette.INK);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, margins(0, 0, 0, 8));

        TextView endpoint = new TextView(this);
        endpoint.setText(API_URL);
        endpoint.setTextSize(14);
        endpoint.setTextColor(Palette.MUTED);
        root.addView(endpoint, margins(0, 0, 0, 18));

        testButton = button("Test users endpoint", Palette.ACCENT);
        testButton.setOnClickListener(v -> testApi());
        root.addView(testButton, margins(0, 0, 0, 12));

        result = new TextView(this);
        result.setText("Press the button to make the request.");
        result.setTextSize(14);
        result.setTextColor(Palette.INK);
        result.setTypeface(Typeface.MONOSPACE);
        result.setGravity(Gravity.TOP | Gravity.START);
        result.setPadding(dp(14), dp(14), dp(14), dp(14));
        result.setBackgroundColor(Color.argb(20, 0, 121, 112));

        ScrollView output = new ScrollView(this);
        output.addView(result);
        root.addView(output, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        Button back = button("Back", Palette.INK);
        back.setOnClickListener(v -> finish());
        root.addView(back, margins(0, 14, 0, 0));
        setContentView(root);
    }

    private void testApi() {
        testButton.setEnabled(false);
        result.setText("Loading...");
        executor.execute(() -> {
            String response;
            try {
                response = requestUsers();
            } catch (Exception exception) {
                response = "Request failed:\n" + exception.getMessage();
            }
            String finalResponse = response;
            runOnUiThread(() -> {
                result.setText(finalResponse);
                testButton.setEnabled(true);
            });
        });
    }

    private String requestUsers() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        int status = connection.getResponseCode();
        InputStream stream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        String body = read(stream);
        connection.disconnect();
        return "HTTP " + status + "\n\n" + formatUsers(body);
    }

    private String formatUsers(String body) {
        try {
            JSONArray users;
            if (body.trim().startsWith("[")) {
                users = new JSONArray(body);
            } else {
                JSONObject response = new JSONObject(body);
                users = response.optJSONArray("users");
                if (users == null) {
                    users = response.optJSONArray("data");
                }
            }

            if (users == null) {
                return "No users array found in response.";
            }

            StringBuilder text = new StringBuilder();
            for (int index = 0; index < users.length(); index++) {
                JSONObject user = users.optJSONObject(index);
                if (user == null) {
                    continue;
                }
                text.append("id: ").append(user.opt("id"))
                        .append("\nname: ").append(user.optString("name", ""))
                        .append("\nage: ").append(user.opt("age"))
                        .append("\n\n");
            }
            return text.length() == 0 ? "No users found." : text.toString().trim();
        } catch (JSONException exception) {
            return "Invalid JSON response:\n" + body;
        }
    }

    private String read(InputStream stream) throws IOException {
        if (stream == null) {
            return "(empty response)";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line).append('\n');
            }
            return body.toString().trim();
        }
    }

    private Button button(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight(dp(50));
        button.setBackgroundColor(color);
        return button;
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
