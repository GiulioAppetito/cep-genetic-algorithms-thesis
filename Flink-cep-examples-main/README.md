# FlinkCEP Genetic Algorithm Sandbox

This project is a sandbox for my master thesis. At the moment, no Genetic Algorithms (GA) is used; instead, the goal here is to define a genotype for the solutions and a mapping from genotype to fenotype. 

## Features
- **Genotype representation**: A genotype represents the configuration of a Flink CEP query, involving parameters such as number of failed login attempts, a timeout (window width), and an event skip strategy.
- **Flink CEP query generation**: The genotype is mapped to a Flink CEP query that can detect certain patterns.

## Project Structure
- `Genotype.java`: Defines the genotype, which includes parameters for the CEP query. This class includes a method to generate randomized genotypes (just for testing purposes).
- `FlinkCEPQuery.java`: This represents a Flink CEP query that detects patterns.
- `LoginEvent.java`: Defines the events in the stream, specifically login attempts, including attributes like timestamp, IP address, and success or failure.
