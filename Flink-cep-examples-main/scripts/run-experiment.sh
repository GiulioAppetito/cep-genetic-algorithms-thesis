#!/bin/bash

# Check parameters
if [ $# -lt 2 ]; then
    echo "Usage: $0 <nr> <nt>"
    echo "nr: Number of runs, nt: Number of threads"
    exit 1
fi

# Parametri
NR=$1
NT=$2

echo "🚀 Starting up experiment with nr=$NR, nt=$NT..."

docker exec flink-app java --add-opens java.base/java.util=ALL-UNNAMED \
  -jar /app/app.jar -v -nr "$NR" -nt "$NT" -f /app/experiment.txt

echo "✅ Experiment succeeded!"
