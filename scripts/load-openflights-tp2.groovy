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

groovy.grape.Grape.grab(group: 'com.xlson.groovycsv', module: 'groovycsv', version: '1.1')

import com.thinkaurelius.titan.core.Cardinality
import com.xlson.groovycsv.CsvParser

PROJECT_DIR = "/projects/datastax/openflights"
DATA_DIR = PROJECT_DIR + "/data"

g = TitanFactory.open(PROJECT_DIR + "/conf/openflights-tp2.properties")
g.shutdown()

com.thinkaurelius.titan.core.util.TitanCleanup.clear(g)

g = TitanFactory.open(PROJECT_DIR + "/conf/openflights-tp2.properties")
m = g.getManagementSystem()
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
m.buildIndex("countryByName", Vertex.class).addKey(name).indexOnly(country).buildCompositeIndex()
m.buildIndex("airportById", Vertex.class).addKey(uid).indexOnly(airport).buildCompositeIndex()
m.buildIndex("airlineById", Vertex.class).addKey(uid).indexOnly(airline).buildCompositeIndex()
m.buildIndex("airportByIATA", Vertex.class).addKey(iata).indexOnly(airport).buildCompositeIndex()
m.buildIndex("byLocation", Vertex.class).addKey(location).buildMixedIndex("search")
m.commit()

getOrCreateCountry = { def name ->
    def itty = g.query().has("label", "country").has("name", name).vertices().iterator()
    if (itty.hasNext()) return itty.next()
    def v = g.addVertexWithLabel("country")
    v.setProperty("name", name)
    return v
}

csv = new File(DATA_DIR + "/airports.dat").getText().replaceAll(/,\\N\b/, ","); []
airports = new CsvParser().parse(csv, readFirstLine: true, columnNames: [
        "uid", "name", "city", "country", "iata", "icao", "latitude", "longitude", "altitude", "timezone", "dst", "tz"]); []

println "Loading airports..."
airports.each { def airport ->
    def v = g.addVertexWithLabel("airport")
    def lat = null, lng = null
    airport.toMap().each { def key, def value ->
        if (!value.isEmpty()) {
            switch (key) {
                case "uid":
                case "altitude":
                    v.setProperty(key, value.toInteger())
                    break
                case "latitude":
                    lat = value.toDouble()
                    break
                case "longitude":
                    lng = value.toDouble()
                    break
                case "timezone":
                    v.setProperty(key, value.toDouble())
                    break
                case "country":
                    v.addEdge("locatedIn", getOrCreateCountry(value))
                    break
                default:
                    v.setProperty(key, value)
                    break
            }
            if (lat != null && lng != null) {
                v.setProperty("location", Geoshape.point(lat, lng))
                lat = lng = null
            }
        }
    }
}
g.commit()

println "Loading airlines..."
csv = new File(DATA_DIR + "/airlines.dat").getText().replaceAll(/,\\N\b/, ","); []
airlines = new CsvParser().parse(csv, readFirstLine: true, columnNames: [
        "uid", "name", "alias", "iata", "icao", "callsign", "country", "active"]); []
airlines.each { def airline ->
    def v = g.addVertexWithLabel("airline")
    airline.toMap().each { def key, def value ->
        if (!value.isEmpty()) {
            switch (key) {
                case "uid":
                    v.setProperty(key, value.toInteger())
                    break
                case "country":
                    v.addEdge("incorporatedIn", getOrCreateCountry(value))
                    break
                default:
                    v.setProperty(key, value)
                    break
            }
        }
    }
}
g.commit()

println "Loading routes..."
csv = new File(DATA_DIR + "/routes.dat").getText().replaceAll(/,\\N\b/, ","); []
i = 0
routes = new CsvParser().parse(csv, readFirstLine: true, columnNames: [
        "airline", "airlineId", "source", "sourceId", "destination", "destinationId", "codeshare", "stops", "equipment"]); []
routes.each { def route ->
    def v = g.addVertexWithLabel("route")
    route.toMap().each { def key, def value ->
        if (!value.isEmpty()) {
            switch (key) {
                case "source":
                case "destination":
                    break
                case "sourceId":
                    v.addEdge("source", g.query().has("label", "airport").has("uid", value.toInteger()).vertices().iterator().next())
                    break
                case "destinationId":
                    v.addEdge("destination", g.query().has("label", "airport").has("uid", value.toInteger()).vertices().iterator().next())
                    break
                case "airlineId":
                    v.addEdge("operatedBy", g.query().has("label", "airline").has("uid", value.toInteger()).vertices().iterator().next())
                    break
                case "stops":
                    v.setProperty(key, value.toInteger())
                    break
                case "equipment":
                    value.split().grep().each {
                        v.addProperty(key, it)
                    }
                    break
                default:
                    v.setProperty(key, value)
                    break
            }
        }
    }
    if (++i % 10000 == 0) g.commit()
}

g.shutdown()
g = HadoopGraph.open(PROJECT_DIR + "/conf/hadoop/openflights-tp2.properties")
g._()

