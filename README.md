![TinkerPop](https://raw.githubusercontent.com/dkuppitz/openflights/master/docs/images/gremlin-plane.png)

0. Clone project into /projects/datastax/openflights
1. Download data files (run ${PROJECT_DIR}/data/download.sh)
2. In a Titan 0.5.4 Gremlin console:

     gremlin> . /projects/datastax/openflights/scripts/load-openflights-tp2.groovy

3. Copy the output files (from the output/ directory in HDFS) into a Hadoop2 HDFS directory called openflights
4. upload the script ${PROJECT_DIR}/scripts/openflights-script-input.groovy into Hadoop2's HDFS home directory
5. compile the project:

     $ mvn clean install -DskipTests

6. Copy the jar file ${PROJECT_DIR}/target/openflights-1.0-SNAPSHOT.jar into ${TITAN_1_0_HOME}/ext/
5. In a Titan 1.0.0 Gremlin console:

     gremlin> :load /projects/datastax/openflights/scripts/load-openflights-tp3.groovy
