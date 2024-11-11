package grammar;

import grammar.utils.CSVTypesExtractor;
import grammar.utils.GrammarFileWriter;
import grammar.utils.GrammarBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class GrammarGenerator {

    public static void generateGrammar(String csvFilePath, String grammarFilePath) throws IOException {
        Map<String, String> columnTypes = CSVTypesExtractor.getColumnTypesFromCSV(csvFilePath);
        Map<String, Set<String>> uniqueStringValues = CSVTypesExtractor.inferUniqueStringValues(csvFilePath, columnTypes);

        String grammar = GrammarBuilder.buildGrammar(columnTypes, uniqueStringValues);
        GrammarFileWriter.writeToFile(grammar, grammarFilePath);
    }
}
