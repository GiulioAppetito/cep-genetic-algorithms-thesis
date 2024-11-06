# Complex Event Processing with Genetic Algorithms

This project utilizes genetic algorithms to generate and optimize event patterns for real-time streaming data using Apache Flink. The system enables the creation of custom event patterns, transformation of patterns into Flink-compatible rules, and the evaluation of how well detected patterns match predefined target sequences.

## Project Structure
- **`app.Main`**: Main entry point; generates grammar, creates patterns, and calculates fitness.
- **`cep.TargetSequencesGenerator`**: Generates target event sequences for testing and evaluation.
- **`events.source.CsvFileEventSource`**: Loads events from a CSV file and represents them as a Flink DataStream.
- **`fitness.FitnessCalculator`**: Evaluates the fitness of patterns by comparing detected sequences against target sequences.
- **`representation.PatternRepresentation`**: Defines the event pattern structure used by the genetic algorithm.
- **`representation.mappers.TreeToRepresentationMapper`**: Maps grammar trees to structured event patterns.
- **`utils.GrammarGenerator`**: Generates a grammar file based on the CSV data structure for pattern creation.
