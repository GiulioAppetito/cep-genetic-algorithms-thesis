package fitness.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreCalculator {

    public static double calculateFitnessScore(Set<List<Map<String, Object>>> targetSequences, Set<List<Map<String, Object>>> detectedSequences) {
        long detectedTargetCount = targetSequences.stream().filter(targetSeq -> detectedSequences.stream().anyMatch(detectedSeq -> compareSequences(targetSeq, detectedSeq))).count();
        return targetSequences.isEmpty() ? 0.0 : (double) detectedTargetCount / targetSequences.size() * 100.0;
    }

    private static boolean compareSequences(List<Map<String, Object>> seq1, List<Map<String, Object>> seq2) {
        if (seq1.size() != seq2.size()) {
            return false;
        }
        for (int i = 0; i < seq1.size(); i++) {
            if (!canonicalizeMap(seq1.get(i)).equals(canonicalizeMap(seq2.get(i)))) {
                return false;
            }
        }
        return true;
    }

    private static String canonicalizeMap(Map<String, Object> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((entry1, entry2) -> entry1 + ";" + entry2)
                .orElse("");
    }
}
