package flinkCEP.genotypes;

import flinkCEP.queries.FlinkCEPQuery;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;

import java.util.Random;

public class Genotype {
    private int failedAttempts;
    private float timeoutSeconds;
    private AfterMatchSkipStrategy afterMatchSkipStrategy;

    public Genotype(int failedAttempts, float timeoutSeconds, AfterMatchSkipStrategy afterMatchSkipStrategy) {
        this.failedAttempts = failedAttempts;
        this.timeoutSeconds = timeoutSeconds;
        this.afterMatchSkipStrategy = afterMatchSkipStrategy;
    }

    //method to create a CEP query starting from a genotype
    public FlinkCEPQuery genotypeToQuery() {
        return new FlinkCEPQuery(failedAttempts, timeoutSeconds, afterMatchSkipStrategy);
    }

    // create a randomized genotype (only for test purposes)
    public static Genotype randomizedGenotype() {
        Random random = new Random();

        //select random parameters
        int failedAttempts = random.nextInt(50) ;
        float timeoutSeconds = random.nextFloat() * 100;

        AfterMatchSkipStrategy[] strategies = {
                AfterMatchSkipStrategy.noSkip(),
                AfterMatchSkipStrategy.skipToNext(),
                AfterMatchSkipStrategy.skipPastLastEvent(),
                AfterMatchSkipStrategy.skipToFirst("failures"),
                AfterMatchSkipStrategy.skipToLast("failures")
        };
        //select a random skip strategy
        AfterMatchSkipStrategy skipStrategy = strategies[random.nextInt(strategies.length)];
        return new Genotype(failedAttempts, timeoutSeconds, skipStrategy);
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public float getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(float timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public AfterMatchSkipStrategy getAfterMatchSkipStrategy() {
        return afterMatchSkipStrategy;
    }

    public void setAfterMatchSkipStrategy(AfterMatchSkipStrategy afterMatchSkipStrategy) {
        this.afterMatchSkipStrategy = afterMatchSkipStrategy;
    }
}
