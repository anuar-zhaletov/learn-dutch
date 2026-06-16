# Dutch Learn Trainer

A small console trainer for learning Dutch words and phrases with repeat modes for mistakes.

## How It Works

The app runs from `src/Main.java` and starts `WordTrainerGame.playFromConsole()`.

You choose **two things** in the console:

1. **Source mode** (what file to train from)
2. **Training mode** (word or phrase direction)

## Source Modes

### 1) NEW_WORDS_LEARNING

- Reads rows from `resources/words-learning.csv`
- Saves mistakes to `resources/words-failed.csv`

### 2) FAILED_WORDS_LEARNING

- Reads rows from `resources/words-failed.csv`
- After session:
  - if there are still mistakes, it overwrites `resources/words-failed.csv` with only new failed rows
  - if there are no mistakes, it deletes `resources/words-failed.csv`

## Training Modes

You choose one of these in the second menu:

1. `DUTCH_TO_ENGLISH` - Dutch word -> English translation
2. `ENGLISH_TO_DUTCH` - English word -> Dutch translation
3. `PHRASE_DUTCH_TO_ENGLISH` - Dutch phrase -> English phrase
4. `PHRASE_ENGLISH_TO_DUTCH` - English phrase -> Dutch phrase

## CSV Format

Each line must have 4 comma-separated values:

```csv
dutchWord,englishWord,dutchPhrase,englishPhrase
```

Example:

```csv
eten,to eat,Ik wil eten.,I want to eat.
```

## Typical Learning Loop

1. Start with **NEW_WORDS_LEARNING**.
2. Train a line range (for example `1-20`).
3. Mistakes are collected into `words-failed.csv`.
4. Repeat using **FAILED_WORDS_LEARNING** until there are no mistakes left.
5. When all are correct, `words-failed.csv` is removed automatically.

## Run

### IntelliJ IDEA (recommended)

- Open `src/Main.java`
- Run the `main` entry point

### Terminal (Java 21+ source-file mode)

```bash
cd "~/IdeaProjects/dutch-learn"
java src/Main.java
```

## Notes

- You can choose a custom line range each session (for example `21-30`).
- Matching is normalized internally (case/spacing tolerant for regular input differences).
- `words-failed.csv` is written without an extra trailing empty line.

