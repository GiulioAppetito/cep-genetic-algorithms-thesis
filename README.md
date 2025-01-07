
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

This file controls various parameters of the framework. Below is a detailed explanation of its key fields:

```properties
# Paths to required files and directories
datasetDirPath=src/main/resources/datasets/sources/ # Directory containing the source datasets
csvFileName=ithaca-sshd-processed-simple.csv       # Name of the input CSV file with event data
grammarDirPath=src/main/resources/grammars/generated/ # Directory where generated grammars will be saved
grammarFileName=generatedGrammar.bnf              # Name of the grammar file to be generated
targetDatasetPath=src/main/resources/datasets/target/targetDataset.csv # Path to the target dataset

# Grammar settings
grammarType=BOUNDED_DURATION  # Type of grammar. Options:
                              # - UNBOUNDED: No restrictions on patterns.
                              # - BOUNDED_DURATION: Patterns restricted by time duration.
                              # - BOUNDED_KEY_BY: Patterns grouped by a key field.
                              # - BOUNDED_DURATION_AND_KEY_BY: Both restrictions apply.

keyByField=                   # Key field for "group by" operations in detected patterns.
targetKeyByField=ip_address   # Key field for "group by" operations in target patterns.

# Print options
printIndividuals=true         # Print detailed information for individuals. Options: true, false.

# Matching strategies for patterns
targetStrategy=skipToNext     # Strategy for handling overlapping matches in target patterns.
individualStrategy=skipToNext # Strategy for handling overlaps in generated patterns.
```

### Experiment Configuration (`experiments/experiment.txt`)

Defines genetic algorithm parameters. Key settings include:

- **`$nEvals`**: Total number of evaluations.
- **Population size**: Number of individuals in the genetic population (`nPop`).
- **Grammar-based representation**: Configuration for pattern representation and mapping.

Example configuration:

```text
$nEvals = [1000]

ea.experiment(
  runs = (randomGenerator = (seed = [1:1:10]) * [m.defaultRG()]) *
    (solver = (nEval = $nEvals) * [
      ea.s.ga(
        name = "gp";
        nPop = 100;
        representation = ea.r.cfgTree(grammar = ea.grammar.fromProblem(problem = tesi.problem.patternInferenceProblem()));
        mapper = ea.m.grammarTreeBP(problem = tesi.problem.patternInferenceProblem())
      )
    ]) * [
    ea.run(problem = tesi.problem.patternInferenceProblem())
  ];
  listeners = [
    ea.l.console(
      functions = [
        ea.f.size(of = ea.f.genotype(of = ea.f.best()); format = "%3d");
        ea.f.quality(of = ea.f.best(); format = "%8.5f");
        ea.f.hist(of = f.each(of = ea.f.all(); mapF = ea.f.quality()));
        ea.f.solution(of = ea.f.best(); format = "%s")
      ]
    );
    ea.l.savePlotForExp(
      path = "../RESULTS/{name}/{startTime}/fitness";
      plot = ea.plot.multi.quality(x=ea.f.nOfEvals())
    )
  ]
)
```

---

## Workflow

1. **Define Target Patterns**:  
   Use `TargetSequencesGenerator` to specify the target sequences based on event conditions.

2. **Generate Grammar**:  
   Use `GrammarGenerator` to create grammars based on dataset attributes and configuration.

3. **Run Experiments**:  
   Execute experiments with the genetic algorithm defined in `experiment.txt`.

4. **Evaluate Fitness**:  
   Use `FitnessCalculator` to compute the fitness score of inferred patterns.

---

## Setup and Execution

### Prerequisites

- Java 11+
- Maven
- Apache Flink
- Required datasets in the specified paths.

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/cep-pattern-matching
   cd cep-pattern-matching
   ```

2. Configure properties in `config.properties`.

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the project using the following command:
   ```bash
   java --add-opens java.base/java.util=ALL-UNNAMED -Xms10g -Xmx12g -cp "<classpath>" io.github.ericmedvet.jgea.experimenter.Starter -v -nr 1 -nt 10 -f <path_to_experiment_file>
   ```

   - Replace `<classpath>` with your project's full classpath, including all dependencies.
   - Replace `<path_to_experiment_file>` with the path to your experiment configuration file.

   Example:
   ```bash
   java --add-opens java.base/java.util=ALL-UNNAMED -Xms10g -Xmx12g -cp "C:\path\to\dependencies\*.jar;target\flinkCEP-Patterns-0.1.jar" io.github.ericmedvet.jgea.experimenter.Starter -v -nr 1 -nt 10 -f src/main/resources/experiments/experiment.txt
   ```

5. Analyze results in the `RESULTS` directory.

---

## References

- [Apache Flink](https://flink.apache.org/)
- [JGEA Library](https://github.com/ericmedvet/jgea)
