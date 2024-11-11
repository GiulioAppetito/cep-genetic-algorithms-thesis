# Complex Event Processing with Genetic Algorithms

This project utilizes genetic algorithms to generate and optimize event patterns for real-time streaming data using Apache Flink. The system enables the creation of custom event patterns, transformation of patterns into Flink-compatible rules, and the evaluation of how well detected patterns match predefined target sequences.

## Project Structure

- **app.Main**: Main entry point; loads configuration, generates grammar from CSV, creates patterns, sets up the Flink environment, and calculates fitness using the `FitnessCalculator`.

- **cep.TargetSequencesGenerator**: Generates target event sequences for testing and evaluation by applying predefined patterns to a stream of events and saving matches to a file.

- **events.source.CsvFileEventSource**: Loads events from a CSV file and represents them as a Flink DataStream, allowing for real-time processing of event data.

- **fitness.FitnessCalculator**: Main fitness evaluation component, which initializes configuration and calculates the fitness of generated patterns against target sequences. It now delegates sequence matching and scoring to `EventSequenceMatcher` and `FitnessScoreCalculator`.

- **fitness.EventSequenceMatcher**: Responsible for applying patterns to the event data stream and collecting matched sequences. This component performs the sequence matching logic for pattern evaluation.

- **fitness.FitnessScoreCalculator**: Calculates the fitness score by comparing detected sequences (generated patterns) against predefined target sequences. Responsible for the scoring logic to determine how well a generated pattern aligns with target patterns.

- **fitness.grammar.EventParser**: Provides helper methods to parse event sequences, convert data formats, and handle any CSV-based sequence parsing required by `FitnessCalculator`.

- **representation.PatternRepresentation**: Defines the event pattern structure used by the genetic algorithm, including event attributes, conditions, and quantifiers.

- **representation.mappers.RepresentationToPatternMapper**: Maps a `PatternRepresentation` object to a Flink CEP `Pattern`. Converts the genetic algorithm’s pattern representation into a Flink-compatible format for real-time event pattern detection.

- **representation.mappers.TreeToRepresentationMapper**: Maps grammar trees to structured event patterns, translating a `Tree<String>` representation of grammar elements into a `PatternRepresentation` used in pattern creation.

- **grammar.GrammarGenerator**: Generates a grammar file based on the CSV data structure for pattern creation. Analyzes the CSV schema and produces a BNF grammar file to guide the genetic algorithm in generating valid event patterns.

