package com.example.newmediawritingprizesubmission2.services;

import android.util.Log;

import com.example.newmediawritingprizesubmission2.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class OpenAINLPService {

    private static final String TAG = "OpenAINLPService";
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;
    private final ChatService service;

    /* -------------------- Retrofit API -------------------- */

    interface ChatService {
        @POST("v1/chat/completions")
        Call<ChatResponse> chat(
                @Header("Authorization") String authorization,
                @Body ChatRequest request
        );
    }

    /* -------------------- Request Models -------------------- */

    static class ChatRequest {
        private String model = "gpt-3.5-turbo";
        private List<Message> messages;

        public ChatRequest(String prompt) {
            messages = new ArrayList<>();

            messages.add(new Message(
                    "system",
                    "You are a helpful assistant that extracts information from blue plaque text. " +
                            "Extract the person's name, their profession/description, and their birth/death years if available. " +
                            "Respond in this exact format:\n" +
                            "NAME: [full name]\n" +
                            "PROFESSION: [profession or description]\n" +
                            "YEARS: [birth-death years or 'unknown']\n" +
                            "If you cannot find a name, respond with NAME: unknown"
            ));

            messages.add(new Message(
                    "user",
                    "Extract information from this blue plaque text:\n\n" + prompt
            ));
        }

        public String getModel() {
            return model;
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    /* -------------------- Response Models -------------------- */

    static class ChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }
    }

    static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    /* -------------------- Constructor -------------------- */

    public OpenAINLPService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ChatService.class);
    }

    /* -------------------- Availability -------------------- */

    public boolean isAvailable() {
        return API_KEY != null
                && !API_KEY.isEmpty()
                && !API_KEY.equals("YOUR_API_KEY");
    }

    /* -------------------- Public API -------------------- */

    public void extractPersonInfo(String rawText, NLPCallback callback) {

        if (!isAvailable()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        Log.d(TAG, "Extracting info from: " + rawText);

        ChatRequest request = new ChatRequest(rawText);

        service.chat("Bearer " + API_KEY, request)
                .enqueue(new Callback<ChatResponse>() {

                    @Override
                    public void onResponse(
                            Call<ChatResponse> call,
                            Response<ChatResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getChoices() != null
                                && !response.body().getChoices().isEmpty()) {

                            String content = response.body()
                                    .getChoices()
                                    .get(0)
                                    .getMessage()
                                    .getContent();

                            PersonInfo info = parseResponse(content);
                            Log.d(TAG, "Extracted: " + info.name + ", " + info.profession);

                            callback.onSuccess(info);

                        } else {
                            callback.onError("OpenAI request failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatResponse> call, Throwable t) {
                        callback.onError("OpenAI request failed: " + t.getMessage());
                    }
                });
    }

    /* -------------------- Parsing -------------------- */

    private PersonInfo parseResponse(String response) {
        PersonInfo info = new PersonInfo();

        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.startsWith("NAME:")) {
                info.name = line.substring(5).trim();
            } else if (line.startsWith("PROFESSION:")) {
                info.profession = line.substring(11).trim();
            } else if (line.startsWith("YEARS:")) {
                info.years = line.substring(6).trim();
            }
        }

        return info;
    }

    /* -------------------- Models & Callback -------------------- */

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
