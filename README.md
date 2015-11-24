# Titan 1.0 Data Migration

<img src="https://raw.githubusercontent.com/dkuppitz/openflights/master/docs/images/gremlin-plane.png" align="left" width="300">The release of [Titan 1.0](http://thinkaurelius.github.io/titan/) brought with it many important features, but it also came with the need to do a major code and data migration given the upgrade to [Apache TinkerPop 3](http://tinkerpop.apache.org) and the finalized changes to the schema used to store the graph data in Cassandra, HBase, etc. As a result, upgrading from older versions of Titan to this new version has its challenges.

This document and the associated code in this repository are designed to help with the data migration aspect of upgrading to Titan 1.0.  It provides a model for data migration by using the [OpenFlights](https://github.com/jpatokal/openflights) data set and is aimed at users looking to upgrade from Titan 0.5.4 to Titan 1.0.0. It presents methods for dealing with the hardest aspects of data migration in this context, covering geoshapes and multi-properties and it is meant to offer guidance for those migrating graph data of any size.

## Approach

This repository contains all the code required to simulate a data migration from Titan 0.5.4 to Titan 1.0.0. It provides Groovy scripts and Java code that together will generate the Titan 0.5.4 instance from the OpenFlights data set and then migrate it to Titan 1.0.0.  

## Prerequisites

* Java 1.8.0_40+ (required by TinkerPop 3.x)
* Maven 3.x
* Titan 0.5.4 and Titan 1.0.0
* Cassandra as [compatible](http://s3.thinkaurelius.com/docs/titan/1.0.0/version-compat.html) with the Titan versions above (while the tutorial focuses on Cassandra, there should be no reason that these steps will not also work for other Titan backends)
* Hadoop 2

## Assumptions

This tutorial assumes that the reader is knowledgeable with respect to Titan 0.5.x and has some basic knowledge of Titan 1.x and TinkerPop 3.x. 

# The Tutorial

For those wishing to get right to the abbreviated steps required to run this tutorial, they can be found at the [bottom](#For_the_Impatient).

## The Schema

## Loading Titan 0.5.4

## Migrating to Titan 1.0.0

# For the Impatient

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
