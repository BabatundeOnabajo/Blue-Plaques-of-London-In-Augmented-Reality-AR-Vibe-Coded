package com.example.newmediawritingprizesubmission2.services;

public interface ImageGenerationService {
    void generateImage(String prompt, ImageGenerationCallback callback);
    boolean isAvailable();

    interface ImageGenerationCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
