import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Fuzzy compare two strings
 *
 * @autor _drukpakunle_
 */

@Slf4j
public final class StringsFuzzyComparator {

    private static final int ALLOWABLE_ONE_WORD_DISTANCE = 2;

    /**
     * Fuzzy compare two strings
     * The same words are removed from the Strings.
     * After which the Levenshtein distance is calculated over the remaining words.
     * Comparison is made of each word with each with an acceptable distance
     * If the distance is less than or equal to the allowable, such words are also removed from the Strings.
     * After which the Levenshtein distance is calculated over the remaining Strings.
     *
     * @param stringOne         first text
     * @param stringTwo         second text
     * @param allowableDistance the Levenshtein distance (int) at which Strings will still be considered the same (inclusive)
     * @return - true if two strings equals fuzzy
     */
    public static boolean isStringsEqualsFuzzy(String stringOne, String stringTwo, int allowableDistance) {
        List<String> wordsOne = getWordsFromString(stringOne.toLowerCase());
        List<String> wordsTwo = getWordsFromString(stringTwo.toLowerCase());

        List<String> uniqueWordsOne = wordsOne.stream()
                .filter(word -> !wordsTwo.contains(word))
                .collect(Collectors.toList());

        List<String> uniqueWordsTwo = wordsTwo.stream()
                .filter(word -> !wordsOne.contains(word))
                .collect(Collectors.toList());

        log.debug("String 1: '{}'; Words: [{}]; Unique words 1: '{}'", stringOne, wordsOne, uniqueWordsOne);
        log.debug("String 2: '{}'; Words: [{}]; Unique words 2: '{}'", stringTwo, wordsTwo, uniqueWordsOne);

        new HashSet<>(uniqueWordsOne).forEach(wordOne ->
                new HashSet<>(uniqueWordsTwo).forEach(wordTwo -> {
                            int distance = getLevenshteinDistance(wordOne, wordTwo);
                            if (distance <= ALLOWABLE_ONE_WORD_DISTANCE) {
                                uniqueWordsOne.remove(wordOne);
                                uniqueWordsOne.remove(wordTwo);
                                uniqueWordsTwo.remove(wordOne);
                                uniqueWordsTwo.remove(wordTwo);
                            }
                        }
                ));

        String uniqueStringOne = String.join(" ", uniqueWordsOne);
        String uniqueStringTwo = String.join(" ", uniqueWordsTwo);
        int distance = getLevenshteinDistance(uniqueStringOne, uniqueStringTwo);

        log.debug("After compare each to each words in both collections");
        log.debug("String 1: '{}'; Words: [{}]; UniqueString 1: '{}'", stringOne, uniqueWordsOne, uniqueStringOne);
        log.debug("String 2: '{}'; Words: [{}]; UniqueString 2: '{}'", stringTwo, uniqueWordsTwo, uniqueStringTwo);
        log.debug("Levenshtein Distance: {}", distance);

        return distance <= allowableDistance;
    }

    /**
     * Calculating the Levenshtein distance for two Strings
     *
     * @param stringOne text
     * @param stringTwo text to calculate distance
     * @return - distance as int
     */
    private static int getLevenshteinDistance(String stringOne, String stringTwo) {
        int stringOneLength = stringOne.length();
        int stringTwoLength = stringTwo.length();

        int[][] deltaMatrix = new int[stringOneLength + 1][stringTwoLength + 1];

        IntStream.range(1, stringOneLength + 1).forEach(i -> deltaMatrix[i][0] = i);
        IntStream.range(1, stringTwoLength + 1).forEach(j -> deltaMatrix[0][j] = j);

        IntStream.range(1, stringTwoLength + 1).forEach(j ->
                IntStream.range(1, stringOneLength + 1).forEach(i ->
                        deltaMatrix[i][j] = stringOne.charAt(i - 1) == stringTwo.charAt(j - 1)
                                ? deltaMatrix[i][j] = deltaMatrix[i - 1][j - 1]
                                : Math.min(deltaMatrix[i - 1][j] + 1, Math.min(deltaMatrix[i][j - 1] + 1, deltaMatrix[i - 1][j - 1] + 1)
                        )));

        return deltaMatrix[stringOneLength][stringTwoLength];
    }

    /**
     * Converts text to collection of words
     *
     * @param string text
     * @return - List<String> words
     */
    private static List<String> getWordsFromString(String string) {
        return Arrays.asList(string
                .replaceAll("[^\\p{L}\\d\\s]+", "")
                .replaceAll("\\s++", " ")
                .split("\\s"));
    }
}
