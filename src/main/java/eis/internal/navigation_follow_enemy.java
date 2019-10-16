package eis.internal;

import eis.EISAdapter;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import map.AgentMap;
import map.Direction;
import map.MapPercept;
import map.Position;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class navigation_follow_enemy extends DefaultInternalAction {


    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
//        for (var chunk : agentMap.getCurrentPercepts().values().stream().filter(MapPercept::hasEnemyEntity).collect(Collectors.toList()))
//        {
//            chunk.hasEnemyEntity();
//        }
//

       return true;
    }

    private Position findThing(AgentMap map, String type, String details) {
        // It would be good to sort these by how recent the perceptions are, and the distance.
        MapPercept percept = map.getMapGraph().getCache().getCachedThingList().stream().filter(p -> p.hasThing(type, details)).findAny().orElse(null);

        if (percept == null)
            return null;

        List<Position> shortestPath = map.getAgentNavigation().getNavigationPath(percept.getLocation());

        if (shortestPath != null) {
            // Remove the THING location from the navigation path
            shortestPath.removeIf(p -> p.equals(percept.getLocation()));

            // Return the destination position
            if(shortestPath.isEmpty())
            {
                // We are right next to the percept. No need to navigate
                System.out.println("We are next to the percept. Type: " + type + ". Details: " + details);
                return null;
            }

            return shortestPath.get(shortestPath.size() - 1);
        }

        // If the agent is blocked by the percept, no navigation path will be found.
        // This attempts to get a navigation path to the surrounding percepts
        for (Map.Entry<Direction, MapPercept> surroundingPercepts : map.getSurroundingPercepts(percept).entrySet()) {
            shortestPath = map.getAgentNavigation().getNavigationPath(surroundingPercepts.getValue().getLocation());

            // If we have a path to the percept, return the position
            if (shortestPath != null)
                return surroundingPercepts.getValue().getLocation();
        }

        // No path could be found
        return null;
    }

}
