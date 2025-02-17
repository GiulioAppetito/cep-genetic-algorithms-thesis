# EA-CEP-Based Event Sequence Matching Framework (Dockerized)

## 🚀 Overview

This repository provides a framework for **pattern-based event sequence matching** and **pattern inference**.  
It leverages **Apache Flink CEP** for event processing and **JGEA (Java Genetic Evolutionary Algorithm)** for evolutionary computation.  
---

## 📌 Features

✅ **Grammar-Based Pattern Representation**: Define custom grammars for pattern matching.  
✅ **Target Sequence Generator**: Generate target sequences from event streams based on conditions.  
✅ **Fitness Evaluation**: Evaluate pattern matching accuracy using precision, recall, and Fβ scores.  
✅ **Genetic Algorithm Integration**: Optimize patterns using an evolutionary approach.  
✅ **Apache Flink Integration**: Scalable and distributed event processing.  
✅ **Full Docker Support**: Easily deploy and manage the architecture using Docker.  

---

## ⚙️ Configuration

### **🔹 Flink Configuration (`flink-conf.yaml`)**
This file contains Flink memory settings and execution parameters.

```properties
jobmanager.memory.process.size: 2048m
taskmanager.memory.process.size: 3072m
taskmanager.numberOfTaskSlots: 16
parallelism.default: 4
execution.checkpointing.timeout: 60000
```

## 📑 Experiment Configuration (`config.properties`)

Before running experiments, you must configure the `config.properties` file to set up **event datasets, grammars, and target patterns**.

### 🔧 Key Configuration Options

```properties
# Path to the source datasets directory
datasetDirPath=/workspace/src/main/resources/datasets/sources/

# Name of the CSV file containing sensor data
csvFileName=odysseus-sshd-processed-simple.csv

# Path where the generated grammar files will be stored
grammarDirPath=/workspace/src/main/resources/grammars/generated/

# Name of the file for storing the generated grammar
grammarFileName=generatedGrammar.bnf

# Path to the target dataset (CEP pattern matching results)
targetDatasetPath=/workspace/src/main/resources/datasets/target/targetDataset.csv

# Grammar Type Options:
# UNBOUNDED                   - No limit on any parameter
# BOUNDED_DURATION            - Limits duration to dataset duration (from first to last record)
# BOUNDED_KEY_BY              - Limits "key_by" operation to specific attributes
# BOUNDED_DURATION_AND_KEY_BY - Both duration and key_by are limited
grammarType = BOUNDED_DURATION_AND_KEY_BY

# Attribute to use for "key_by" in grammar (Only used for BOUNDED_KEY_BY or BOUNDED_DURATION_AND_KEY_BY)
keyByField=ip_address

# Target pattern matching configurations
targetKeyByField=
targetWithinWindowSeconds = 10
targetFromTimes = 5
targetToTimes = 10

# Enable or disable printing of generated individuals (true/false)
printIndividuals = true

# Attributes used for conditions in event matching (leave empty for all)
conditionAttributes=successful_login

# AfterMatchSkipStrategies for Target and Detected patterns, options:
# noSkip | skipToNext | skipPastLastEvent
targetStrategy = skipToNext
individualStrategy = skipToNext
```
## 🐳 Running with Docker

### **1️⃣ Prerequisites**
- Install [Docker](https://www.docker.com/)
- Install [Docker Compose](https://docs.docker.com/compose/)

### **2️⃣ Start the Cluster**
Run the following command from the project root:
```sh
./scripts/manage-architecture.sh --start
```
This starts:
- **JobManager** (Flink job manager)
- **TaskManagers** (Flink workers)
- **Application container**

You can check running containers with:
```sh
docker ps
```

### **3️⃣ Run the Flink Application**
Execute the Flink job with configurable parameters:
```sh
./scripts/run-experiment.sh <nr> <nt>
```
Example:
```sh
./scripts/run-experiment.sh 2 16
```

### **4️⃣ Stop the Cluster**
To stop all running containers:
```sh
./scripts/manage-architecture.sh --stop
```

---

## 🔬 Workflow

1. **Define Target Patterns**:  
   Use `TargetSequencesGenerator` to specify event sequence patterns.
2. **Generate Grammar**:  
   Use `GrammarGenerator` to define grammar constraints.
3. **Run Experiments**:  
   Execute the genetic algorithm using `experiment.txt` settings.
4. **Evaluate Fitness**:  
   Assess results using precision, recall, and fitness scores.

---

## 🔬 Experiment Configuration (`experiment.txt`)

This file controls the evolutionary process and genetic algorithm parameters.

Example:
```text
$nEvals = [5000]

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
    ea.l.bestCsv(
      path = "../RESULTS/{name}/{startTime}/cep-best.csv";
      functions = [
        ea.f.size(of = ea.f.genotype(of = ea.f.best()));
        ea.f.quality(of = ea.f.best());
        ea.f.genotype(of = ea.f.best())
      ]
    );
    ea.l.savePlotForExp(
      path = "../RESULTS/{name}/{startTime}/bestFitness";
      plot = ea.plot.multi.quality(x=ea.f.nOfEvals())
    );
    ea.l.savePlotForExp(
      path = "../RESULTS/{name}/{startTime}/bestSize";
      plot = ea.plot.multi.xyExp(x=ea.f.nOfEvals(); y=ea.f.size(of = ea.f.genotype(of = ea.f.best())))
    )
  ]
)
```

## 📚 References
- [Apache Flink](https://flink.apache.org/)
- [JGEA Library](https://github.com/ericmedvet/jgea)
