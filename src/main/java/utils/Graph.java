package utils;

import eis.agent.AgentContainer;
import eis.percepts.CustomEdge;
import eis.percepts.MapPercept;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Graph extends ConcurrentHashMap<Position, MapPercept> {

    DirectedGraph<Position, CustomEdge> graph = new DefaultDirectedGraph<>(CustomEdge.class);
    private AgentContainer agentContainer;

    public Graph(AgentContainer agentContainer) {
        this.agentContainer = agentContainer;
    }

    @Override
    public void putAll(Map<? extends Position, ? extends MapPercept> m) {

        m.entrySet().parallelStream().forEach(e ->
        {
            put(e.getKey(), e.getValue());
        });
    }


    @Override
    public synchronized MapPercept put(Position key, MapPercept value) {

        graph.addVertex(key);

//        if (gridVisualizer != null)
//            gridVisualizer.updateGridLocation(value);


        if (!agentContainer.isAttachedPercept(value) && agentContainer.getAgentMap().doesBlockAgent(value)) {
            Set<CustomEdge> defaultEdges = graph.edgesOf(key);
            graph.removeAllEdges(defaultEdges);
        } else {
            // Add edges to surrounding positions
            for (Position p : new Utils.Area(key, 1)) {
                MapPercept cur = get(p);
                if (graph.containsVertex(p) && !key.equals(p) && cur != null && !agentContainer.getAgentMap().doesBlockAgent(cur)) {
                    try {
                        CustomEdge de = graph.addEdge(key, p);
                        if (de != null) {
                            de.setSource(key);
                            de.setTarget(p);
                        }

                        de = graph.addEdge(p, key);
                        if (de != null) {
                            de.setSource(p);
                            de.setTarget(key);
                        }
                    } catch (IllegalArgumentException e) {
                        throw e;
                    }
                }
            }
        }

        return super.put(key, value);
    }

    public List<Position> getShortestPath(Position start, Position end) {
        if (!this.graph.containsVertex(start) || !this.graph.containsVertex(end)) {
            System.out.println("The graph does not contain the source or destination vertex: [" + start + ", " + end + "]");
            return null;
        }


        DijkstraShortestPath<Position, CustomEdge> dijkstraShortestPath = new DijkstraShortestPath<>(this.graph);
        GraphPath<Position, CustomEdge> shortestPath = dijkstraShortestPath.getPath(start, end);

        if (shortestPath == null) {
            System.out.println("Failed to generate the shortest path.");
            return null;
        }

        return shortestPath.getVertexList();
    }
}