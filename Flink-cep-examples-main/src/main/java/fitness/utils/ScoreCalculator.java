package fitness.utils;

import representation.PatternRepresentation;
import utils.ColoredText;

import java.util.*;

import fitness.fitnesstypes.FitnessFunctionEnum;

import static utils.Utils.loadConfig;

public class ScoreCalculator {

    public static double calculateFitnessScore(
            FitnessFunctionEnum fitnessFunction,
            Set<List<Map<String, Object>>> targetSequences,
            Set<List<Map<String, Object>>> detectedSequences,
            PatternRepresentation patternRepresentation,
            double beta) throws Exception {

        Set<String> serializedTargetSequences = serializeSequences(targetSequences);
        Set<String> serializedDetectedSequences = serializeSequences(detectedSequences);

        switch (fitnessFunction) {
            case FBETA:
                return calculateFBeta(serializedDetectedSequences, serializedTargetSequences, targetSequences, beta, patternRepresentation);
            case BINARYEVENTS:
                return calculateBinaryFBeta(serializedDetectedSequences, serializedTargetSequences, targetSequences,beta, patternRepresentation);
            default:
                return 0.0;
        }
    }

    private static double calculateBinaryFBeta(
        Set<String> serializedDetectedSequences,
        Set<String> serializedTargetSequences,
        Set<List<Map<String, Object>>> targetSequences,
        double beta,
        PatternRepresentation patternRepresentation) {

    int truePositives = 0;
    int falsePositives = 0;
    int falseNegatives = 0;

    // Creazione di un insieme di eventi target e rilevati
    Set<String> allTargetEvents = extractEventsFromSequences(targetSequences);
    Set<String> allDetectedEvents = new HashSet<>();

    for (String serializedDetected : serializedDetectedSequences) {
        allDetectedEvents.addAll(extractEventsFromSerializedSequence(serializedDetected));
    }

    // Calcolo TP e FN
    for (String event : allTargetEvents) {
        if (allDetectedEvents.contains(event)) {
            truePositives++;
        } else {
            falseNegatives++;
        }
    }

    // Calcolo FP
    for (String event : allDetectedEvents) {
        if (!allTargetEvents.contains(event)) {
            falsePositives++;
        }
    }

    // Calcolo Precision e Recall
    double precision = (truePositives + falsePositives == 0) ? 0.0 : (double) truePositives / (truePositives + falsePositives);
    double recall = (truePositives + falseNegatives == 0) ? 0.0 : (double) truePositives / (truePositives + falseNegatives);

    // Calcolo F-beta score
    double betaSquared = beta * beta;
    double fBetaScore = (precision + recall == 0.0)
            ? 0.0
            : (1 + betaSquared) * (precision * recall) / ((betaSquared * precision) + recall);

    // Stampa risultati se richiesto
    String configPath = System.getenv("CONFIG_PATH");
    Properties myConfig = null;
    try {
        myConfig = loadConfig(configPath);
    } catch (Exception e) {
        e.printStackTrace();
    }
    String printIndividuals = utils.Utils.getRequiredProperty(myConfig, "printIndividuals");
    if (printIndividuals.equals("true")) {
        System.out.println(ColoredText.LIGHT_GRAY + patternRepresentation + ColoredText.RESET + ColoredText.ORANGE
                + "[Binary ScoreCalculator] Precision: " + precision + ", Recall: " + recall + ", F" + beta + "-Score: "
                + fBetaScore + "\n" + ColoredText.RESET);
    }

    return fBetaScore;
}

/**
 * Estrae tutti gli eventi dalle sequenze target.
 */
private static Set<String> extractEventsFromSequences(Set<List<Map<String, Object>>> sequences) {
    Set<String> events = new HashSet<>();
    for (List<Map<String, Object>> sequence : sequences) {
        for (Map<String, Object> event : sequence) {
            events.add(serializeMap(event));
        }
    }
    return events;
}

/**
 * Estrae gli eventi da una sequenza serializzata.
 */
private static Set<String> extractEventsFromSerializedSequence(String serializedSequence) {
    Set<String> events = new HashSet<>(Arrays.asList(serializedSequence.split(";")));
    return events;
}


/**
 * Converts event sequences into a serialized string format that maintains order.
 */
    private static Map<String, String> mapSequencesToEventString(Set<String> sequences) {
    Map<String, String> eventStrings = new HashMap<>();
    int index = 0;
    for (String sequence : sequences) {
        eventStrings.put("Seq" + index, sequence);
        index++;
    }
    return eventStrings;
}

    private static double calculateFBeta(
            Set<String> serializedDetectedSequences,
            Set<String> serializedTargetSequences,
            Set<List<Map<String, Object>>> targetSequences,
            double beta,
            PatternRepresentation patternRepresentation) {
        
        // Count True Positives and False Negatives
        int truePositives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;

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
                : (double) truePositives / (truePositives + falseNegatives);

        // Calculate F-beta-score
        double betaSquared = beta * beta;
        double fBetaScore = (precision + recall == 0.0)
                ? 0.0
                : (1 + betaSquared) * (precision * recall) / ((betaSquared * precision) + recall);

        // Print individual and its fitness
        String configPath = System.getenv("CONFIG_PATH");
        Properties myConfig = null;
        try {
            myConfig = loadConfig(configPath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String printIndividuals = utils.Utils.getRequiredProperty(myConfig, "printIndividuals");
        if (printIndividuals.equals("true")) {
            System.out.println(ColoredText.LIGHT_GRAY + patternRepresentation + ColoredText.RESET + ColoredText.ORANGE
                    + "[ScoreCalculator] Precision: " + precision + ", Recall: " + recall + ", F" + beta + "-Score: "
                    + fBetaScore + "\n" + ColoredText.RESET);
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
