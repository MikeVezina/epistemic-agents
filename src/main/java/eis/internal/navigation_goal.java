package eis.internal;

import eis.EISAdapter;
import eis.percepts.terrain.Goal;
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

public class navigation_goal extends DefaultInternalAction {


    private static final long serialVersionUID = -6214881485708125130L;
    private static final Atom NORTH = new Atom("n");
    private static final Atom SOUTH = new Atom("s");
    private static final Atom WEST = new Atom("w");
    private static final Atom EAST = new Atom("e");


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
        Position thingDestination = findGoal(agentMap);


        if (thingDestination == null)
            return false;

        Structure location = ASSyntax.createStructure("location", ASSyntax.createNumber(thingDestination.getX()), ASSyntax.createNumber(thingDestination.getY()));

        // Unify
        return un.unifies(location, args[0]);
    }

    private Position findGoal(AgentMap map) {
        // It would be good to sort these by how recent the perceptions are, and the distance.
        var goalPercepts = map.getMapGraph().getCache().getCachedTerrain().stream()
                .filter(p -> p.getTerrain() instanceof Goal)
                .filter(p -> !map.doesBlockAgent(p))
                .filter(p -> !p.getLocation().equals(map.getAgentContainer().getCurrentLocation()))
                .filter(p -> map.getAgentNavigation().getNavigationPath(p.getLocation()) != null)
                .sorted((g1, g2) -> {
                    double dist1 = g1.getLocation().subtract(map.getAgentContainer().getCurrentLocation()).getDistance();
                    double dist2 = g2.getLocation().subtract(map.getAgentContainer().getCurrentLocation()).getDistance();

                    return Double.compare(dist2, dist1);
                }).collect(Collectors.toList());



        if(goalPercepts.isEmpty())
            return null;

        return goalPercepts.get(0).getLocation();
    }

}
