package com.example.newmediawritingprizesubmission2.quiz;

import android.util.Log;

import com.example.newmediawritingprizesubmission2.Question;

import java.util.*;

public class QuizManager {
    private static final String TAG = "QuizManager";

    private QuizGeneratorService aiService;

    public QuizManager() {
        aiService = new QuizGeneratorService();
    }

    // =========================================================================
    // ASYNC METHOD (Recommended - tries AI first, then local fallback)
    // =========================================================================

    public interface QuizReadyCallback {
        void onQuizReady(List<Question> questions);
    }

    public void generateQuizAsync(String personName, String profession, String years, QuizReadyCallback callback) {

        // Try AI service first
        if (aiService.isAvailable()) {
            Log.d(TAG, "Using OpenAI to generate quiz for: " + personName);

            aiService.generateQuiz(personName, profession, years, new QuizGeneratorService.QuizCallback() {
                @Override
                public void onSuccess(List questions) {
                    Log.d(TAG, "AI generated " + questions.size() + " questions");
                    if (questions.size() >= 3) {
                        callback.onQuizReady(questions);
                    } else {
                        // AI returned too few questions, use local
                        Log.w(TAG, "AI returned insufficient questions, using local");
                        String context = buildContext(personName, profession, years);
                        callback.onQuizReady(generateQuizQuestions(personName, context));
                    }
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "AI quiz generation failed: " + error + ", using local fallback");
                    String context = buildContext(personName, profession, years);
                    callback.onQuizReady(generateQuizQuestions(personName, context));
                }
            });
        } else {
            // Use local fallback immediately
            Log.d(TAG, "OpenAI not available, using local quiz generation");
            String context = buildContext(personName, profession, years);
            callback.onQuizReady(generateQuizQuestions(personName, context));
        }
    }

    private String buildContext(String personName, String profession, String years) {
        StringBuilder context = new StringBuilder(personName);
        if (profession != null && !profession.isEmpty()) {
            context.append(" ").append(profession);
        }
        if (years != null && !years.isEmpty()) {
            context.append(" ").append(years);
        }
        return context.toString();
    }

    // =========================================================================
    // SYNC METHOD (Your existing local logic - kept for backward compatibility)
    // =========================================================================

    public List generateQuizQuestions(String personName, String context) {
        List questions = new ArrayList<>();

        // First check if we have pre-made questions for known figures
        List knownQuestions = getKnownPersonQuestions(personName);
        if (knownQuestions != null) {
            Log.d(TAG, "Using pre-made questions for: " + personName);
            return knownQuestions;
        }

        // Parse context to determine profession
        String profession = extractProfession(context);

        // Question 1: Profession
        questions.add(new Question(
                "What was " + personName + "'s primary profession?",
                generateProfessionOptions(profession),
                0 // Correct answer is first
        ));

        // Question 2: Era
        questions.add(new Question(
                "In which era did " + personName + " primarily live?",
                Arrays.asList("Victorian Era", "Georgian Era", "Edwardian Era", "Modern Era"),
                determineEra(context)
        ));

        // Question 3: Contribution
        questions.add(new Question(
                "What is " + personName + " best known for?",
                generateContributionOptions(profession),
                0
        ));

        // Question 4: Location
        questions.add(new Question(
                "This blue plaque commemorates where " + personName + ":",
                Arrays.asList("Lived", "Worked", "Was born", "Died"),
                0 // Most blue plaques commemorate where someone lived
        ));

        // Question 5: Blue plaque general knowledge
        questions.add(new Question(
                "The English Heritage blue plaque scheme is the oldest of its kind. When did it begin?",
                Arrays.asList("1867", "1901", "1923", "1945"),
                0
        ));

        return questions;
    }

    // =========================================================================
    // PRE-MADE QUESTIONS FOR KNOWN HISTORICAL FIGURES
    // =========================================================================

    private List getKnownPersonQuestions(String personName) {
        String nameLower = personName.toLowerCase();

        // Charles Dickens
        if (nameLower.contains("charles dickens") || nameLower.contains("dickens")) {
            return Arrays.asList(
                    new Question(
                            "Which famous novel did Charles Dickens write?",
                            Arrays.asList("Oliver Twist", "Pride and Prejudice", "Wuthering Heights", "Jane Eyre"),
                            0
                    ),
                    new Question(
                            "In which city was Charles Dickens born?",
                            Arrays.asList("Portsmouth", "London", "Manchester", "Birmingham"),
                            0
                    ),
                    new Question(
                            "Which of these is a Charles Dickens character?",
                            Arrays.asList("Ebenezer Scrooge", "Sherlock Holmes", "Heathcliff", "Mr Darcy"),
                            0
                    ),
                    new Question(
                            "What was the name of Dickens' famous Christmas story?",
                            Arrays.asList("A Christmas Carol", "The Christmas Tree", "Winter Tales", "Holiday Spirit"),
                            0
                    ),
                    new Question(
                            "Charles Dickens often wrote about which social issue?",
                            Arrays.asList("Poverty and social inequality", "Women's suffrage", "Environmental conservation", "Foreign policy"),
                            0
                    )
            );
        }

        // Winston Churchill
        if (nameLower.contains("winston churchill") || nameLower.contains("churchill")) {
            return Arrays.asList(
                    new Question(
                            "Winston Churchill was Prime Minister during which war?",
                            Arrays.asList("World War II", "World War I", "Korean War", "Boer War"),
                            0
                    ),
                    new Question(
                            "Which famous speech included 'We shall fight on the beaches'?",
                            Arrays.asList("We Shall Fight speech (1940)", "Iron Curtain Speech", "Victory Speech", "D-Day Address"),
                            0
                    ),
                    new Question(
                            "Besides being a politician, Churchill was also an accomplished...",
                            Arrays.asList("Painter", "Composer", "Architect", "Sculptor"),
                            0
                    ),
                    new Question(
                            "Churchill won the Nobel Prize in which category?",
                            Arrays.asList("Literature", "Peace", "Physics", "Economics"),
                            0
                    ),
                    new Question(
                            "What was Churchill's famous 'V' hand sign meant to represent?",
                            Arrays.asList("Victory", "Valor", "Virtue", "Vigilance"),
                            0
                    )
            );
        }

        // Virginia Woolf
        if (nameLower.contains("virginia woolf") || nameLower.contains("woolf")) {
            return Arrays.asList(
                    new Question(
                            "Virginia Woolf was a member of which intellectual group?",
                            Arrays.asList("The Bloomsbury Group", "The Inklings", "The Algonquin Round Table", "The Beat Generation"),
                            0
                    ),
                    new Question(
                            "Which novel did Virginia Woolf write?",
                            Arrays.asList("Mrs Dalloway", "Rebecca", "Gone with the Wind", "The Great Gatsby"),
                            0
                    ),
                    new Question(
                            "Virginia Woolf is considered a pioneer of which literary technique?",
                            Arrays.asList("Stream of consciousness", "Magical realism", "Hard-boiled fiction", "Gothic horror"),
                            0
                    ),
                    new Question(
                            "Virginia Woolf's essay 'A Room of One's Own' discusses...",
                            Arrays.asList("Women and fiction writing", "Architecture", "Interior design", "Travel"),
                            0
                    ),
                    new Question(
                            "Virginia Woolf and her husband Leonard founded which press?",
                            Arrays.asList("Hogarth Press", "Penguin Books", "Faber and Faber", "Bloomsbury Publishing"),
                            0
                    )
            );
        }

        // Alan Turing
        if (nameLower.contains("alan turing") || nameLower.contains("turing")) {
            return Arrays.asList(
                    new Question(
                            "Alan Turing is considered the father of which field?",
                            Arrays.asList("Computer science", "Nuclear physics", "Genetics", "Astronomy"),
                            0
                    ),
                    new Question(
                            "During World War II, Turing helped break which enemy code?",
                            Arrays.asList("Enigma", "Morse code", "Purple", "Navajo"),
                            0
                    ),
                    new Question(
                            "Where did Turing do his wartime codebreaking work?",
                            Arrays.asList("Bletchley Park", "Cambridge University", "MI5 Headquarters", "Downing Street"),
                            0
                    ),
                    new Question(
                            "The 'Turing Test' is used to evaluate what?",
                            Arrays.asList("Artificial intelligence", "Computer speed", "Code security", "Mathematical ability"),
                            0
                    ),
                    new Question(
                            "In 2021, Alan Turing appeared on which British banknote?",
                            Arrays.asList("£50 note", "£20 note", "£10 note", "£5 note"),
                            0
                    )
            );
        }

        // Isaac Newton
        if (nameLower.contains("isaac newton") || nameLower.contains("newton")) {
            return Arrays.asList(
                    new Question(
                            "What falling object supposedly inspired Newton's theory of gravity?",
                            Arrays.asList("An apple", "A leaf", "A bird", "A branch"),
                            0
                    ),
                    new Question(
                            "Newton's three famous laws describe which aspect of physics?",
                            Arrays.asList("Motion", "Thermodynamics", "Electricity", "Sound"),
                            0
                    ),
                    new Question(
                            "Besides physics, Newton made major contributions to...",
                            Arrays.asList("Mathematics (calculus)", "Biology", "Chemistry", "Geology"),
                            0
                    ),
                    new Question(
                            "Newton was a professor at which university?",
                            Arrays.asList("Cambridge", "Oxford", "Edinburgh", "St Andrews"),
                            0
                    ),
                    new Question(
                            "What famous book did Newton publish in 1687?",
                            Arrays.asList("Principia Mathematica", "Origin of Species", "Elements", "The Republic"),
                            0
                    )
            );
        }

        // Jimi Hendrix
        if (nameLower.contains("jimi hendrix") || nameLower.contains("hendrix")) {
            return Arrays.asList(
                    new Question(
                            "What instrument was Jimi Hendrix famous for playing?",
                            Arrays.asList("Electric guitar", "Piano", "Drums", "Saxophone"),
                            0
                    ),
                    new Question(
                            "At which 1969 festival did Hendrix famously perform 'The Star-Spangled Banner'?",
                            Arrays.asList("Woodstock", "Monterey Pop", "Isle of Wight", "Altamont"),
                            0
                    ),
                    new Question(
                            "What was the name of Jimi Hendrix's most famous band?",
                            Arrays.asList("The Jimi Hendrix Experience", "The Doors", "Cream", "Led Zeppelin"),
                            0
                    ),
                    new Question(
                            "Which song is one of Hendrix's most famous recordings?",
                            Arrays.asList("Purple Haze", "Stairway to Heaven", "Satisfaction", "Hey Jude"),
                            0
                    ),
                    new Question(
                            "Jimi Hendrix was originally from which country?",
                            Arrays.asList("United States", "United Kingdom", "Jamaica", "Canada"),
                            0
                    )
            );
        }

        return null; // No pre-made questions for this person
    }

    // =========================================================================
    // HELPER METHODS (Your existing code)
    // =========================================================================

    private String extractProfession(String context) {
        String lowerContext = context.toLowerCase();

        if (lowerContext.contains("writer") || lowerContext.contains("author") || lowerContext.contains("novelist")) return "writer";
        if (lowerContext.contains("scientist") || lowerContext.contains("physicist") || lowerContext.contains("chemist")) return "scientist";
        if (lowerContext.contains("politician") || lowerContext.contains("prime minister") || lowerContext.contains("statesman")) return "politician";
        if (lowerContext.contains("artist") || lowerContext.contains("painter") || lowerContext.contains("sculptor")) return "artist";
        if (lowerContext.contains("composer") || lowerContext.contains("musician") || lowerContext.contains("singer")) return "musician";
        if (lowerContext.contains("actor") || lowerContext.contains("actress")) return "actor";
        if (lowerContext.contains("architect")) return "architect";
        if (lowerContext.contains("poet")) return "poet";
        if (lowerContext.contains("philosopher")) return "philosopher";
        if (lowerContext.contains("explorer") || lowerContext.contains("navigator")) return "explorer";
        if (lowerContext.contains("inventor") || lowerContext.contains("engineer")) return "inventor";

        return "historical figure";
    }

    private List generateProfessionOptions(String correctProfession) {
        List allProfessions = new ArrayList();
        allProfessions.add("Writer");
        allProfessions.add("Scientist");
        allProfessions.add("Politician");
        allProfessions.add("Artist");
        allProfessions.add("Musician");
        allProfessions.add("Philosopher");
        allProfessions.add("Explorer");
        allProfessions.add("Inventor");
        allProfessions.add("Actor");
        allProfessions.add("Architect");
        allProfessions.add("Poet");
        allProfessions.add("Doctor");

        List options = new ArrayList();
        options.add(capitalizeFirst(correctProfession));

        // Remove correct answer manually
        String toRemove = null;
        for (int i = 0; i < allProfessions.size(); i++) {
            String p = (String) allProfessions.get(i);
            if (p.equalsIgnoreCase(correctProfession)) {
                toRemove = p;
                break;
            }
        }

        if (toRemove != null) {
            allProfessions.remove(toRemove);
        }

        // Shuffle and add 3 wrong options
        Collections.shuffle(allProfessions);
        for (int i = 0; i < 3 && i < allProfessions.size(); i++) {
            options.add(allProfessions.get(i));
        }

        return options;
    }

    private List<String> generateContributionOptions(String profession) {
        String lowerProfession = profession.toLowerCase();

        if (lowerProfession.equals("writer")) {
            return Arrays.asList("Literary works", "Scientific discoveries", "Political reforms", "Artistic innovations");
        }
        if (lowerProfession.equals("scientist")) {
            return Arrays.asList("Scientific discoveries", "Literary works", "Military victories", "Social reforms");
        }
        if (lowerProfession.equals("politician")) {
            return Arrays.asList("Political leadership and reforms", "Scientific discoveries", "Literary works", "Artistic innovations");
        }
        if (lowerProfession.equals("artist")) {
            return Arrays.asList("Artistic innovations", "Scientific discoveries", "Political reforms", "Literary works");
        }
        if (lowerProfession.equals("musician")) {
            return Arrays.asList("Musical compositions and performances", "Scientific discoveries", "Political reforms", "Literary works");
        }
        if (lowerProfession.equals("architect")) {
            return Arrays.asList("Architectural designs", "Scientific discoveries", "Political reforms", "Musical compositions");
        }
        if (lowerProfession.equals("inventor")) {
            return Arrays.asList("Inventions and innovations", "Literary works", "Political reforms", "Musical compositions");
        }
        if (lowerProfession.equals("explorer")) {
            return Arrays.asList("Exploration and discovery", "Literary works", "Political reforms", "Scientific theories");
        }

        // Default for unknown professions
        return Arrays.asList("Cultural contributions", "Scientific advances", "Social reforms", "Artistic works");
    }


    private int determineEra(String context) {
        String lowerContext = context.toLowerCase();

        // Try to extract year
        java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("\\b(1[5-9]\\d{2}|20[0-2]\\d)\\b");
        java.util.regex.Matcher matcher = yearPattern.matcher(context);

        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            if (year < 1714) return 1; // Before Georgian
            if (year < 1837) return 1; // Georgian Era
            if (year < 1901) return 0; // Victorian Era
            if (year < 1910) return 2; // Edwardian Era
            return 3; // Modern Era
        }

        // Fallback to keyword matching
        if (lowerContext.contains("victorian") || lowerContext.contains("19th century")) return 0;
        if (lowerContext.contains("georgian") || lowerContext.contains("18th century")) return 1;
        if (lowerContext.contains("edwardian")) return 2;

        return 3; // Default to Modern
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
