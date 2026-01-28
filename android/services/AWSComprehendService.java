package com.example.newmediawritingprizesubmission2.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

// Uncomment these imports when you add the Gradle dependency:
// import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
// import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.comprehend.ComprehendClient;
// import software.amazon.awssdk.services.comprehend.model.DetectEntitiesRequest;
// import software.amazon.awssdk.services.comprehend.model.DetectEntitiesResponse;
// import software.amazon.awssdk.services.comprehend.model.Entity;

public class AWSComprehendService {
    private static final String TAG = "AWSComprehendService";

    // =========================================================================
    // AWS CONFIGURATION
    // =========================================================================
    // To enable AWS Comprehend:
    //
    // 1. Add to gradle.properties:
    //    AWS_ACCESS_KEY=your_access_key_here
    //    AWS_SECRET_KEY=your_secret_key_here
    //
    // 2. Add to app/build.gradle dependencies:
    //    implementation 'software.amazon.awssdk:comprehend:2.21.0'
    //
    // 3. Add to app/build.gradle defaultConfig:
    //    buildConfigField "String", "AWS_ACCESS_KEY", ""${AWS_ACCESS_KEY}""
    //    buildConfigField "String", "AWS_SECRET_KEY", ""${AWS_SECRET_KEY}""
    //
    // 4. Set ENABLE_AWS to true below
    // 5. Uncomment the imports at the top of this file
    // 6. Uncomment the code in the constructor and extractPersonInfo method
    // =========================================================================

    private static final boolean ENABLE_AWS = false; // Change to true when configured

    // Uncomment when configured:
    // private static final String AWS_ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY;
    // private static final String AWS_SECRET_KEY = BuildConfig.AWS_SECRET_KEY;
    // private static final String AWS_REGION = "us-east-1";
    // private ComprehendClient comprehendClient;

    public AWSComprehendService() {
        if (ENABLE_AWS) {
            initializeAWSClient();
        }
    }

    private void initializeAWSClient() {
        // Uncomment this entire method body when AWS is configured:
        /*
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                AWS_ACCESS_KEY,
                AWS_SECRET_KEY
            );

            comprehendClient = ComprehendClient.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

            Log.d(TAG, "AWS Comprehend client initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AWS Comprehend client", e);
        }
        */
    }

    public boolean isAvailable() {
        if (!ENABLE_AWS) {
            return false;
        }

        // Uncomment when configured:
        // return comprehendClient != null;

        return false;
    }

    public void extractPersonInfo(String rawText, NLPCallback callback) {
        if (!isAvailable()) {
            callback.onError("AWS Comprehend not configured");
            return;
        }

        // Uncomment this entire block when AWS is configured:
        /*
        Log.d(TAG, "Extracting person info with AWS Comprehend");

        new Thread(() -> {
            try {
                DetectEntitiesRequest request = DetectEntitiesRequest.builder()
                    .text(rawText)
                    .languageCode("en")
                    .build();

                DetectEntitiesResponse response = comprehendClient.detectEntities(request);
                java.util.List<entity> entities = response.entities();

                PersonInfo info = new PersonInfo();

                for (Entity entity : entities) {
                    String entityType = entity.type().toString();

                    switch (entityType) {
                        case "PERSON":
                            // Take the first person found as the main subject
                            if (info.name.equals("Unknown")) {
                                info.name = entity.text();
                                Log.d(TAG, "Found person: " + info.name);
                            }
                            break;
                        case "DATE":
                            // Collect date information
                            if (info.years.isEmpty()) {
                                info.years = entity.text();
                                Log.d(TAG, "Found date: " + info.years);
                            }
                            break;
                        case "TITLE":
                            // Job titles or honorifics
                            if (info.profession.isEmpty()) {
                                info.profession = entity.text();
                                Log.d(TAG, "Found title: " + info.profession);
                            }
                            break;
                        case "ORGANIZATION":
                            // Sometimes profession is detected as organization
                            if (info.profession.isEmpty()) {
                                info.profession = entity.text();
                                Log.d(TAG, "Found organization: " + info.profession);
                            }
                            break;
                    }
                }

                Log.d(TAG, "AWS Comprehend extraction complete: " + info.getDisplayName());

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(info));

            } catch (Exception e) {
                Log.e(TAG, "AWS Comprehend error", e);
                new Handler(Looper.getMainLooper()).post(() ->
                    callback.onError("AWS Comprehend error: " + e.getMessage()));
            }
        }).start();
        */

        // Remove this line when uncommenting the above:
        callback.onError("AWS Comprehend not enabled");
    }

    // =========================================================================
    // Data classes and interfaces
    // =========================================================================

    public static class PersonInfo {
        public String name = "Unknown";
        public String profession = "";
        public String years = "";

        public String getDisplayName() {
            if (profession.isEmpty()) {
                return name;
            }
            return name + ", " + profession;
        }

        @Override
        public String toString() {
            return "PersonInfo{name='" + name + "', profession='" + profession + "', years='" + years + "'}";
        }
    }

    public interface NLPCallback {
        void onSuccess(PersonInfo info);
        void onError(String error);
    }
}
