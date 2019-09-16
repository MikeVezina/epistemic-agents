package eis.map;

import eis.agent.AgentContainer;

import es.usc.citius.hipster.graph.HashBasedHipsterGraph;
import utils.Utils;

import java.util.*;

public class Graph extends HashBasedHipsterGraph<Position, Double> {

    private AgentContainer agentContainer;
    private MapCache mapCache;

    public Graph(AgentContainer agentContainer) {
        this.agentContainer = agentContainer;
        this.mapCache = new MapCache();
    }

    private boolean doesBlockAgent(MapPercept percept) {
        return agentContainer.getAgentMap().doesBlockAgent(percept);
    }

    public boolean containsKey(Position key) {
        return mapCache.containsKey(key);
    }

    /**
     * Prepares the chunk of map percept updates by creating vertices and resetting any existing edges.
     *
     * @param percepts The list of percepts to update.
     */
    public void prepareChunk(List<MapPercept> percepts) {
        if (percepts == null)
            return;

        for (MapPercept percept : percepts) {
            // First, remove the old percept from the graph representation
            super.remove(percept.getLocation());

            // Add the percept to our own mapping from Position -> MapPercept
            // This allows us to look this value up later for edge creation.
            mapCache.put(percept.getLocation(), percept);

            // Lastly, add the percept to the internal graph representation
            super.add(percept.getLocation());
        }
    }

    public synchronized void updateChunk(List<MapPercept> mapChunk) {
        // Prepares the map chunk (Creates all vertices)
        prepareChunk(mapChunk);

        // Second, add any available edges
        for (MapPercept percept : mapChunk) {

            // Don't add any edges if this percept blocks the agent.
            if (doesBlockAgent(percept))
                return;

            // Add any edges for percepts that do not block.
            for (Position areaPos : new Utils.Area(percept.getLocation(), 1)) {
                // Don't add an edge to the current cell
                if (percept.getLocation().equals(areaPos) || !mapCache.containsKey(areaPos))
                    continue;

                MapPercept mapPercept = mapCache.getOrDefault(areaPos, null);

                // If the next block does not block the agent, connect it to the current block
                if (!doesBlockAgent(mapPercept))
                    super.connect(percept.getLocation(), areaPos, 1.0d);
            }
        }
    }

//    public synchronized MapPercept put(Position key, MapPercept value) {
//
//
////        if (gridVisualizer != null)
////            gridVisualizer.updateGridLocation(value);
//
//
//        if (!agentContainer.isAttachedPercept(value) && agentContainer.getAgentMap().doesBlockAgent(value)) {
//            Set<CustomEdge> defaultEdges = graph.edgesOf(key);
//            graph.removeAllEdges(defaultEdges);
//        } else {
//            // Add edges to surrounding positions
//            for (Position p : new Utils.Area(key, 1)) {
//                MapPercept cur = edgesOf(p);
//                if (graph.containsVertex(p) && !key.equals(p) && cur != null && !agentContainer.getAgentMap().doesBlockAgent(cur)) {
//                    try {
//                        CustomEdge de = graph.addEdge(key, p);
//                        if (de != null) {
//                            de.setSource(key);
//                            de.setTarget(p);
//                        }
//
//                        de = graph.addEdge(p, key);
//                        if (de != null) {
//                            de.setSource(p);
//                            de.setTarget(key);
//                        }
//                    } catch (IllegalArgumentException e) {
//                        throw e;
//                    }
//                }
//            }
//        }
//
//        return super.put(key, value);
//    }

    public List<Position> getShortestPath(Position start, Position end) {
        return new ArrayList<>();
//        if (!this.graph.containsVertex(start) || !this.graph.containsVertex(end)) {
//            System.out.println("The graph does not contain the source or destination vertex: [" + start + ", " + end + "]");
//            return null;
//        }
//
//
//        DijkstraShortestPath<Position, CustomEdge> dijkstraShortestPath = new DijkstraShortestPath<>(this.graph);
//        GraphPath<Position, CustomEdge> shortestPath = dijkstraShortestPath.getPath(start, end);
//
//        if (shortestPath == null) {
//            System.out.println("Failed to generate the shortest path.");
//            return null;
//        }
//
//        return shortestPath.getVertexList();
    }

    public MapPercept get(Position absolutePosition) {
        return mapCache.getOrDefault(absolutePosition, null);
    }

    public MapCache getCache() {
        return mapCache;
    }
}