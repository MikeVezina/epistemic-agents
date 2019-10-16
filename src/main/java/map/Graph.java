package map;

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

    public synchronized void updateChunkEdges(List<MapPercept> mapChunk) {

        // Second, add any available edges
        for (MapPercept percept : mapChunk) {

            // Don't add any edges if this percept blocks the agent.
            if (doesBlockAgent(percept))
                continue;

            // Add any edges for percepts that do not block.
            for (Position areaPos : new Utils.Area(percept.getLocation(), 1)) {
                // Don't add an edge to the current cell
                if (percept.getLocation().equals(areaPos) || !mapCache.containsKey(areaPos))
                    continue;

                MapPercept mapPercept = mapCache.getOrDefault(areaPos, null);

                // If the next block does not block the agent, connect it to the current block
                if (!doesBlockAgent(mapPercept)) {
                    super.connect(percept.getLocation(), areaPos, 1.0d);
                }
            }
        }
    }

    public MapPercept get(Position absolutePosition) {
        return mapCache.getOrDefault(absolutePosition, null);
    }

    public MapCache getCache() {
        return mapCache;
    }
}