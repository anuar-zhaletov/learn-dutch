import records.WordRow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import static utils.ScannerUtil.askLineRange;
import static utils.ScannerUtil.loadRows;
import static utils.StringUtil.normalize;

public final class WordTrainerGame {
    private static final String SOURCE_FILE_NAME = "words-learning.csv";
    private static final String FAILED_FILE_NAME = "words-failed.csv";

    private enum SessionMode {
        NEW_WORDS_LEARNING(false),
        FAILED_WORDS_LEARNING(true);

        private final boolean deleteFailedFileWhenNoFailed;

        SessionMode(boolean deleteFailedFileWhenNoFailed) {
            this.deleteFailedFileWhenNoFailed = deleteFailedFileWhenNoFailed;
        }
    }

    public enum Mode {
        DUTCH_TO_ENGLISH("Dutch", "English", "word", WordRow::dutchWord, WordRow::englishWord, false),
        ENGLISH_TO_DUTCH("English", "Dutch", "word", WordRow::englishWord, WordRow::dutchWord, false),
        PHRASE_DUTCH_TO_ENGLISH("Dutch", "English", "phrase", WordRow::dutchPhrase, WordRow::englishPhrase, true),
        PHRASE_ENGLISH_TO_DUTCH("English", "Dutch", "phrase", WordRow::englishPhrase, WordRow::dutchPhrase, true);

        private final String promptLanguage;
        private final String answerLanguage;
        private final String promptUnit;
        private final java.util.function.Function<WordRow, String> promptWord;
        private final java.util.function.Function<WordRow, String> expectedWord;
        private final boolean phraseMode;

        Mode(
                String promptLanguage,
                String answerLanguage,
                String promptUnit,
                java.util.function.Function<WordRow, String> promptWord,
                java.util.function.Function<WordRow, String> expectedWord,
                boolean phraseMode
        ) {
            this.promptLanguage = promptLanguage;
            this.answerLanguage = answerLanguage;
            this.promptUnit = promptUnit;
            this.promptWord = promptWord;
            this.expectedWord = expectedWord;
            this.phraseMode = phraseMode;
        }
    }

    private WordTrainerGame() {

    }

    public static void playFromConsole() throws Exception {
        try (var scanner = new Scanner(System.in)) {
            var sessionMode = askSessionMode(scanner);
            var mode = askMode(scanner);
            var selectedSourceFileName = sessionMode == SessionMode.NEW_WORDS_LEARNING
                    ? SOURCE_FILE_NAME
                    : FAILED_FILE_NAME;

            play(mode, selectedSourceFileName, sessionMode.deleteFailedFileWhenNoFailed, scanner);
        }
    }

    public static void play(Mode mode) throws Exception {
        try (var scanner = new Scanner(System.in)) {
            play(mode, SOURCE_FILE_NAME, false, scanner);
        }
    }

    private static void play(
            Mode mode,
            String sourceFileName,
            boolean deleteFailedFileWhenNoFailed,
            Scanner scanner
    ) throws Exception {
        var csvPath = Path.of("resources/" + sourceFileName);
        if (!Files.exists(csvPath)) {
            if (FAILED_FILE_NAME.equals(sourceFileName)) {
                System.out.println("There are no failed words to train right now. Nice progress - keep going with new words!");
            } else {
                System.out.println("CSV file not found: " + csvPath.toAbsolutePath());
            }
            return;
        }

        var lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            System.out.println("CSV has no data rows.");
            return;
        }

        var isFailedWordsLearning = FAILED_FILE_NAME.equals(sourceFileName);
        ArrayList<WordRow> rows;
        if (isFailedWordsLearning) {
            rows = new ArrayList<>(loadRows(lines, 1, lines.size()));
        } else {
            var range = askLineRange(scanner, lines.size());
            rows = new ArrayList<>(loadRows(lines, range.from(), range.to()));
        }

        if (rows.isEmpty()) {
            System.out.println("No valid rows found in that range.");
            return;
        }

        System.out.printf("%nLoaded %d row(s). Let's train!%n", rows.size());

        var correct = 0;
        var failedRows = new ArrayList<String>();
        for (var i = 0; i < rows.size(); i++) {
            var row = rows.get(i);
            var prompt = mode.promptWord.apply(row);
            var expected = mode.expectedWord.apply(row);

            System.out.printf("%n[%d/%d] %s %s: %s%n", i + 1, rows.size(), mode.promptLanguage, mode.promptUnit, prompt);
            var answerUnit = mode.phraseMode ? "phrase" : "translation";
            System.out.printf("%s %s: ", mode.answerLanguage, answerUnit);
            var answer = scanner.nextLine().trim();

            if (normalize(answer).equalsIgnoreCase(normalize(expected))) {
                correct++;
                System.out.println("Success");
            } else {
                failedRows.add(lines.get(row.lineNumber() - 1));
                System.out.println("Failed (correct: " + expected + ")");
            }

            if (!mode.phraseMode) {
                System.out.println("Dutch phrase  : " + row.dutchPhrase());
                System.out.println("English phrase: " + row.englishPhrase());
            }
        }

        System.out.printf("%nDone. Score: %d/%d correct.%n", correct, rows.size());

        var failedCsvPath = Path.of("resources/" + FAILED_FILE_NAME);
        if (!failedRows.isEmpty()) {
            Files.writeString(failedCsvPath, String.join(System.lineSeparator(), failedRows));
            System.out.println("Failed rows saved, you can train them now.");
        } else if (deleteFailedFileWhenNoFailed && Files.exists(failedCsvPath)) {
            Files.delete(failedCsvPath);
            System.out.println("No failed rows left. Go to next level or words.");
        }
    }

    private static SessionMode askSessionMode(Scanner scanner) {
        while (true) {
            System.out.println("Choose source mode:");
            System.out.println("1) NEW_WORDS_LEARNING");
            System.out.println("2) FAILED_WORDS_LEARNING");
            System.out.print("Enter number: ");

            var input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                return SessionMode.NEW_WORDS_LEARNING;
            }
            if ("2".equals(input)) {
                return SessionMode.FAILED_WORDS_LEARNING;
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private static Mode askMode(Scanner scanner) {
        var modes = Mode.values();
        while (true) {
            System.out.println("Choose training mode:");
            for (var i = 0; i < modes.length; i++) {
                var mode = modes[i];
                System.out.printf("%d) %s %s -> %s %s%n",
                        i + 1,
                        mode.promptLanguage,
                        mode.promptUnit,
                        mode.answerLanguage,
                        mode.promptUnit
                );
            }
            System.out.print("Enter number: ");
            var input = scanner.nextLine().trim();

            try {
                var choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= modes.length) {
                    return modes[choice - 1];
                }
            } catch (NumberFormatException ignored) {
                // Keep asking until a valid numeric option is entered.
            }

            System.out.println("Invalid choice. Try again.");
        }
    }
}
