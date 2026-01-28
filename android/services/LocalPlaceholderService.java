package com.example.newmediawritingprizesubmission2.services;

import android.content.Context;

import com.example.newmediawritingprizesubmission2.R;
import com.example.newmediawritingprizesubmission2.services.ImageGenerationService;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class LocalPlaceholderService implements ImageGenerationService {
    private static final String TAG = "LocalPlaceholderService";
    private final Context context;
    private final Map<String, Integer> historicalFigures;

    public LocalPlaceholderService(Context context) {
        this.context = context;
        this.historicalFigures = new HashMap<>();

        // Pre-load some common historical figures
        historicalFigures.put("charles dickens", R.drawable.placeholder_dickens);
        historicalFigures.put("virginia woolf", R.drawable.placeholder_woolf);
        historicalFigures.put("winston churchill", R.drawable.placeholder_churchill);
        historicalFigures.put("alan turing", R.drawable.placeholder_turing);
        historicalFigures.put("isaac newton", R.drawable.placeholder_newton);
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available
    }

    @Override
    public void generateImage(String prompt, ImageGenerationCallback callback) {
        Log.d(TAG, "Using local placeholder for: " + prompt);

        // Extract name from prompt
        String lowerPrompt = prompt.toLowerCase();

        for (Map.Entry<String, Integer> entry : historicalFigures.entrySet()) {
            if (lowerPrompt.contains(entry.getKey())) {
                String resourceUri = "android.resource://" + context.getPackageName() + "/" + entry.getValue();
                Log.d(TAG, "Found specific placeholder: " + entry.getKey());
                callback.onSuccess(resourceUri);
                return;
            }
        }

        // Return generic placeholder
        String genericUri = "android.resource://" + context.getPackageName() + "/" + R.drawable.placeholder_historical;
        Log.d(TAG, "Using generic placeholder");
        callback.onSuccess(genericUri);
    }
}
