package com.example.newmediawritingprizesubmission2;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class BluePlaqueDetector {
    private static final String TAG = "BluePlaqueDetector";

    // English Heritage blue plaque color range (HSV)
    // The plaques are a distinctive dark blue
    private static final float HUE_MIN = 200f;
    private static final float HUE_MAX = 230f;
    private static final float SATURATION_MIN = 0.4f;
    private static final float VALUE_MIN = 0.3f;

    // Minimum percentage of blue pixels to consider it a plaque
    private static final float BLUE_PIXEL_THRESHOLD = 0.05f; // 5% of image

    public interface DetectionCallback {
        void onPlaqueDetected(Bitmap bitmap);
        void onNoPlaqueFound();
    }

    public void detectPlaque(Bitmap bitmap, DetectionCallback callback) {
        if (bitmap == null) {
            callback.onNoPlaqueFound();
            return;
        }

        // Run detection on background thread
        new Thread(() -> {
            boolean detected = analyzeImage(bitmap);
            if (detected) {
                Log.d(TAG, "Blue plaque detected!");
                callback.onPlaqueDetected(bitmap);
            } else {
                callback.onNoPlaqueFound();
            }
        }).start();
    }

    private boolean analyzeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;
        int bluePixelCount = 0;

        // Sample pixels (checking every pixel is slow, so we sample)
        int sampleStep = 4; // Check every 4th pixel

        float[] hsv = new float[3];

        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                int pixel = bitmap.getPixel(x, y);

                Color.colorToHSV(pixel, hsv);

                float hue = hsv[0];
                float saturation = hsv[1];
                float value = hsv[2];

                // Check if pixel matches blue plaque color
                if (hue >= HUE_MIN && hue <= HUE_MAX &&
                    saturation >= SATURATION_MIN &&
                    value >= VALUE_MIN) {
                    bluePixelCount++;
                }
            }
        }

        // Adjust count for sampling
        int sampledPixels = totalPixels / (sampleStep * sampleStep);
        float blueRatio = (float) bluePixelCount / sampledPixels;

        Log.d(TAG, "Blue pixel ratio: " + blueRatio + " (threshold: " + BLUE_PIXEL_THRESHOLD + ")");

        return blueRatio >= BLUE_PIXEL_THRESHOLD;
    }
}
