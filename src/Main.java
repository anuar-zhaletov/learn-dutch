import java.nio.file.Files;
import java.nio.file.Path;

void main() throws Exception {
    try (var lines = Files.lines(Path.of("resources/words-working.csv"))) {
        lines.forEach(System.out::println);
    }
}
