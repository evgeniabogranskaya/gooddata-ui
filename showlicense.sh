#!/bin/sh
set -e

echo "INFO: Building packages and listing licenses"
mvn package -T 1C -DskipTests -P show-licenses -am -pl cfal-restapi

echo "INFO: Grouping licenses into one file and removing duplicate entries."
echo "INFO: Result (also stored in /tmp/cfal-licenses.csv):"
find . -name "licenses.csv" | xargs cat | sort | uniq | tee /tmp/cfal-licenses.csv
