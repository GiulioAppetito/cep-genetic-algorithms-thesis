package fitness;

import events.engineering.BaseEvent;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FitnessCalculator {

    private static final String TARGET_DATASET_PATH = "Flink-cep-examples-main/src/main/resources/datasets/target/targetDataset.csv";

    public static double calculateFitness(
            StreamExecutionEnvironment env,
            DataStream<BaseEvent> inputDataStream,
            Pattern<BaseEvent, ?> generatedPattern) throws Exception {

        // Read target sequences from file (precomputed target matches)
        Set<List<Map<String, Object>>> loadedTargetSequences = readTargetSequencesFromFile(TARGET_DATASET_PATH);

        // Find detected sequences from the generated pattern
        Set<List<Map<String, Object>>> detectedSequences = collectSequenceMatches(inputDataStream, Collections.singletonList(generatedPattern), "Generated");

        // Calculate fitness as the percentage of target sequences detected
        return calculateFitnessScore(loadedTargetSequences, detectedSequences);
    }

    private static Set<List<Map<String, Object>>> readTargetSequencesFromFile(String filePath) {
        Set<List<Map<String, Object>>> sequences = new HashSet<>();
        try (Scanner scanner = new Scanner(new java.io.File(filePath))) {
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
            eventString = eventString.replace("{", "").replace("}", "").replace(";", ",");
            String[] keyValuePairs = eventString.split(",");
            Map<String, Object> eventMap = new HashMap<>();
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    if ("successful_login".equals(key)) {
                        eventMap.put(key, Boolean.parseBoolean(value));
                    } else if ("timestamp".equals(key)) {
                        eventMap.put(key, Long.parseLong(value));
                    } else {
                        eventMap.put(key, value);
                    }
                }
            }
            sequence.add(eventMap);
        }
        return sequence;
    }

    private static Set<List<Map<String, Object>>> collectSequenceMatches(DataStream<BaseEvent> inputDataStream, List<Pattern<BaseEvent, ?>> patterns, String type) throws Exception {
        Set<List<Map<String, Object>>> sequencesSet = new HashSet<>();

        for (Pattern<BaseEvent, ?> pattern : patterns) {
            DataStream<List<BaseEvent>> matchedStream = getMatchedDataStream(inputDataStream, pattern);

            Iterator<List<BaseEvent>> iterator = DataStreamUtils.collect(matchedStream);
            while (iterator.hasNext()) {
                List<BaseEvent> eventsList = iterator.next();

                // Convert the list of events into a list of maps for easier comparison
                List<Map<String, Object>> sequence = new ArrayList<>();
                for (BaseEvent event : eventsList) {
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
        int targetCount = targetSequences.size();
        int detectedTargetCount = (int) targetSequences.stream().filter(targetSeq -> detectedSequences.stream().anyMatch(detectedSeq -> compareSequences(targetSeq, detectedSeq))).count();
        return targetCount == 0 ? 0.0 : (double) detectedTargetCount / targetCount * 100.0;
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
