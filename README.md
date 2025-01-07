# Pattern-Based Event Sequence Matching Framework

## Overview

This repository contains a framework for **pattern-based event sequence matching** and **pattern inference**. It is designed for exploring complex event processing (CEP) patterns over streaming data using genetic algorithms to infer optimal patterns based on a target dataset.

The project leverages **Apache Flink CEP** for event processing and the **JGEA (Java Genetic Evolutionary Algorithm)** library for evolutionary computations. The main goal is to define grammars, infer patterns, and evaluate their fitness against a target dataset.

---

## Features

1. **Grammar-Based Pattern Representation**:
   - Custom grammars for pattern representation.
   - Generation of bounded and unbounded grammars.
2. **Target Sequence Generator**:
   - Extracts target patterns from event streams based on predefined conditions.
3. **Fitness Evaluation**:
   - Matches inferred patterns with target patterns and computes fitness using precision, recall, and Fβ scores.
4. **Genetic Algorithm Integration**:
   - Evolves pattern representations to maximize fitness.
5. **Apache Flink Integration**:
   - Supports high-performance, distributed event processing.

---

## Project Structure

- `cep/`: Contains core CEP-related logic and utilities.
- `events/`: Base classes for defining events and their attributes.
- `fitness/`: Handles fitness evaluation, matching, and scoring.
- `grammar/`: Modules for grammar generation and management.
- `representation/`: Manages pattern representations and conversions.
- `problem/`: Defines the pattern inference problem and quality evaluation.
- `resources/`: Configuration and dataset files.

---

## Configuration

### `config.properties`

Key configurations for running the project.

```properties
# Paths
datasetDirPath=src/main/resources/datasets/sources/
csvFileName=ithaca-sshd-processed-simple.csv
grammarDirPath=src/main/resources/grammars/generated/
grammarFileName=generatedGrammar.bnf
targetDatasetPath=src/main/resources/datasets/target/targetDataset.csv

# Grammar settings
grammarType=BOUNDED_DURATION
keyByField=
targetKeyByField=ip_address

# Print options
printIndividuals=true

# Matching strategies
targetStrategy=skipToNext
individualStrategy=skipToNext
