# FlinkCEP Genetic Algorithm Example

This project implements a pattern detection system using Flink CEP (Complex Event Processing) combined with a genetic algorithm approach. The goal is to dynamically generate CEP queries based on a randomized genotype, run these queries on a stream of events, and evaluate their performance using a fitness function.

## Features
- **Genotype representation**: A genotype represents the configuration of a Flink CEP query, including parameters like the number of failed login attempts, a timeout, and an event skip strategy.
- **Flink CEP query generation**: The genotype is mapped to a Flink CEP query that can detect login failure patterns.
- **Fitness evaluation**: A fitness function is used to evaluate how well the generated query performs based on a known dataset.

## Project Structure
- `Genotype.java`: Defines the genotype, which includes parameters for the CEP query. This class includes a method to generate randomized genotypes for testing purposes.
- `FlinkCEPQuery.java`: Represents a Flink CEP query that detects patterns of failed login attempts.
- `FitnessEvaluator.java`: Contains the logic for evaluating the fitness (or quality) of a query. The fitness function will execute the query and compare the results to expected outcomes.
- `LoginEvent.java`: Defines the events in the stream, specifically login attempts, including attributes like timestamp, IP address, and success or failure.
