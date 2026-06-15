import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static utils.ScannerUtil.askLineRange;
import static utils.ScannerUtil.loadRows;
import static utils.StringUtil.normalize;

public final class Game1 {
    private Game1() {

    }

    public static void play() throws Exception {
        var csvPath = Path.of("resources/words-working.csv");
        if (!Files.exists(csvPath)) {
            System.out.println("CSV file not found: " + csvPath.toAbsolutePath());
            return;
        }

        var lines = Files.readAllLines(csvPath);
        if (lines.size() < 2) {
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
            for (var i = 0; i < rows.size(); i++) {
                var row = rows.get(i);
                System.out.printf("%n[%d/%d] Dutch word: %s%n", i + 1, rows.size(), row.dutchWord());
                System.out.print("English translation: ");
                var answer = scanner.nextLine().trim();

                if (normalize(answer).equals(normalize(row.englishWord()))) {
                    correct++;
                    System.out.println("Success");
                } else {
                    System.out.println("Failed (correct: " + row.englishWord() + ")");
                }

                System.out.println("Dutch phrase  : " + row.dutchPhrase());
                System.out.println("English phrase: " + row.englishPhrase());
            }

            System.out.printf("%nDone. Score: %d/%d correct.%n", correct, rows.size());
        }
    }
}
