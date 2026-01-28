// New file: NLPManager.java
package com.example.newmediawritingprizesubmission2.services;

import android.util.Log;

public class NLPManager {
    private static final String TAG = "NLPManager";

    private OpenAINLPService openAIService;
    private AWSComprehendService awsService;
    private LocalNLPService localService;

    public NLPManager() {
        openAIService = new OpenAINLPService();
        awsService = new AWSComprehendService();
        localService = new LocalNLPService();
    }

    public void extractPersonInfo(String rawText, NLPCallback callback) {
        // Try OpenAI first
        if (openAIService.isAvailable()) {
            Log.d(TAG, "Using OpenAI for NLP");
            openAIService.extractPersonInfo(rawText, new OpenAINLPService.NLPCallback() {
                @Override
                public void onSuccess(OpenAINLPService.PersonInfo info) {
                    callback.onSuccess(convertOpenAIInfo(info));
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "OpenAI failed: " + error + ", trying AWS");
                    tryAWS(rawText, callback);
                }
            });
        } else {
            tryAWS(rawText, callback);
        }
    }

    private void tryAWS(String rawText, NLPCallback callback) {
        if (awsService.isAvailable()) {
            Log.d(TAG, "Using AWS Comprehend for NLP");
            awsService.extractPersonInfo(rawText, new AWSComprehendService.NLPCallback() {
                @Override
                public void onSuccess(AWSComprehendService.PersonInfo info) {
                    callback.onSuccess(convertAWSInfo(info));
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "AWS failed: " + error + ", using local");
                    useLocal(rawText, callback);
                }
            });
        } else {
            useLocal(rawText, callback);
        }
    }

    private void useLocal(String rawText, NLPCallback callback) {
        Log.d(TAG, "Using local NLP");
        localService.extractPersonInfo(rawText, new LocalNLPService.NLPCallback() {
            @Override
            public void onSuccess(LocalNLPService.PersonInfo info) {
                callback.onSuccess(convertLocalInfo(info));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // Converter methods
    private PersonInfo convertOpenAIInfo(OpenAINLPService.PersonInfo info) {
        PersonInfo result = new PersonInfo();
        result.name = info.name;
        result.profession = info.profession;
        result.years = info.years;
        return result;
    }

    private PersonInfo convertAWSInfo(AWSComprehendService.PersonInfo info) {
        PersonInfo result = new PersonInfo();
        result.name = info.name;
        result.profession = info.profession;
        result.years = info.years;
        return result;
    }

    private PersonInfo convertLocalInfo(LocalNLPService.PersonInfo info) {
        PersonInfo result = new PersonInfo();
        result.name = info.name;
        result.profession = info.profession;
        result.years = info.years;
        return result;
    }

    // Unified PersonInfo class
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
    }

    public interface NLPCallback {
        void onSuccess(PersonInfo info);
        void onError(String error);
    }
}
