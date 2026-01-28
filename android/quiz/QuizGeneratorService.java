package com.example.newmediawritingprizesubmission2.quiz;

import android.util.Log;

import com.example.newmediawritingprizesubmission2.BuildConfig;
import com.example.newmediawritingprizesubmission2.Question;

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

public class QuizGeneratorService {

    private static final String TAG = "QuizGeneratorService";
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

    public static class ChatRequest {
        private String model = "gpt-3.5-turbo";
        private List<Message> messages;

        public ChatRequest(String prompt) {
            messages = new ArrayList<>();

            messages.add(new Message(
                    "system",
                    "You are a quiz generator. Generate exactly 5 multiple choice questions. " +
                            "Each question should have 4 options with exactly one correct answer. " +
                            "Format your response EXACTLY like this, with no extra text:\n\n" +
                            "Q1: [Question text]\n" +
                            "A) [Option A]\n" +
                            "B) [Option B]\n" +
                            "C) [Option C]\n" +
                            "D) [Option D]\n" +
                            "ANSWER: [A, B, C, or D]\n\n" +
                            "Q2: [Question text]\n" +
                            "...and so on"
            ));

            messages.add(new Message("user", prompt));
        }

        public String getModel() {
            return model;
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    public static class Message {
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

    public static class ChatResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }
    }

    /* -------------------- Callback -------------------- */

    public interface QuizCallback {
        void onSuccess(List<Question> questions);
        void onError(String error);
    }

    /* -------------------- Constructor -------------------- */

    public QuizGeneratorService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ChatService.class);
    }

    /* -------------------- Public API -------------------- */

    public boolean isAvailable() {
        return API_KEY != null && !API_KEY.isEmpty() && !API_KEY.equals("YOUR_API_KEY");
    }

    public void generateQuiz(
            String personName,
            String profession,
            String years,
            QuizCallback callback
    ) {
        if (!isAvailable()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        String prompt = buildPrompt(personName, profession, years);
        Log.d(TAG, "Generating quiz for: " + personName);

        ChatRequest request = new ChatRequest(prompt);

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

                            List<Question> questions = parseQuizResponse(content);

                            if (questions.isEmpty()) {
                                callback.onError("Failed to parse quiz questions");
                            } else {
                                callback.onSuccess(questions);
                            }

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

    /* -------------------- Helpers -------------------- */

    private String buildPrompt(String personName, String profession, String years) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate 5 interesting quiz questions about ");
        prompt.append(personName);

        if (profession != null && !profession.isEmpty()) {
            prompt.append(", who was a ").append(profession);
        }

        if (years != null && !years.isEmpty()) {
            prompt.append(" (").append(years).append(")");
        }

        prompt.append(". The questions should test knowledge about their life, ");
        prompt.append("achievements, and historical significance. ");
        prompt.append("Make the questions educational but not too difficult.");

        return prompt.toString();
    }

    private List<Question> parseQuizResponse(String response) {
        List<Question> questions = new ArrayList<>();

        try {
            String[] questionBlocks = response.split("Q\\d+:");

            for (String block : questionBlocks) {
                if (block.trim().isEmpty()) continue;

                Question question = parseQuestionBlock(block);
                if (question != null) {
                    questions.add(question);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing quiz response", e);
        }

        return questions;
    }

    private Question parseQuestionBlock(String block) {
        try {
            String[] lines = block.trim().split("\n");
            if (lines.length < 6) return null;

            String questionText = lines[0].trim();

            List<String> options = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                String option = lines[i].trim();
                if (option.length() > 2 && option.charAt(1) == ')') {
                    option = option.substring(2).trim();
                }
                options.add(option);
            }

            int correctAnswer = 0;
            for (String line : lines) {
                if (line.toUpperCase().contains("ANSWER:")) {
                    String answer = line.replaceAll(".*ANSWER:\\s*", "").trim().toUpperCase();
                    correctAnswer = answer.charAt(0) - 'A';
                    break;
                }
            }

            return new Question(questionText, options, correctAnswer);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing question block", e);
        }

        return null;
    }
}
