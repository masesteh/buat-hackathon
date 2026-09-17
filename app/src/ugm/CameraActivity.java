package io.ugm;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.camera2.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import org.tensorflow.lite.*;

public final class CameraActivity extends Activity {
    private static final int CAMERA_REQUEST = 100;
    private static final int INPUT_SIZE = 640;
    private static final String MODEL_NAME = "yolov8.tflite";

    private TextureView preview;
    private DetectionOverlay overlay;
    private TextView status;
    private CameraDevice camera;
    private CameraCaptureSession captureSession;
    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private ExecutorService inferenceExecutor;
    private Interpreter interpreter;
    private boolean processing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Palette.PAPER);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        buildView();
        loadModel();
    }

    private void buildView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(20));
        root.setBackgroundColor(Palette.PAPER);

        TextView title = new TextView(this);
        title.setText("Object camera");
        title.setTextSize(24);
        title.setTextColor(Palette.INK);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, margins(0, 0, 0, 6));

        status = new TextView(this);
        status.setText("Loading yolov8.pt model...");
        status.setTextSize(14);
        status.setTextColor(Palette.MUTED);
        status.setGravity(Gravity.CENTER);
        root.addView(status, margins(0, 0, 0, 14));

        FrameLayout cameraFrame = new FrameLayout(this);
        cameraFrame.setBackgroundColor(Color.BLACK);
        preview = new TextureView(this);
        preview.setSurfaceTextureListener(textureListener);
        cameraFrame.addView(preview, new FrameLayout.LayoutParams(dp(300), dp(300), Gravity.CENTER));
        overlay = new DetectionOverlay(this);
        cameraFrame.addView(overlay, new FrameLayout.LayoutParams(dp(300), dp(300), Gravity.CENTER));
        root.addView(cameraFrame, new LinearLayout.LayoutParams(dp(300), dp(300)));

        Button back = button("Back to main", Palette.INK);
        back.setOnClickListener(v -> finish());
        root.addView(back, margins(0, 18, 0, 0));
        setContentView(root);
    }

    private void loadModel() {
        inferenceExecutor = Executors.newSingleThreadExecutor();
        try {
            interpreter = new Interpreter(loadAsset(MODEL_NAME));
            status.setText("Point the camera at an object");
        } catch (IOException | RuntimeException exception) {
            status.setText("Add assets/" + MODEL_NAME + " to enable detection");
        }
    }

    private ByteBuffer loadAsset(String name) throws IOException {
        AssetFileDescriptor descriptor = getAssets().openFd(name);
        FileInputStream input = new FileInputStream(descriptor.getFileDescriptor());
        FileChannel channel = input.getChannel();
        ByteBuffer model = channel.map(FileChannel.MapMode.READ_ONLY,
                descriptor.getStartOffset(), descriptor.getDeclaredLength());
        input.close();
        return model;
    }

    private void requestCamera() {
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission("android.permission.CAMERA")
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.CAMERA"}, CAMERA_REQUEST);
            return;
        }
        openCamera();
    }

    private void openCamera() {
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0];
            manager.openCamera(cameraId, cameraStateCallback, cameraHandler);
        } catch (CameraAccessException | SecurityException exception) {
            status.setText("Camera unavailable");
        }
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("camera");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private final TextureView.SurfaceTextureListener textureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    requestCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    if (interpreter != null && !processing) {
                        processing = true;
                        Bitmap frame = preview.getBitmap(INPUT_SIZE, INPUT_SIZE);
                        inferenceExecutor.execute(() -> detect(frame));
                    }
                }
            };

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice openedCamera) {
            camera = openedCamera;
            createPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice disconnectedCamera) {
            disconnectedCamera.close();
            camera = null;
        }

        @Override
        public void onError(CameraDevice failedCamera, int error) {
            failedCamera.close();
            camera = null;
            runOnUiThread(() -> status.setText("Camera error"));
        }
    };

    private void createPreviewSession() {
        try {
            SurfaceTexture texture = preview.getSurfaceTexture();
            texture.setDefaultBufferSize(dp(300), dp(300));
            Surface surface = new Surface(texture);
                CaptureRequest.Builder request = camera.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW);
            request.addTarget(surface);
            camera.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                session.setRepeatingRequest(request.build(), null, cameraHandler);
                            } catch (CameraAccessException exception) {
                                status.setText("Preview unavailable");
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            status.setText("Preview unavailable");
                        }
                    }, cameraHandler);
        } catch (CameraAccessException | SecurityException exception) {
            status.setText("Preview unavailable");
        }
    }

    private void detect(Bitmap frame) {
        try {
            ByteBuffer input = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
                    .order(ByteOrder.nativeOrder());
            int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
            frame.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
            for (int pixel : pixels) {
                input.putFloat(Color.red(pixel) / 255f);
                input.putFloat(Color.green(pixel) / 255f);
                input.putFloat(Color.blue(pixel) / 255f);
            }
            input.rewind();
            int[] shape = interpreter.getOutputTensor(0).shape();
            float[][][] output = new float[shape[0]][shape[1]][shape[2]];
            interpreter.run(input, output);
            List<Detection> detections = decode(output, shape);
            runOnUiThread(() -> overlay.setDetections(detections));
        } catch (RuntimeException exception) {
            runOnUiThread(() -> status.setText("Model output format unsupported"));
        } finally {
            processing = false;
            frame.recycle();
        }
    }

    private List<Detection> decode(float[][][] output, int[] shape) {
        List<Detection> result = new ArrayList<>();
        boolean channelsFirst = shape[1] < shape[2];
        int candidates = channelsFirst ? shape[2] : shape[1];
        int values = channelsFirst ? shape[1] : shape[2];
        for (int index = 0; index < candidates; index++) {
            float centerX = value(output, channelsFirst, 0, index);
            float centerY = value(output, channelsFirst, 1, index);
            float width = value(output, channelsFirst, 2, index);
            float height = value(output, channelsFirst, 3, index);
            float confidence = 0f;
            for (int channel = 4; channel < values; channel++) {
                confidence = Math.max(confidence, value(output, channelsFirst, channel, index));
            }
            if (confidence < 0.45f) {
                continue;
            }
            result.add(new Detection(new RectF(
                    (centerX - width / 2f) / INPUT_SIZE,
                    (centerY - height / 2f) / INPUT_SIZE,
                    (centerX + width / 2f) / INPUT_SIZE,
                    (centerY + height / 2f) / INPUT_SIZE), confidence));
        }
        return result;
    }

    private float value(float[][][] output, boolean channelsFirst, int channel, int candidate) {
        return channelsFirst ? output[0][channel][candidate] : output[0][candidate][channel];
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private Button button(String label, int color) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setMinHeight(dp(50));
        button.setBackgroundColor(color);
        return button;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CAMERA_REQUEST && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else if (requestCode == CAMERA_REQUEST) {
            status.setText("Camera permission is required");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraThread();
    }

    @Override
    protected void onPause() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (camera != null) {
            camera.close();
            camera = null;
        }
        if (cameraThread != null) {
            cameraThread.quitSafely();
            cameraThread = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (interpreter != null) {
            interpreter.close();
        }
        if (inferenceExecutor != null) {
            inferenceExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    private static final class Detection {
        private final RectF bounds;
        private final float confidence;

        private Detection(RectF bounds, float confidence) {
            this.bounds = bounds;
            this.confidence = confidence;
        }
    }

    private final class DetectionOverlay extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private List<Detection> detections = new ArrayList<>();

        private DetectionOverlay(Context context) {
            super(context);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Palette.ACCENT);
        }

        private void setDetections(List<Detection> detections) {
            this.detections = detections;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (Detection detection : detections) {
                canvas.drawRect(detection.bounds.left * getWidth(),
                        detection.bounds.top * getHeight(),
                        detection.bounds.right * getWidth(),
                        detection.bounds.bottom * getHeight(), paint);
            }
        }
    }
}
