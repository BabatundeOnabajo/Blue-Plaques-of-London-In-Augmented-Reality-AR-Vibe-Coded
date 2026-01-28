package com.example.newmediawritingprizesubmission2.quiz;

import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.newmediawritingprizesubmission2.Question;
import com.example.newmediawritingprizesubmission2.R;
import com.example.newmediawritingprizesubmission2.quiz.QuizManager;

import java.util.List;

public class QuizFragment extends DialogFragment {
    private static final String TAG = "QuizFragment";

    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<Question> questions;

    // UI elements
    private ImageView historicalImageView;
    private TextView questionTextView;
    private RadioGroup optionsRadioGroup;
    private Button nextButton;
    private TextView scoreTextView;
    private ProgressBar progressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        // Initialize UI elements
        historicalImageView = view.findViewById(R.id.historicalImageView);
        questionTextView = view.findViewById(R.id.questionTextView);
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup);
        nextButton = view.findViewById(R.id.nextButton);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        progressBar = view.findViewById(R.id.progressBar);

        // Get data from arguments
        Bundle args = getArguments();
        String personName = args.getString("personName", "Unknown");
        String profession = args.getString("profession", "");
        String years = args.getString("years", "");
        String imageUrl = args.getString("imageUrl");

        // Load image
        loadHistoricalImage(imageUrl);

        // Show loading state
        questionTextView.setText("Generating quiz questions...");
        nextButton.setEnabled(false);

        // Generate questions (async - may use AI or local)
        QuizManager quizManager = new QuizManager();
        quizManager.generateQuizAsync(personName, profession, years, generatedQuestions -> {
            // This callback runs when questions are ready
            getActivity().runOnUiThread(() -> {
                questions = generatedQuestions;

                // Set up progress bar
                progressBar.setMax(questions.size());
                progressBar.setProgress(1);

                // Display first question
                displayQuestion(currentQuestionIndex);
                updateScore();

                // Enable the next button
                nextButton.setEnabled(true);
            });
        });

        // Set up next button
        nextButton.setOnClickListener(v -> {
            if (questions == null || questions.isEmpty()) return;

            if (checkAnswer()) {
                score++;
                updateScore();
            }

            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion(currentQuestionIndex);
                progressBar.setProgress(currentQuestionIndex + 1);
            } else {
                showResults();
            }
        });

        return view;
    }

    private void loadHistoricalImage(String imageUrl) {
        if (imageUrl == null) {
            historicalImageView.setImageResource(R.drawable.placeholder_historical);
            return;
        }

        if (imageUrl.startsWith("data:image")) {
            // Handle base64 image from AWS Bedrock
            String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            historicalImageView.setImageBitmap(bitmap);
        } else if (imageUrl.startsWith("android.resource://")) {
            // Handle local resource
            historicalImageView.setImageURI(Uri.parse(imageUrl));
        } else {
            // Handle remote URL
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_historical)
                    .error(R.drawable.placeholder_historical)
                    .into(historicalImageView);
        }
    }

    private void displayQuestion(int index) {
        Question question = questions.get(index);
        questionTextView.setText(question.getQuestion());

        optionsRadioGroup.removeAllViews();
        List<String> options = question.getOptions();

        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(options.get(i));
            radioButton.setId(i);
            radioButton.setTextSize(16);
            radioButton.setPadding(8, 8, 8, 8);
            optionsRadioGroup.addView(radioButton);
        }

        nextButton.setEnabled(true);
        nextButton.setText(index < questions.size() - 1 ? "Next" : "Finish");
    }

    private boolean checkAnswer() {
        int selectedId = optionsRadioGroup.getCheckedRadioButtonId();
        return selectedId != -1 && selectedId == questions.get(currentQuestionIndex).getCorrectAnswer();
    }

    private void updateScore() {
        scoreTextView.setText(String.format("Score: %d/%d", score, currentQuestionIndex));
    }

    private void showResults() {
        // Create results dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Quiz Complete!");
        builder.setMessage(String.format("Your final score: %d out of %d\n\nGreat job learning about this historical figure!",
                score, questions.size()));

        builder.setPositiveButton("Share", (dialog, which) -> shareResults());
        builder.setNegativeButton("Close", (dialog, which) -> dismiss());

        builder.show();
    }

    private void shareResults() {
        String shareText = String.format("I just scored %d/%d on the Blue Plaque AR Quiz! " +
                        "I learned about a fascinating historical figure in London. #BluePlaqueAR",
                score, questions.size());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share your score"));
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
