package utils;

import com.mxgraph.layout.*;
import com.mxgraph.util.mxCellRenderer;
import eis.percepts.AgentMap;
import eis.percepts.CustomEdge;
import eis.percepts.MapPercept;
import eis.percepts.terrain.Obstacle;
import eis.percepts.things.Entity;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Graph extends ConcurrentHashMap<Position, MapPercept> {

    DirectedGraph<Position, CustomEdge> graph = new DefaultDirectedGraph<>(CustomEdge.class);
    private GridVisualizer gridVisualizer;
    private AgentMap agentMap;

    public Graph(AgentMap map) {
        this.agentMap = map;
        gridVisualizer = new GridVisualizer(map.getAgent());
    }

    public void redraw() {
        if (gridVisualizer != null)
            gridVisualizer.validate();
    }

    @Override
    public synchronized MapPercept put(Position key, MapPercept value) {


        graph.addVertex(key);
        if (gridVisualizer != null)
            gridVisualizer.updateGridLocation(agentMap.getCurrentAgentPosition(), key, value);

        if (value.isBlocking()) {
            Set<CustomEdge> defaultEdges = graph.edgesOf(key);
            graph.removeAllEdges(defaultEdges);
        } else {
            // Add edges to surrounding positions
            for (Position p : new Utils.Area(key, 1)) {
                MapPercept cur = get(p);
                if (graph.containsVertex(p) && !key.equals(p) && cur != null && !cur.isBlocking()) {
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

    public static void main(String[] args) {
//        Graph g = new Graph(null);
//
//
//        for (Position p : new Utils.Area(new Position(0, 0), 3)) {
//            g.create(p);
//        }
//
//        g.create(new Position(0, 0), true, false);
//        g.create(new Position(0, 1), false, true);
//        g.create(new Position(1, 0), true, false);
//        g.create(new Position(1, 2), true, false);
//        g.create(new Position(-1, 2), true, false);
//
//        g.drawGraph();
    }

    private void create(Position p) {
        create(p, false, false);
    }

    private void create(Position p, boolean hasThing, boolean hasTerrain) {
        MapPercept map = new MapPercept(p, "agent", 5);

        if (hasThing)
            map.setThing(new Entity(p.getX(), p.getY(), "age"));

        if (hasTerrain)
            map.setTerrain(new Obstacle(p.getX(), p.getY()));
        this.put(p, map);
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


    public void drawGraph() {
        JGraphXAdapter<Position, CustomEdge> graphAdapter = new JGraphXAdapter<>(graph);

        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);

        File imgFile = new File("./graph.png");
        try {
            imgFile.createNewFile();
            ImageIO.write(image, "PNG", imgFile);
        } catch (Exception e) {
            System.out.println("Failed to create/write image file.");
        }

    }
}