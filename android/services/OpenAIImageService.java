package com.example.newmediawritingprizesubmission2.services;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;
import com.example.newmediawritingprizesubmission2.BuildConfig;
import com.example.newmediawritingprizesubmission2.services.ImageGenerationService;

import java.util.List;
import android.util.Log;

public class OpenAIImageService implements ImageGenerationService {
    private static final String TAG = "OpenAIImageService";
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;
    private final DallEService service;

    // Retrofit interface
    interface DallEService {
        @POST("v1/images/generations")
        Call<ImageGenerationResponse> generateImage(
                @Header("Authorization") String authorization,
                @Body ImageGenerationRequest request
        );
    }

    // Request model
    static class ImageGenerationRequest {
        private String prompt;
        private int n = 1;
        private String size = "512x512";

        public ImageGenerationRequest(String prompt) {
            this.prompt = prompt;
        }

        // Getters and setters
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public int getN() { return n; }
        public void setN(int n) { this.n = n; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
    }

    // Response models
    static class ImageGenerationResponse {
        private List<ImageData> data;

        public List<ImageData> getData() { return data; }
        public void setData(List<ImageData> data) { this.data = data; }
    }

    static class ImageData {
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public OpenAIImageService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(DallEService.class);
    }

    @Override
    public boolean isAvailable() {
        return API_KEY != null && !API_KEY.isEmpty() && !API_KEY.equals("YOUR_API_KEY");
    }

    @Override
    public void generateImage(String prompt, ImageGenerationCallback callback) {
        if (!isAvailable()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        Log.d(TAG, "Generating image with prompt: " + prompt);
        ImageGenerationRequest request = new ImageGenerationRequest(prompt);

        service.generateImage("Bearer " + API_KEY, request).enqueue(new Callback<ImageGenerationResponse>() {
            @Override
            public void onResponse(Call<ImageGenerationResponse> call, Response<ImageGenerationResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {
                    String imageUrl = response.body().getData().get(0).getUrl();
                    Log.d(TAG, "Image generated successfully: " + imageUrl);
                    callback.onSuccess(imageUrl);
                } else {
                    String error = "OpenAI request failed: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ImageGenerationResponse> call, Throwable t) {
                String error = "OpenAI request failed: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }
}
