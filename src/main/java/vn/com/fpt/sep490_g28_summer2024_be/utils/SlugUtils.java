package vn.com.fpt.sep490_g28_summer2024_be.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class SlugUtils {
    public String genSlug(String input){
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);

        return Arrays.stream(normalizedString.split("\\s+"))
                .map(word -> word.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""))
                .map(word -> word.replaceAll("[^\\w-]", "").toLowerCase())
                .filter(word -> !word.isEmpty())
                .collect(Collectors.joining("-"));
    }

}
