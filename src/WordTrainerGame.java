import records.WordRow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import static utils.ScannerUtil.askLineRange;
import static utils.ScannerUtil.loadRows;
import static utils.StringUtil.normalize;

public final class WordTrainerGame {
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

    public static void play(Mode mode, String sourceFileName, String failedFileName) throws Exception {
        var csvPath = Path.of("resources/" + sourceFileName);
        if (!Files.exists(csvPath)) {
            System.out.println("CSV file not found: " + csvPath.toAbsolutePath());
            return;
        }

        var lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            System.out.println("CSV has no data rows.");
            return;
        }

        try (var scanner = new Scanner(System.in)) {
            var range = askLineRange(scanner, lines.size());
            var rows = loadRows(lines, range.from(), range.to());

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

                if (normalize(answer).equals(normalize(expected))) {
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

            var failedCsvPath = Path.of("resources/" + failedFileName);
            Files.writeString(failedCsvPath, String.join(System.lineSeparator(), failedRows));
            System.out.println("Failed rows saved to: " + failedCsvPath.toAbsolutePath());
        }
    }
}
