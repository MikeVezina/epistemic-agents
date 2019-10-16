package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.percepts.terrain.FreeSpace;
import eis.percepts.terrain.Obstacle;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import map.AgentMap;
import map.Direction;
import map.MapPercept;
import map.Position;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class is_agent_contained extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;
    private static final String CLASS_NAME = is_agent_contained.class.getName();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());


        //boolean isContained = Arrays.stream(Direction.validDirections()).noneMatch(d -> agentMap.getAgentNavigation().canAgentMove(d));

        Position cagedBlock = getCagedBlock(agentMap);

        if (cagedBlock == null)
            return false;

        Position relPercept = agentMap.getAgentContainer().absoluteToRelativeLocation(cagedBlock);

        NumberTerm xTerm = ASSyntax.createNumber(relPercept.getX());
        NumberTerm yTerm = ASSyntax.createNumber(relPercept.getY());

        return un.unifies(args[0], xTerm) && un.unifies(args[1], yTerm);
    }

    private Position getCagedBlock(AgentMap agentMap) {
        Set<Position> checkedPos = new HashSet<>();
        checkedPos.add(agentMap.getCurrentAgentPosition());

        for (int curRadius = 1; curRadius < 4; curRadius++) {
            var areaList = new Utils.Area(agentMap.getCurrentAgentPosition(), curRadius);
            var remainingArea = areaList.stream()
                    .filter(pos -> !checkedPos.contains(pos)).collect(Collectors.toList());

            boolean allBlocked = remainingArea.stream()
                    .map(agentMap::getMapPercept)
                    .filter(Objects::nonNull)
                    .allMatch(agentMap::doesBlockAgent);

            if (allBlocked) {
                return getWall(agentMap, remainingArea);
            }

            checkedPos.addAll(areaList);
        }

        return null;
    }

    private Position getWall(AgentMap agentMap, List<Position> positions) {
        Position highestSurroundingSpaces = null;
        long numSurrounding = -1;

        for (Position pos : positions) {
            MapPercept percept = agentMap.getMapPercept(pos);
            long surrounding = agentMap.getSurroundingPercepts(percept).values().stream().filter(p -> p.getTerrain() instanceof FreeSpace).count();
            if (numSurrounding < surrounding) {
                highestSurroundingSpaces = pos;
                numSurrounding = surrounding;
            }
        }

        return highestSurroundingSpaces;
    }
}
