package fitness;

import events.BaseEvent;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static app.Main.loadConfig;

public class FitnessCalculator {

    public static double calculateFitness(StreamExecutionEnvironment env, DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> generatedPattern) throws Exception {

        // Load configuration properties from config.properties file
        Properties config = loadConfig("config.properties");

        // Path for CSV
        String TARGET_DATASET_PATH = config.getProperty("targetDatasetPath");

        // Read target sequences from file (precomputed target matches)
        Set<List<Map<String, Object>>> loadedTargetSequences = readTargetSequencesFromFile(TARGET_DATASET_PATH);

        // Find detected sequences from the generated pattern
        Set<List<Map<String, Object>>> detectedSequences = collectSequenceMatches(inputDataStream, Collections.singletonList(generatedPattern), "Generated");

        // Calculate fitness as the percentage of target sequences detected
        return calculateFitnessScore(loadedTargetSequences, detectedSequences);
    }

    private static Set<List<Map<String, Object>>> readTargetSequencesFromFile(String filePath) {
        Set<List<Map<String, Object>>> sequences = new HashSet<>();
        try (Scanner scanner = new Scanner(new File(filePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sequences.add(parseCsvLineToSequence(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sequences;
    }

    private static List<Map<String, Object>> parseCsvLineToSequence(String line) {
        List<Map<String, Object>> sequence = new ArrayList<>();
        String[] eventStrings = line.split("\\|");
        for (String eventString : eventStrings) {
            Map<String, Object> eventMap = new HashMap<>();
            String[] keyValuePairs = eventString.replace("{", "").replace("}", "").replace(";", ",").split(",");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    eventMap.put(key, inferValueType(value));
                }
            }
            sequence.add(eventMap);
        }
        return sequence;
    }

    // Infer the type of the value: boolean, long, or string
    private static Object inferValueType(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // Not a long, continue checking other types
        }
        return value; // Default to string if no other type matches
    }

    private static Set<List<Map<String, Object>>> collectSequenceMatches(DataStream<BaseEvent> inputDataStream, List<Pattern<BaseEvent, ?>> patterns, String type) throws Exception {
        Set<List<Map<String, Object>>> sequencesSet = new HashSet<>();

        for (Pattern<BaseEvent, ?> pattern : patterns) {
            DataStream<List<BaseEvent>> matchedStream = getMatchedDataStream(inputDataStream, pattern);

            Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
            while (iterator.hasNext()) {
                List<Map<String, Object>> sequence = new ArrayList<>();
                for (BaseEvent event : iterator.next()) {
                    sequence.add(new HashMap<>(event.toMap()));
                }
                sequencesSet.add(sequence);
                System.out.println("[" + type + "] match sequence: " + sequence);
            }
        }

        return sequencesSet;
    }

    private static DataStream<List<BaseEvent>> getMatchedDataStream(DataStream<BaseEvent> inputDataStream, Pattern<BaseEvent, ?> pattern) {
        PatternStream<BaseEvent> patternStream = CEP.pattern(inputDataStream, pattern);
        return patternStream.select(new PatternToListSelectFunction());
    }

    private static double calculateFitnessScore(Set<List<Map<String, Object>>> targetSequences, Set<List<Map<String, Object>>> detectedSequences) {
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
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((entry1, entry2) -> entry1 + ";" + entry2)
                .orElse("");
    }

    private static class PatternToListSelectFunction implements PatternSelectFunction<BaseEvent, List<BaseEvent>> {
        @Override
        public List<BaseEvent> select(Map<String, List<BaseEvent>> match) {
            List<BaseEvent> collectedEvents = new ArrayList<>();
            match.values().forEach(collectedEvents::addAll);
            return collectedEvents;
        }
    }
}
