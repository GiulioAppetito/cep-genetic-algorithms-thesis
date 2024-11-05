# Complex Event Processing with Genetic Algorithms

This project utilizes genetic algorithms to generate and optimize event patterns for real-time streaming data using Apache Flink. The system enables the creation of custom event patterns, transformation of patterns into Flink-compatible rules, and the evaluation of how well detected patterns match predefined target sequences.

## Features

- **Grammar Generation**: Generates a custom grammar from a CSV file to structure event patterns.
- **Pattern Mapping**: Converts generated patterns into Flink-compatible patterns for execution.
- **Fitness Calculation**: Evaluates the accuracy of detected sequences by comparing them to target sequences.
- **Flink CEP Integration**: Real-time processing and detection of event patterns within a streaming environment.

## Setup

### Prerequisites
- **Java JDK 17+**
- **Apache Maven**
- **Apache Flink 1.19.0**

### Installation

1. Clone the repository and navigate to the project directory:
   
   ```bash
   git clone https://github.com/yourusername/cep-genetic-algorithms.git
   cd cep-genetic-algorithms
   ```

2. Build the project using Maven:
   
   ```bash
   mvn clean install
   ```

3. Configure `config.properties` (located in `src/main/resources`) to set paths and parameters, such as:
   - `datasetDirPath` and `csvFileName` for the dataset.
   - `grammarDirPath` and `grammarFileName` for the grammar file.
   - `targetDatasetPath` for target sequences.

## Usage

### 1. Generate Grammar from CSV
Generate grammar rules based on a CSV dataset:

```bash
mvn exec:java -Dexec.mainClass="utils.GrammarGenerator"
```

### 2. Generate and Map Patterns
Run the main program to create patterns and map them into Flink-compatible formats:

```bash
mvn exec:java -Dexec.mainClass="app.Main"
```

### 3. Generate Target Sequences (Optional)
To generate target sequences based on specific patterns:

```bash
mvn exec:java -Dexec.mainClass="cep.TargetSequencesGenerator"
```

## Project Structure
- **`app.Main`**: Main entry point; generates grammar, creates patterns, and calculates fitness.
- **`cep.TargetSequencesGenerator`**: Generates target event sequences for testing and evaluation.
- **`events.source.CsvFileEventSource`**: Loads events from a CSV file and represents them as a Flink DataStream.
- **`fitness.FitnessCalculator`**: Evaluates the fitness of patterns by comparing detected sequences against target sequences.
- **`representation.PatternRepresentation`**: Defines the event pattern structure used by the genetic algorithm.
- **`representation.mappers.TreeToRepresentationMapper`**: Maps grammar trees to structured event patterns.
- **`utils.GrammarGenerator`**: Generates a grammar file based on the CSV data structure for pattern creation.
