package utils;

import java.util.Set;
import java.util.regex.Pattern;

public class ProfanityFilter {

    // Simple list of profane words to mask. Extend as needed.
    private static final Set<String> BAD_WORDS = Set.of(
            "damn",
            "shit",
            "fuck",
            "bastard",
            "asshole",
            "crap",
            "hell",
            "badword"
    );

    // Replace any occurrence of a bad word (word boundaries, case-insensitive) with fixed ****
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) return input;

        String result = input;
        for (String w : BAD_WORDS) {
            String pattern = "(?i)\\b" + Pattern.quote(w) + "\\b";
            result = result.replaceAll(pattern, "****");
        }
        return result;
    }
}
