package com.example.newmediawritingprizesubmission2.services;

import android.content.Context;
import android.util.Log;

import com.example.newmediawritingprizesubmission2.services.AWSBedrockImageService;
import com.example.newmediawritingprizesubmission2.services.ImageGenerationService;
import com.example.newmediawritingprizesubmission2.services.LocalPlaceholderService;
import com.example.newmediawritingprizesubmission2.services.OpenAIImageService;

import java.util.ArrayList;
import java.util.List;

public class FallbackImageGenerator {
    private static final String TAG = "FallbackImageGenerator";
    private final List<ImageGenerationService> services;
    private final Context context;

    public FallbackImageGenerator(Context context) {
        this.context = context;
        this.services = new ArrayList<>();

        // Add services in priority order
        services.add(new OpenAIImageService());
        services.add(new AWSBedrockImageService());
        services.add(new LocalPlaceholderService(context));
    }

    public void generateImage(String personName, String details, ImageGenerationCallback callback) {
        String prompt = "Historical portrait of " + personName + ", " + details +
                ", in the style of a Victorian painting, high quality, detailed";

        tryNextService(0, prompt, callback);
    }

    private void tryNextService(int index, String prompt, ImageGenerationCallback callback) {
        if (index >= services.size()) {
            callback.onError("No image generation service available");
            return;
        }

        ImageGenerationService service = services.get(index);
        String serviceName = service.getClass().getSimpleName();

        if (service.isAvailable()) {
            Log.d(TAG, "Trying " + serviceName);

            service.generateImage(prompt, new ImageGenerationService.ImageGenerationCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, serviceName + " succeeded");
                    callback.onSuccess(imageUrl);
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, serviceName + " failed: " + error);
                    // Try next service
                    tryNextService(index + 1, prompt, callback);
                }
            });
        } else {
            Log.d(TAG, serviceName + " not available, skipping");
            // Skip unavailable service
            tryNextService(index + 1, prompt, callback);
        }
    }

    public interface ImageGenerationCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
