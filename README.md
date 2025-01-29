```markdown
# EA-CEP-Based Event Sequence Matching Framework (Dockerized)

## 🚀 Overview

This repository provides a framework for **pattern-based event sequence matching** and **pattern inference**.  
It leverages **Apache Flink CEP** for event processing and **JGEA (Java Genetic Evolutionary Algorithm)** for evolutionary computation.  

🔹 **New Feature:** The project is now fully **containerized with Docker and Apache Flink**! 🎉  

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

---

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
./scripts/run-flink-app.sh <nr> <nt>
```
Example:
```sh
./scripts/run-flink-app.sh 2 16
```
This will run:
```sh
java --add-opens java.base/java.util=ALL-UNNAMED \
  -jar /app/app.jar -v -nr 2 -nt 16 -f /app/experiment.txt
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

## 🛠️ Building & Running Manually

If you prefer to run the application manually instead of using Docker:

### **1️⃣ Build the Project**
```sh
mvn clean install
```

### **2️⃣ Run the Application**
```sh
java --add-opens java.base/java.util=ALL-UNNAMED \
  -jar target/flinkCEP
```

---

## 📚 References
- [Apache Flink](https://flink.apache.org/)
- [JGEA Library](https://github.com/ericmedvet/jgea)

---

🚀 **Enjoy your event processing journey with Flink and genetic algorithms!**
```

