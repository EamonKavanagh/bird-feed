#!/bin/bash

echo "Running Spark job"

$SPARK_HOME/bin/spark-submit --master spark://172.31.0.46:7077 --class Batch target/bird-feed-1.0-jar-with-dependencies.jar

echo "Finished Spark job"
