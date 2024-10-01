package flinkCEP.app;

import com.esotericsoftware.kryo.serializers.JavaSerializer;
import flinkCEP.genotypes.Genotype;
import flinkCEP.queries.FlinkCEPQuery;
import flinkCEP.events.LoginEvent;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import java.util.Arrays;
import java.util.Objects;

public class GeneticFlinkQueryExample {

    public static void main(String[] args) throws Exception {
        // csv file path
        String csvFilePath = "C:\\Users\\giuli\\OneDrive\\Desktop\\Tesi\\Datasets\\processed-sshd-logs\\processed-sshd-logs\\athena-sshd-processed-simple.csv";

        // generate a randomized genotype for the query
        Genotype genotype = Genotype.randomizedGenotype();

        // from genotype to fenotype (query)
        FlinkCEPQuery query = genotype.genotypeToQuery();

        // print query params (fenotype)
        System.out.println("Query Parameters:");
        System.out.println("  - Failed Attempts: " + genotype.getFailedAttempts());
        System.out.println("  - Timeout (seconds): " + genotype.getTimeoutSeconds());
        System.out.println("  - Skip Strategy: " + genotype.getAfterMatchSkipStrategy().getClass().getSimpleName());

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setAutoWatermarkInterval(1000);

        // from csv to LoginEvent objects
        DataStream<String> rawLines = env.readTextFile(csvFilePath);
        DataStream<LoginEvent> loginEventStream = rawLines.map(line -> {
                    try {
                        String[] fields = line.split(",");
                        long timestamp = Long.parseLong(fields[0]);
                        String ipAddress = fields[1];
                        boolean successfulLogin = Boolean.parseBoolean(fields[2]);
                        return new LoginEvent(timestamp, ipAddress, successfulLogin);
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line);
                        return null;
                    }
                }).filter(Objects::nonNull)  // Filtra eventuali eventi nulli
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<LoginEvent>forMonotonousTimestamps()
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );

        // execute the query on the datastream
        DataStream<String> queryResult = query.execute(env, loginEventStream);

        // printing query results
        queryResult.map(result -> {
            String[] parts = result.split("\\|");
            String failuresCount = parts[0].trim();
            String ipAddress = parts[1].trim();
            String timeRange = parts[2].trim();

            return String.format(
                    "=== Query Result ===\n" +
                            "Numero di tentativi falliti: %s\n" +
                            "Indirizzo IP: %s\n" +
                            "Intervallo di tempo: %s\n" +
                            "====================\n",
                    failuresCount, ipAddress, timeRange
            );
        }).print();

        env.execute("Example of randomized Queries in Flink CEP");
    }
}