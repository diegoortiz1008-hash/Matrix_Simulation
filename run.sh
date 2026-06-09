#!/bin/bash
mkdir -p out
javac -d out src/main/java/matrix/*.java && java -cp out matrix.Game
