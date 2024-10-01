package flinkCEP.cases;

import flinkCEP.events.LoginEvent;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import java.time.Duration;
import java.util.List;

public class CEPExample {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // csv file path
        String inputPath = "C:\\Users\\giuli\\OneDrive\\Desktop\\Tesi\\Datasets\\processed-sshd-logs\\processed-sshd-logs\\athena-sshd-processed-simple.csv";

        // DataStream from the csv
        DataStream<String> csvData;
        csvData = env.readTextFile(inputPath);

        // parsing every row in a LoginEvent object
        DataStream<LoginEvent> loginEventStream = csvData
                .filter(line -> !line.startsWith("timestamp"))  // ignora l'intestazione
                .map((MapFunction<String, LoginEvent>) line -> {
                    String[] fields = line.split(",");
                    long timestamp = Long.parseLong(fields[0]);
                    String ipAddress = fields[1];
                    boolean successfulLogin = Boolean.parseBoolean(fields[2]);
                    return new LoginEvent(timestamp, ipAddress, successfulLogin);
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<LoginEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((SerializableTimestampAssigner<LoginEvent>) (loginEvent, recordTimestamp) -> loginEvent.getTimestamp())
                )
                ;

        // Pattern parameters
        int failedAttempts = 5; // Number of failed attempts in the window
        float timeoutSeconds = 2; // Window interval length in seconds

        // Skip strategy declarations
        AfterMatchSkipStrategy noSkip = AfterMatchSkipStrategy.noSkip();
        AfterMatchSkipStrategy skipToNext = AfterMatchSkipStrategy.skipToNext();
        AfterMatchSkipStrategy skipPastLastEvent = AfterMatchSkipStrategy.skipPastLastEvent();
        AfterMatchSkipStrategy skipToFirst = AfterMatchSkipStrategy.skipToFirst("failures");
        AfterMatchSkipStrategy skipToLast = AfterMatchSkipStrategy.skipToLast("failures");

        // Skip strategy selection
        AfterMatchSkipStrategy skipStrategy = noSkip;

        // Pattern definition
        Pattern<LoginEvent, ?> loginFailPattern = Pattern.<LoginEvent>begin("failures", skipStrategy)
                .where(new SimpleCondition<LoginEvent>() {
                    @Override
                    public boolean filter(LoginEvent event) {
                        return !event.successfulLogin; // Verifica se il login è fallito
                    }
                })
                .timesOrMore(failedAttempts)  // Ripete il pattern per 'failedAttempts' volte
                .within(Time.seconds((long) timeoutSeconds)); // Deve accadere entro 'timeoutSeconds' secondi

        // apply the pattern to data
        PatternStream<LoginEvent> patternStream = CEP.pattern(
                loginEventStream.keyBy(event -> event.ipAddress), // Raggruppa per IP
                loginFailPattern
        );

        // select events matching the pattern
        DataStream<String> alerts = patternStream.select(
                (PatternSelectFunction<LoginEvent, String>) pattern -> {
                    List<LoginEvent> failures = pattern.get("failures"); // Usa la chiave "failures"
                    LoginEvent firstFail = failures.get(0);  // Primo evento (inizio della finestra)
                    LoginEvent lastFail = failures.get(failures.size() - 1);  // Ultimo evento (fine della finestra)

                    float interval = (float) (lastFail.timestamp - firstFail.timestamp) /1000;

                    // returns windows interval
                    return "Failures: " + failures.size() +
                            " | IP: " + firstFail.ipAddress +
                            " | Between " + firstFail.timestamp + " and " + lastFail.timestamp +
                            " | Window elapsed time: "+ interval + " seconds.";
                }
        );
        alerts.print();

        env.execute("CSV Login Fail Detection");
    }
}
