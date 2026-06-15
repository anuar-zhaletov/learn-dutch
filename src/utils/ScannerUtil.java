package utils;

import records.LineRange;
import records.WordRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class ScannerUtil {
    private static final Pattern RANGE = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");

    private ScannerUtil() {

    }

    public static List<WordRow> loadRows(List<String> lines, int from, int to) {
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

    public static LineRange askLineRange(Scanner scanner, int maxLine) {
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
}
