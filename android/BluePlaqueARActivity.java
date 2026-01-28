package com.example.newmediawritingprizesubmission2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.newmediawritingprizesubmission2.location.LocationChecker;
import com.example.newmediawritingprizesubmission2.ocr.TextExtractor;
import com.example.newmediawritingprizesubmission2.quiz.QuizFragment;
import com.example.newmediawritingprizesubmission2.services.FallbackImageGenerator;
import com.example.newmediawritingprizesubmission2.services.NLPManager;
import com.google.ar.core.*;

import io.github.sceneview.ar.ARSceneView;

import kotlin.Unit;

public class BluePlaqueARActivity extends AppCompatActivity {

    private static final String TAG = "BluePlaqueAR";

    private ARSceneView arSceneView;
    private Session arSession;
    private boolean isProcessingPlaque = false;

    private TextExtractor textExtractor;
    private FallbackImageGenerator imageGenerator;
    private LocationChecker locationChecker;
    private BluePlaqueDetector plaqueDetector;
    private NLPManager nlpManager;

    // Frame capture timing
    private Handler frameHandler = new Handler(Looper.getMainLooper());
    private static final long FRAME_CHECK_INTERVAL = 1000; // Check every 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initialize();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initialize() {
        textExtractor = new TextExtractor();
        imageGenerator = new FallbackImageGenerator(this);
        locationChecker = new LocationChecker(this);
        plaqueDetector = new BluePlaqueDetector();
        nlpManager = new NLPManager();
        setupArSceneView();
    }

    private void setupArSceneView() {
        arSceneView = findViewById(R.id.ar_scene_view);
        arSceneView.setLifecycle(getLifecycle());

        arSceneView.setOnSessionCreated(session -> {
            Log.d(TAG, "AR Session created");
            arSession = session;
            configureSession();
            startFrameChecking();
            return Unit.INSTANCE;
        });
    }

    private void configureSession() {
        if (arSession == null) return;
        try {
            Config config = new Config(arSession);
            config.setFocusMode(Config.FocusMode.AUTO);
            arSession.configure(config);
            Log.d(TAG, "Session configured");
        } catch (Exception e) {
            Log.e(TAG, "Config failed", e);
        }
    }

    private void startFrameChecking() {
        frameHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isProcessingPlaque) {
                    captureAndAnalyzeFrame();
                }
                frameHandler.postDelayed(this, FRAME_CHECK_INTERVAL);
            }
        }, FRAME_CHECK_INTERVAL);
    }

    private void captureAndAnalyzeFrame() {
        if (arSceneView.getWidth() == 0 || arSceneView.getHeight() == 0) {
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                arSceneView.getWidth(),
                arSceneView.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        try {
            PixelCopy.request(arSceneView, bitmap, result -> {
                if (result == PixelCopy.SUCCESS) {
                    plaqueDetector.detectPlaque(bitmap, new BluePlaqueDetector.DetectionCallback() {
                        @Override
                        public void onPlaqueDetected(Bitmap detectedBitmap) {
                            handlePlaqueDetected(detectedBitmap);
                        }

                        @Override
                        public void onNoPlaqueFound() {
                            // Continue scanning
                        }
                    });
                }
            }, new Handler(Looper.getMainLooper()));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "PixelCopy failed", e);
        }
    }

    private void handlePlaqueDetected(Bitmap bitmap) {
        if (isProcessingPlaque) return;

        isProcessingPlaque = true;
        runOnUiThread(() -> Toast.makeText(this, "Blue plaque detected!", Toast.LENGTH_SHORT).show());

        // Step 1: Extract raw text using OCR
        textExtractor.extractTextFromPlaque(bitmap, rawText -> {
            Log.d(TAG, "Raw OCR text: " + rawText);

            // Step 2: Use NLP to extract person information
            nlpManager.extractPersonInfo(rawText, new NLPManager.NLPCallback() {
                @Override
                public void onSuccess(NLPManager.PersonInfo info) {
                    Log.d(TAG, "Extracted person: " + info.name + ", " + info.profession);
                    generateHistoricalImage(info);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "NLP failed: " + error);
                    // Fallback: use raw text as name
                    NLPManager.PersonInfo fallback = new NLPManager.PersonInfo();
                    fallback.name = rawText.split("\n")[0]; // Use first line
                    generateHistoricalImage(fallback);
                }
            });
        });
    }

    private void generateHistoricalImage(NLPManager.PersonInfo personInfo) {
        imageGenerator.generateImage(personInfo.name, personInfo.profession,
                new FallbackImageGenerator.ImageGenerationCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            startQuiz(personInfo, imageUrl);
                            isProcessingPlaque = false;
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            startQuiz(personInfo, null);
                            isProcessingPlaque = false;
                        });
                    }
                });
    }

    private void startQuiz(NLPManager.PersonInfo personInfo, String imageUrl) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString("personName", personInfo.name);
        args.putString("profession", personInfo.profession);
        args.putString("years", personInfo.years);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), "quiz");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        frameHandler.removeCallbacksAndMessages(null);
    }
}
