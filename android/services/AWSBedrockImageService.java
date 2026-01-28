package com.example.newmediawritingprizesubmission2.services;

import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.charset.StandardCharsets;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
// We comment this out until the time we need to actually use our API key
// import com.example.newmediawritingprizesubmission2.BuildConfig;
import com.example.newmediawritingprizesubmission2.services.ImageGenerationService;

public class AWSBedrockImageService implements ImageGenerationService {
    private static final String TAG = "AWSBedrockService";
    // We comment this out until the time we need to actually use our API key
    // private static final String AWS_ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY;
    // We comment this out until the time we need to actually use our API key
    // private static final String AWS_SECRET_KEY = BuildConfig.AWS_SECRET_KEY;
    private static final String AWS_REGION = "us-east-1";

    private BedrockRuntimeClient bedrockClient;

   /* We've commented this out to disable the AWS facility temporarily.
     public AWSBedrockImageService() {
        if (isAvailable()) {
            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(
                        AWS_ACCESS_KEY,
                        AWS_SECRET_KEY
                );

                bedrockClient = BedrockRuntimeClient.builder()
                        .region(Region.of(AWS_REGION))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .build();
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize AWS client", e);
            }
        }
    }


    */


   @Override
    public boolean isAvailable() {

       return false;

       //We have commented out the below lines of code as we just want to test the app temporarily without using AWS' features
       // return AWS_ACCESS_KEY != null && !AWS_ACCESS_KEY.isEmpty() &&
         //       AWS_SECRET_KEY != null && !AWS_SECRET_KEY.isEmpty() &&
         //       !AWS_ACCESS_KEY.equals("YOUR_AWS_ACCESS_KEY");
    }




    @Override
    public void generateImage(String prompt, ImageGenerationCallback callback) {
        if (!isAvailable() || bedrockClient == null) {
            callback.onError("AWS credentials not configured");
            return;
        }

        Log.d(TAG, "Generating image with AWS Bedrock: " + prompt);

        // Execute on background thread
        new Thread(() -> {
            try {
                // Create request for Stable Diffusion
                JSONObject requestBody = new JSONObject();
                JSONArray textPrompts = new JSONArray();
                textPrompts.put(new JSONObject()
                        .put("text", prompt)
                        .put("weight", 1.0));

                requestBody.put("text_prompts", textPrompts);
                requestBody.put("cfg_scale", 7.0);
                requestBody.put("steps", 50);
                requestBody.put("seed", 0);
                requestBody.put("width", 512);
                requestBody.put("height", 512);

                InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                        .modelId("stability.stable-diffusion-xl-v1")
                        .contentType("application/json")
                        .accept("application/json")
                        .body(SdkBytes.fromString(requestBody.toString(), StandardCharsets.UTF_8))
                        .build();

                InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);

                // Parse response
                String responseBody = response.body().asUtf8String();
                JSONObject jsonResponse = new JSONObject(responseBody);

                // Extract base64 image
                String base64Image = jsonResponse.getJSONArray("artifacts")
                        .getJSONObject(0)
                        .getString("base64");

                // Convert to data URL
                String imageUrl = "data:image/png;base64," + base64Image;

                Log.d(TAG, "AWS Bedrock image generated successfully");

                // Call callback on main thread
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onSuccess(imageUrl)
                );

            } catch (Exception e) {
                Log.e(TAG, "AWS Bedrock error", e);
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("AWS Bedrock error: " + e.getMessage())
                );
            }
        }).start();
    }
}
