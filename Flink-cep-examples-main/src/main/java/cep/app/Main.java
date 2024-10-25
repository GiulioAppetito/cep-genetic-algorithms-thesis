package cep.app;

import cep.events.ExampleEvent;
import cep.events.BaseEvent;
import cep.utils.PatternFitnessEvaluator;
import io.github.ericmedvet.jgea.core.representation.grammar.Grammar;
import io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        StringGrammar<String> g = StringGrammar.load(StringGrammar.class.getResourceAsStream("/equivGrammar.bnf"));
        System.out.println(g);
    }
}
