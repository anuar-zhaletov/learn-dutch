import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

private static final Pattern RANGE = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");

void main() throws Exception {
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

LineRange askLineRange(Scanner scanner, int maxLine) {
    while (true) {
        System.out.printf("Enter line range (example 2-10, max line %d): ", maxLine);
        var input = scanner.nextLine().trim();
        var matcher = RANGE.matcher(input);

        if (!matcher.matches()) {
            System.out.println("Invalid format. Use start-end, for example 2-10.");
            continue;
        }

        var from = Integer.parseInt(matcher.group(1));
        var to = Integer.parseInt(matcher.group(2));

        if (from < 2 || to < 2 || from > to || to > maxLine) {
            System.out.println("Invalid bounds. Data rows start at line 2.");
            continue;
        }

        return new LineRange(from, to);
    }
}

List<WordRow> loadRows(List<String> lines, int from, int to) {
    var rows = new ArrayList<WordRow>();
    for (var lineNumber = from; lineNumber <= to; lineNumber++) {
        var raw = lines.get(lineNumber - 1).trim();
        if (raw.isEmpty()) {
            continue;
        }

        var parts = raw.split(",", 4);
        if (parts.length < 4) {
            System.out.println("Skipping incomplete row at line " + lineNumber + ": " + raw);
            continue;
        }

        rows.add(new WordRow(
                lineNumber,
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim()
        ));
    }
    return rows;
}

String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
}

record LineRange(int from, int to) {
}

record WordRow(int lineNumber, String dutchWord, String englishWord, String dutchPhrase, String englishPhrase) {
}
