package com.example.newmediawritingprizesubmission2.ocr;

import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.common.InputImage;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TextExtractor {
    private static final String TAG = "TextExtractor";
    private final TextRecognizer recognizer;

    public TextExtractor() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void extractTextFromPlaque(Bitmap bitmap, TextCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String extractedText = visionText.getText();
                    Log.d(TAG, "Raw extracted text: " + extractedText);

                    // Parse for person's name and details
                    String personInfo = parseBlueplaqueText(extractedText);
                    callback.onTextExtracted(personInfo);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text extraction failed", e);
                    callback.onTextExtracted("Unknown historical figure");
                });
    }

    private String parseBlueplaqueText(String text) {
        // Blue plaques typically have patterns like:
        // "PERSON NAME (1800-1900) Profession lived here"

        // Try to extract name (usually in capitals)
        Pattern namePattern = Pattern.compile("([A-Z][A-Z\\s\\.]+)(?:\\s*\\(|\\s+[0-9])");
        Matcher nameMatcher = namePattern.matcher(text);

        String name = "";
        if (nameMatcher.find()) {
            name = nameMatcher.group(1).trim();
        }

        // Try to extract profession
        Pattern professionPattern = Pattern.compile("(?:was a|was an|the)\\s+([\\w\\s]+?)(?:\\s+who|\\s+lived|\\s+born|\\.|,)");
        Matcher professionMatcher = professionPattern.matcher(text.toLowerCase());

        String profession = "";
        if (professionMatcher.find()) {
            profession = professionMatcher.group(1).trim();
        }

        // Combine information
        if (!name.isEmpty()) {
            return profession.isEmpty() ? name : name + ", " + profession;
        } else {
            // Fallback to first few words
            String[] words = text.split("\\s+");
            return words.length > 2 ? words[0] + " " + words[1] : text;
        }
    }

    public interface TextCallback {
        void onTextExtracted(String text);
    }
}
