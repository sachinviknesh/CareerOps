package com.careerops.app.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lightweight TF-IDF cosine similarity for offline job-fit text matching.
 * No external libraries required.
 */
@Component
public class TextSimilarityUtil {

    private static final Pattern NON_ALPHA = Pattern.compile("[^a-z0-9\\s]");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "this", "that", "these",
            "those", "it", "its", "we", "you", "i", "as", "up", "if", "not"
    );

    /**
     * Returns cosine similarity [0.0, 1.0] between two texts.
     */
    public double cosineSimilarity(String textA, String textB) {
        if (textA == null || textB == null || textA.isBlank() || textB.isBlank()) return 0.0;

        Map<String, Integer> freqA = termFrequency(tokenize(textA));
        Map<String, Integer> freqB = termFrequency(tokenize(textB));

        Set<String> vocab = new HashSet<>(freqA.keySet());
        vocab.addAll(freqB.keySet());

        if (vocab.isEmpty()) return 0.0;

        double dot = 0, magA = 0, magB = 0;
        for (String term : vocab) {
            int a = freqA.getOrDefault(term, 0);
            int b = freqB.getOrDefault(term, 0);
            dot += (double) a * b;
            magA += (double) a * a;
            magB += (double) b * b;
        }

        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /**
     * Returns terms that appear in textA but not textB.
     */
    public List<String> uniqueTerms(String textA, String textB) {
        Set<String> tokensB = new HashSet<>(tokenize(textB));
        List<String> unique = new ArrayList<>();
        for (String t : tokenize(textA)) {
            if (!tokensB.contains(t) && !unique.contains(t)) {
                unique.add(t);
            }
        }
        return unique;
    }

    public List<String> tokenize(String text) {
        if (text == null) return List.of();
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String lower = NON_ALPHA.matcher(normalized.toLowerCase()).replaceAll(" ");
        return Arrays.stream(lower.split("\\s+"))
                .filter(t -> t.length() > 2 && !STOP_WORDS.contains(t))
                .toList();
    }

    private Map<String, Integer> termFrequency(List<String> tokens) {
        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) freq.merge(t, 1, Integer::sum);
        return freq;
    }
}
