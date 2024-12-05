package fitness.utils;

import representation.PatternRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ScoreCalculator {

    public static double calculateFitnessScore(
            Set<List<Map<String, Object>>> targetSequences,
            Set<List<Map<String, Object>>> detectedSequences,
            PatternRepresentation patternRepresentation) {

        long detectedTargetCount = 0;

        for (List<Map<String, Object>> targetSeq : targetSequences) {
            for (List<Map<String, Object>> detectedSeq : detectedSequences) {
                if (compareSequences(targetSeq, detectedSeq)) {
                    detectedTargetCount++;
                    break;
                }
            }
        }

        double fitnessScore = targetSequences.isEmpty()
                ? 0.0
                : (double) detectedTargetCount / targetSequences.size() * 100.0;

        System.out.println("[ScoreCalculator] Fitness score: " + fitnessScore+" \n "+patternRepresentation);
        return fitnessScore;
    }

    private static boolean compareSequences(List<Map<String, Object>> seq1, List<Map<String, Object>> seq2) {
        if (seq1.size() != seq2.size()) {
            return false;
        }

        for (int i = 0; i < seq1.size(); i++) {
            Map<String, Object> map1 = seq1.get(i);
            Map<String, Object> map2 = seq2.get(i);

            if (!compareMaps(map1, map2)) {
                return false;
            }
        }

        return true;
    }

    private static boolean compareMaps(Map<String, Object> map1, Map<String, Object> map2) {
        if (map1.size() != map2.size()) return false;

        for (String key : map1.keySet()) {
            Object val1 = map1.get(key);
            Object val2 = map2.get(key);

            if (val1 instanceof Number && val2 instanceof Number) {
                if (!compareDoubles(((Number) val1).doubleValue(), ((Number) val2).doubleValue(), 6)) {
                    return false;
                }
            } else if (!Objects.equals(val1, val2)) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareDoubles(double d1, double d2, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(d1 * scale) == Math.round(d2 * scale);
    }
}
