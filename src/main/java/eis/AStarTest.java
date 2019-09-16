package eis;

import aima.core.environment.map.ExtendableMap;
import aima.core.environment.map.Map;
import es.usc.citius.hipster.algorithm.ADStarForward;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.graph.HipsterGraph;
import es.usc.citius.hipster.model.Node;
import es.usc.citius.hipster.model.function.HeuristicFunction;
import es.usc.citius.hipster.model.problem.SearchComponents;
import eis.map.Position;

import java.util.Iterator;
import java.util.function.ToDoubleFunction;

public class AStarTest {
    public static void main(String[] args) throws Exception {
        Position start = Position.ZERO;
        Position first = new Position(0, 1);
        Position second= new Position(1, 1);
        Position end = new Position(1, 0);


    }

    /**
     * Heuristic function required to define search problems to be used with Hipster.
     *
     * @return {@link es.usc.citius.hipster.model.function.HeuristicFunction} with the {@link #heuristics()} values.
     * @see es.usc.citius.hipster.model.problem.SearchProblem
     */
    public static HeuristicFunction<Position, Double> heuristicFunction(Position end) {
        return state -> Math.abs(end.subtract(state).getDistance());
    }


    private static ToDoubleFunction getMapHeuristicFunction() {
        return null;// MapFunctionFactory.getSLDHeuristicFunction(new Position(1,0), createPerceptionMap());
    }

    private static Map createPerceptionMap() {
        ExtendableMap map = new ExtendableMap();
        addDirectionalLinks(map, new Position(0, 0));
        return map;
    }

    private static void addDirectionalLinks(ExtendableMap map, Position pos) {
        // Add west
//        Position west = pos.add(Direction.WEST.getPosition());
//        map.addBidirectionalLink(getLocationString(pos), getLocationString(west), 1.0);
//
//        Position east = pos.add(Utils.DirectionToRelativeLocation("e"));
//        map.addBidirectionalLink(getLocationString(pos), getLocationString(east), 1.0);
//
//        Position north = pos.add(Utils.DirectionToRelativeLocation("n"));
//        map.addBidirectionalLink(getLocationString(pos), getLocationString(north), 1.0);
//
//        Position south = pos.add(Utils.DirectionToRelativeLocation("s"));
//        map.addBidirectionalLink(getLocationString(pos), getLocationString(south), 1.0);
    }

    private static String getLocationString(Position p) {
        return p.toString();
    }
}
