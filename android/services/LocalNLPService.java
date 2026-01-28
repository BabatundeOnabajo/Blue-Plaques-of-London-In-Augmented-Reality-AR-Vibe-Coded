// New file: NLPManager.java
package com.example.newmediawritingprizesubmission2.services;

import android.util.Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocalNLPService {
    private static final String TAG = "LocalNLPService";

    // Common words that are NOT names
    private static final List STOP_WORDS = Arrays.asList(
            "the", "and", "was", "were", "here", "lived", "born", "died",
            "this", "that", "with", "from", "have", "has", "had", "been",
            "english", "british", "london", "england", "plaque", "heritage",
            "street", "road", "house", "building", "where", "who", "which",
            "founder", "pioneer", "artist", "writer", "poet", "author",
            "scientist", "politician", "actor", "actress", "musician",
            "composer", "painter", "architect", "engineer", "doctor",
            "inventor", "philosopher", "reformer", "statesman"
    );

    private static final List<String> TITLES = Arrays.asList(
            "sir", "dame", "lord", "lady", "dr", "dr.", "professor", "prof",
            "captain", "colonel", "general", "admiral", "reverend", "rev"
    );


    public boolean isAvailable() {
        return true; // Always available
    }

    public void extractPersonInfo(String rawText, NLPCallback callback) {
        Log.d(TAG, "Processing text locally: " + rawText);

        PersonInfo info = new PersonInfo();

        // Clean the text
        String cleanedText = rawText.replace("\n", " ").trim();

        // Strategy 1: Look for ALL CAPS names (common on plaques)
        String capsName = findCapsName(cleanedText);
        if (capsName != null) {
            info.name = formatName(capsName);
        }

        // Strategy 2: Look for names with titles
        if (info.name.equals("Unknown")) {
            String titledName = findTitledName(cleanedText);
            if (titledName != null) {
                info.name = titledName;
            }
        }

        // Strategy 3: Look for names followed by dates
        if (info.name.equals("Unknown")) {
            String datedName = findNameWithDates(cleanedText);
            if (datedName != null) {
                info.name = datedName;
            }
        }

        // Extract years
        info.years = findYears(cleanedText);

        // Extract profession
        info.profession = findProfession(cleanedText);

        Log.d(TAG, "Extracted locally - Name: " + info.name +
                ", Profession: " + info.profession +
                ", Years: " + info.years);

        callback.onSuccess(info);
    }

    private String findCapsName(String text) {
        // Look for sequences of capitalized words (at least 2)
        Pattern pattern = Pattern.compile("\\b([A-Z]{2,}(?:\\s+[A-Z]{2,})+)\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String potential = matcher.group(1);
            // Filter out common non-name phrases
            if (!containsStopWord(potential.toLowerCase())) {
                return potential;
            }
        }
        return null;
    }

    private String findTitledName(String text) {
        String lowerText = text.toLowerCase();

        for (String title : TITLES) {

            int titleIndex = lowerText.indexOf(title + " ");
            if (titleIndex != -1) {
                // Extract words following the title
                String afterTitle = text.substring(titleIndex + title.length()).trim();
                String[] words = afterTitle.split("\\s+");

                StringBuilder name = new StringBuilder(title.substring(0, 1).toUpperCase() + title.substring(1));
                int wordCount = 0;

                for (String word : words) {
                    // Stop at common non-name words or punctuation
                    String cleanWord = word.replaceAll("[^a-zA-Z]", "");
                    if (cleanWord.isEmpty() || STOP_WORDS.contains(cleanWord.toLowerCase())) {
                        break;
                    }
                    if (Character.isUpperCase(word.charAt(0)) || wordCount == 0) {
                        name.append(" ").append(cleanWord);
                        wordCount++;
                        if (wordCount >= 3) break; // Most names are 2-3 words
                    } else {
                        break;
                    }
                }

                if (wordCount >= 1) {
                    return name.toString();
                }
            }
        }
        return null;
    }

    private String findNameWithDates(String text) {
        // Pattern: Name (1800-1900) or Name 1800-1900
        Pattern pattern = Pattern.compile("([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)+)\\s*$?\\d{4}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String potential = matcher.group(1).trim();
            if (!containsStopWord(potential.toLowerCase())) {
                return potential;
            }
        }
        return null;
    }

    private String findYears(String text) {
        // Look for date ranges like 1850-1920 or (1850-1920)
        Pattern pattern = Pattern.compile("$?(\\d{4})\\s*[-–]\\s*(\\d{4})$?");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1) + "-" + matcher.group(2);
        }

        // Look for single years
        Pattern singleYear = Pattern.compile("\\b(1[6-9]\\d{2}|20[0-2]\\d)\\b");
        Matcher singleMatcher = singleYear.matcher(text);

        if (singleMatcher.find()) {
            return singleMatcher.group(1);
        }

        return "";
    }

    private String findProfession(String text) {
        String lowerText = text.toLowerCase();

        String[] professions = {
                "writer", "author", "poet", "novelist", "playwright",
                "artist", "painter", "sculptor", "architect",
                "scientist", "physicist", "chemist", "biologist", "mathematician",
                "musician", "composer", "singer", "conductor",
                "actor", "actress", "director", "filmmaker",
                "politician", "statesman", "prime minister", "reformer",
                "inventor", "engineer", "pioneer",
                "doctor", "physician", "surgeon", "nurse",
                "philosopher", "economist", "historian",
                "explorer", "navigator", "aviator",
                "soldier", "general", "admiral",
                "king", "queen", "prince", "princess"
        };

        for (String profession : professions) {
            if (lowerText.contains(profession)) {
                return profession.substring(0, 1).toUpperCase() + profession.substring(1);
            }
        }

        return "";
    }

    private boolean containsStopWord(String text) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (STOP_WORDS.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String formatName(String capsName) {
        // Convert "CHARLES DICKENS" to "Charles Dickens"
        StringBuilder formatted = new StringBuilder();
        String[] words = capsName.split("\\s+");

        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }

        return formatted.toString();
    }

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
