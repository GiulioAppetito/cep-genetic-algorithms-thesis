package grammar;

import grammar.datatypes.GrammarTypes;
import grammar.utils.CSVTypesExtractor;
import grammar.utils.GrammarBuilder;
import grammar.utils.GrammarFileWriter;
import grammar.datatypes.DataTypesEnum;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GrammarGenerator {

    public static void generateGrammar(String csvFilePath, String grammarFilePath, GrammarTypes grammarType) throws IOException {
        Map<String, DataTypesEnum> columnTypes = CSVTypesExtractor.getColumnTypesFromCSV(csvFilePath);
        List<DataTypesEnum> uniqueColumnTypes = columnTypes.values().stream().toList();
        Map<String, Set<String>> uniqueStringValues = CSVTypesExtractor.inferUniqueStringValues(csvFilePath, columnTypes);

        String grammar = null;
        try {
            grammar = GrammarBuilder.buildGrammar(columnTypes, uniqueStringValues, uniqueColumnTypes, grammarType, csvFilePath);
        } catch (Exception e) {
            System.out.println("Error while generating grammar: " + e.getMessage());
            throw new RuntimeException(e);
        }
        GrammarFileWriter.writeToFile(grammar, grammarFilePath);
    }
}
