package problem;

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;

@Discoverable(prefixTemplate = "tesi.problem")
public class Problems {

    // Private constructor to avoid instantiation
    private Problems() {
    }

    // Create a GrammarBasedProblem
    @SuppressWarnings("unused")
    @Cacheable
    public static PatternInferenceProblem patternInferenceProblem() throws Exception {
        String configPath = "src/main/resources/config.properties";

        return new PatternInferenceProblem("src/main/resources/config.properties");
    }
}