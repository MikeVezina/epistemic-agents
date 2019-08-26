package eis.internal;

import aima.core.environment.map.ExtendableMap;
import aima.core.environment.map.Map;
import aima.core.search.framework.problem.Problem;
import aima.core.search.framework.qsearch.GraphSearch;
import aima.core.search.framework.qsearch.QueueSearch;
import aima.core.search.informed.AStarSearch;
import utils.Position;
import utils.Utils;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class AStarTest {
    public static void main(String[] args)
    {

        AStarSearch aStarSearch = new AStarSearch(new GraphSearch(), getMapHeuristicFunction());

//        Problem<String, MoveToAction> problem = new GeneralProblem<>("start",
//                MapFunctions.createActionsFunction(map),
//                MapFunctions.createResultFunction(), Predicate.isEqual("goal"),
//                MapFunctions.createDistanceStepCostFunction(map));
//
//        SearchAgent agent = new SearchAgent(problem, search);

    }

    private static ToDoubleFunction getMapHeuristicFunction()
    {
        return null;// MapFunctionFactory.getSLDHeuristicFunction(new Position(1,0), createPerceptionMap());
    }

    private static Map createPerceptionMap()
    {
        ExtendableMap map = new ExtendableMap();
        addDirectionalLinks(map, new Position(0,0));
        return map;
    }

    private static void addDirectionalLinks(ExtendableMap map, Position pos)
    {
        // Add west
        Position west = pos.add(Utils.DirectionToRelativeLocation("w"));
        map.addBidirectionalLink(getLocationString(pos), getLocationString(west), 1.0);

        Position east = pos.add(Utils.DirectionToRelativeLocation("e"));
        map.addBidirectionalLink(getLocationString(pos), getLocationString(east), 1.0);

        Position north = pos.add(Utils.DirectionToRelativeLocation("n"));
        map.addBidirectionalLink(getLocationString(pos), getLocationString(north), 1.0);

        Position south = pos.add(Utils.DirectionToRelativeLocation("s"));
        map.addBidirectionalLink(getLocationString(pos), getLocationString(south), 1.0);
    }

    private static String getLocationString(Position p) {
        return p.toString();
    }
}
