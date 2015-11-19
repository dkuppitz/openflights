/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import com.datastax.openflights.OpenflightsBulkLoader
import com.thinkaurelius.titan.core.Cardinality

PROJECT_DIR = "/projects/datastax/openflights"

g = TitanFactory.open(PROJECT_DIR + "/conf/openflights-tp3.properties")
g.close()

com.thinkaurelius.titan.core.util.TitanCleanup.clear(g)

g = TitanFactory.open(PROJECT_DIR + "/conf/openflights-tp3.properties")
m = g.openManagement()
country = m.makeVertexLabel("country").make()
airport = m.makeVertexLabel("airport").make()
airline = m.makeVertexLabel("airline").make()
route = m.makeVertexLabel("route").make()
m.makeEdgeLabel("locatedIn").make()
m.makeEdgeLabel("incorporatedIn").make()
m.makeEdgeLabel("source").make()
m.makeEdgeLabel("destination").make()
m.makeEdgeLabel("operatedBy").make()
name = m.makePropertyKey("name").dataType(String.class).make()
uid = m.makePropertyKey("uid").dataType(Integer.class).make()
city = m.makePropertyKey("city").dataType(String.class).make()
iata = m.makePropertyKey("iata").dataType(String.class).make()
icao = m.makePropertyKey("icao").dataType(String.class).make()
location = m.makePropertyKey("location").dataType(Geoshape.class).make()
alt = m.makePropertyKey("altitude").dataType(Integer.class).make()
timezone = m.makePropertyKey("timezone").dataType(Double.class).make()
dst = m.makePropertyKey("dst").dataType(String.class).make()
tz = m.makePropertyKey("tz").dataType(String.class).make()
m.makePropertyKey("alias").dataType(String.class).make()
callsign = m.makePropertyKey("callsign").dataType(String.class).make()
m.makePropertyKey("airline").dataType(String.class).make()
active = m.makePropertyKey("active").dataType(String.class).make()
codeshare = m.makePropertyKey("codeshare").dataType(String.class).make()
stops = m.makePropertyKey("stops").dataType(Integer.class).make()
equipment = m.makePropertyKey("equipment").dataType(String.class).cardinality(Cardinality.SET).make()
blid = m.makePropertyKey("bulkLoader.vertex.id").dataType(Long.class).make()
m.buildIndex("countryByName", Vertex.class).addKey(name).indexOnly(country).buildCompositeIndex()
m.buildIndex("airportById", Vertex.class).addKey(uid).indexOnly(airport).buildCompositeIndex()
m.buildIndex("airlineById", Vertex.class).addKey(uid).indexOnly(airline).buildCompositeIndex()
m.buildIndex("airportByIATA", Vertex.class).addKey(iata).indexOnly(airport).buildCompositeIndex()
m.buildIndex("byLocation", Vertex.class).addKey(location).buildMixedIndex("search")
m.buildIndex("byBulkLoaderVertexId", Vertex.class).addKey(blid).buildCompositeIndex()
m.commit()
g.close()

graph = GraphFactory.open(PROJECT_DIR + "/conf/hadoop/openflights-tp3.properties")

writeGraph = PROJECT_DIR + "/conf/openflights.properties"
blvp = OpenflightsBulkLoaderVertexProgram.build().keepOriginalIds(false).writeGraph(writeGraph).
        intermediateBatchSize(10000).bulkLoader(OpenflightsBulkLoader).create(graph)
graph.compute(SparkGraphComputer).program(blvp).submit().get()

