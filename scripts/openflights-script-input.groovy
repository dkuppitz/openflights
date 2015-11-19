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
import com.thinkaurelius.titan.core.attribute.Geoshape

def parse(line, factory) {
    def slurper = new JsonSlurper()
    def properties = slurper.parseText(line)
    def outE = properties.remove("_outE")
    def inE = properties.remove("_inE")
    def vid = properties.remove("_id")
    def vlabel = properties.remove("_label") ?: Vertex.DEFAULT_LABEL
    def vertex = factory.vertex(vid, vlabel)
    properties.each { def key, def value ->
        if (value instanceof Iterable) {
            value.each {
                vertex.property(set, key, it)
            }
        } else if (key == "location") {
            def ll = value.split(",")*.replaceAll(/[^0-9E.-]/, "")*.toFloat()
            vertex.property(key, Geoshape.point(ll[0], ll[1]))
        } else {
            vertex.property(key, value)
        }
    }
    if (outE != null) {
        outE.each { def e ->
            def eid = e.remove("_id")
            def elabel = e.remove("_label") ?: Edge.DEFAULT_LABEL
            def inV = factory.vertex(e.remove("_inV"))
            edge = factory.edge(vertex, inV, elabel)
            e.each { def key, def value ->
                edge.property(key, value)
            }
        }
    }
    if (inE != null) {
        inE.each { def e ->
            def eid = e.remove("_id")
            def elabel = e.remove("_label")
            def outV = factory.vertex(e.remove("_outV"))
            edge = factory.edge(outV, vertex, elabel)
            e.each { def key, def value ->
                edge.property(key, value)
            }
        }
    }
    return vertex
}
