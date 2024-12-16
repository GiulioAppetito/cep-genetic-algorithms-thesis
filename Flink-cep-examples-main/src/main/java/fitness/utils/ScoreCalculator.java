package fitness.utils;

import representation.PatternRepresentation;
import utils.ColoredText;

import java.util.*;

import static utils.Utils.loadConfig;

public class ScoreCalculator {

    public static double calculateFitnessScore(
            Set<List<Map<String, Object>>> targetSequences,
            Set<List<Map<String, Object>>> detectedSequences,
            PatternRepresentation patternRepresentation,
            double beta) throws Exception {

        Set<String> serializedTargetSequences = serializeSequences(targetSequences);
        Set<String> serializedDetectedSequences = serializeSequences(detectedSequences);

        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

        // Count True Positives and False Negatives
        for (String serializedTarget : serializedTargetSequences) {
            if (serializedDetectedSequences.contains(serializedTarget)) {
                truePositives++;
            } else {
                falseNegatives++;
            }
        }

        // Count False Positives
        for (String serializedDetected : serializedDetectedSequences) {
            if (!serializedTargetSequences.contains(serializedDetected)) {
                falsePositives++;
            }
        }

        // Calculate Precision and Recall
        double precision = (truePositives + falsePositives == 0)
                ? 0.0
                : (double) truePositives / (truePositives + falsePositives);

        double recall = (targetSequences.isEmpty())
                ? 0.0
                : (double) truePositives / (truePositives+falseNegatives);

        // Calculate F-beta-score
        double betaSquared = beta * beta;
        double fBetaScore = (precision + recall == 0.0)
                ? 0.0
                : (1 + betaSquared) * (precision * recall) / ((betaSquared * precision) + recall);

        // Print individual and its fitness
        Properties myConfig = loadConfig("src/main/resources/config.properties");
        String printIndividuals = utils.Utils.getRequiredProperty(myConfig, "printIndividuals");
        if (printIndividuals.equals("true")){
            System.out.println(ColoredText.LIGHT_GRAY + patternRepresentation + ColoredText.RESET + ColoredText.ORANGE + "[ScoreCalculator] Precision: " + precision + ", Recall: " + recall + ", F" + beta + "-Score: " + fBetaScore + "\n" + ColoredText.RESET);
        }
        return fBetaScore;
    }

    private static Set<String> serializeSequences(Set<List<Map<String, Object>>> sequences) {
        Set<String> serializedSet = new HashSet<>();
        for (List<Map<String, Object>> sequence : sequences) {
            serializedSet.add(serializeSequence(sequence));
        }
        return serializedSet;
    }

    private static String serializeSequence(List<Map<String, Object>> sequence) {
        StringBuilder serialized = new StringBuilder();
        for (Map<String, Object> map : sequence) {
            serialized.append(serializeMap(map)).append(";");
        }
        return serialized.toString();
    }

    private static String serializeMap(Map<String, Object> map) {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Number) {
                value = String.format("%.6f", ((Number) value).doubleValue());
            }
            entries.add(key + "=" + value);
        }
        entries.sort(String::compareTo); // Ensure consistent ordering
        return String.join(",", entries);
    }
}
