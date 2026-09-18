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
import java.util.*;
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

        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setSingleLine(true);
        root.addView(nameInput, margins(0, 0, 0, 8));

        EditText ageInput = new EditText(this);
        ageInput.setHint("Age");
        ageInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        ageInput.setSingleLine(true);
        root.addView(ageInput, margins(0, 0, 0, 8));

        Button submitButton = button("Submit user", Palette.INK);
        submitButton.setOnClickListener(v -> postUser(nameInput, ageInput, submitButton));
        root.addView(submitButton, margins(0, 0, 0, 12));

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

    private void postUser(EditText nameInput, EditText ageInput, Button submitButton) {
        String name = nameInput.getText().toString().trim();
        String age = ageInput.getText().toString().trim();
        if (name.isEmpty() || age.isEmpty()) {
            result.setText("Name and age are required.");
            return;
        }

        submitButton.setEnabled(false);
        result.setText("Submitting...");
        executor.execute(() -> {
            String response;
            try {
                response = postUserRequest(nextUserId(), name, Integer.parseInt(age));
            } catch (Exception exception) {
                response = "Request failed:\n" + exception.getMessage();
            }
            String finalResponse = response;
            runOnUiThread(() -> {
                result.setText(finalResponse);
                submitButton.setEnabled(true);
            });
        });
    }
    private int nextUserId() throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
    
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
    
        int status = connection.getResponseCode();
    
        InputStream stream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
    
        String body = read(stream);
        connection.disconnect();
    
        if (status >= 400) {
            throw new IOException("GET failed: HTTP " + status + "\n" + body);
        }
    
        JSONArray users = usersArray(body);
    
        int maximumId = 0;
    
        if (users != null) {
            for (int index = 0; index < users.length(); index++) {
                JSONObject user = users.optJSONObject(index);
    
                if (user != null) {
                    maximumId = Math.max(
                            maximumId,
                            user.optInt("id", 0)
                    );
                }
            }
        }
    
        return maximumId + 1;
    }
    
    private String postUserRequest(int id, String name, int age) throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(API_URL).openConnection();
    
        connection.setRequestMethod("POST");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
    
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
    
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("age", age);
    
        try (OutputStream output = connection.getOutputStream()) {
            output.write(
                    jsonObject.toString().getBytes(StandardCharsets.UTF_8)
            );
        }
    
        int status = connection.getResponseCode();
    
        InputStream stream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
    
        String body = read(stream);
    
        connection.disconnect();
    
        return "HTTP " + status + "\n\n" + body;
    }
    
    private String formatUsers(String body) {
        try {
            JSONArray users = usersArray(body);
    
            if (users == null) {
                return "No users array found in response.";
            }
    
            StringBuilder text = new StringBuilder();
    
            for (int index = 0; index < users.length(); index++) {
                JSONObject user = users.optJSONObject(index);
    
                if (user == null) {
                    continue;
                }
    
                text.append("id: ")
                        .append(user.opt("id"))
                        .append("\n");
    
                text.append("name: ")
                        .append(user.optString("name", ""))
                        .append("\n");
    
                text.append("age: ")
                        .append(user.opt("age"))
                        .append("\n\n");
            }
    
            return text.length() == 0
                    ? "No users found."
                    : text.toString().trim();
    
        } catch (JSONException exception) {
            return "Invalid JSON response:\n" + body;
        }
    }
    
    private JSONArray usersArray(String body) throws JSONException {
        body = body.trim();
    
        if (body.startsWith("[")) {
            return new JSONArray(body);
        }
    
        JSONObject response = new JSONObject(body);
    
        JSONArray users = response.optJSONArray("users");
    
        if (users != null) {
            return users;
        }
    
        return response.optJSONArray("data");
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
