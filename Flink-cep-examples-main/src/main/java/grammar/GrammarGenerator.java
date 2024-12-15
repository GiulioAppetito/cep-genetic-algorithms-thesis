package grammar;

import grammar.datatypes.GrammarTypes;
import grammar.utils.CSVTypesExtractor;
import grammar.utils.GrammarBuilder;
import grammar.utils.GrammarFileWriter;
import grammar.datatypes.DataTypesEnum;
import java.util.*;

import static utils.Utils.loadConfig;

public class GrammarGenerator {

    public static void generateGrammar(String csvFilePath, String grammarFilePath, GrammarTypes grammarType) throws Exception {
        // Carica la configurazione
        Properties myConfig = loadConfig("src/main/resources/config.properties");
        String conditionAttributesConfig = myConfig.getProperty("conditionAttributes", "").trim();
        Set<String> allowedAttributes = new HashSet<>();
        if (!conditionAttributesConfig.isEmpty()) {
            allowedAttributes.addAll(Arrays.asList(conditionAttributesConfig.split(",")));
        }
        System.out.println("[GrammarGenerator] Allowed attributes: " + allowedAttributes);

        // Filtra gli attributi usando allowedAttributes
        Map<String, DataTypesEnum> columnTypes = CSVTypesExtractor.getColumnTypesFromCSV(csvFilePath, allowedAttributes);
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
