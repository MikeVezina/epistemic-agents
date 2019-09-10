package eis.percepts.containers;

import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.percepts.parsers.PerceptHandlerFactory;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.PerceptUtils;

import java.util.*;

public class AgentPerceptContainer extends PerceptContainer {
    private static final String ENERGY_PERCEPT_NAME = "energy";
    private static final String DISABLED_PERCEPT_NAME = "disabled";
    private static final String THING_PERCEPT_NAME = Thing.PERCEPT_NAME;
    private static final String OBSTACLE_PERCEPT_NAME = Obstacle.PERCEPT_NAME;
    private static final String GOAL_PERCEPT_NAME = Goal.PERCEPT_NAME;
    private static final String LAST_ACTION_PERCEPT_NAME = "lastAction";
    private static final String LAST_ACTION_RESULT_PERCEPT_NAME = "lastActionResult";
    private static final String LAST_ACTION_PARAMS_PERCEPT_NAME = "lastActionParams";

    // Percept names that are contained within this container.
    private static final Set<String> VALID_PERCEPT_NAMES = Set.of(ENERGY_PERCEPT_NAME, DISABLED_PERCEPT_NAME, THING_PERCEPT_NAME, OBSTACLE_PERCEPT_NAME,
            GOAL_PERCEPT_NAME, LAST_ACTION_PERCEPT_NAME, LAST_ACTION_RESULT_PERCEPT_NAME, LAST_ACTION_PARAMS_PERCEPT_NAME);

    // Required percept names (all raw percept lists should have these).
    private static final Set<String> REQUIRED_PERCEPT_NAMES = Set.of(LAST_ACTION_PERCEPT_NAME, LAST_ACTION_PARAMS_PERCEPT_NAME, LAST_ACTION_RESULT_PERCEPT_NAME, ENERGY_PERCEPT_NAME, DISABLED_PERCEPT_NAME);

    private SharedPerceptContainer sharedPerceptContainer;

    // Individual agent percepts
    private int energy;
    private boolean disabled;
    private List<Thing> thingList;
    private List<Terrain> terrainList;
    private String lastAction;
    private String lastActionResult;
    private ParameterList lastActionParams;

    @Override
    protected Set<String> getRequiredPerceptNames() {
        return REQUIRED_PERCEPT_NAMES;
    }

    protected AgentPerceptContainer(Map<String, List<Percept>> filteredPerceptMap, SharedPerceptContainer sharedPerceptContainer) {
        super(filteredPerceptMap);
        setFilteredPerceptMap(filteredPerceptMap);
        setSharedPerceptContainer(sharedPerceptContainer);

        // Private constructor
        setEnergy();
        setDisabled();
        setLastActionInfo();

        // Set Other percept info
        setThingList();
        setTerrainList();
    }

    public SharedPerceptContainer getSharedPerceptContainer()
    {
        return sharedPerceptContainer;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public List<Thing> getThingList() {
        return thingList;
    }

    public List<Terrain> getTerrainList() {
        return terrainList;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getLastActionResult() {
        return lastActionResult;
    }

    public ParameterList getLastActionParams() {
        return lastActionParams;
    }

    private void setSharedPerceptContainer(SharedPerceptContainer sharedPerceptContainer)
    {
        this.sharedPerceptContainer = sharedPerceptContainer;
    }

    private void setEnergy() {
        this.energy = parseSingleNumberPercept(getFilteredPerceptMap().get(ENERGY_PERCEPT_NAME)).intValue();
    }

    private void setDisabled() {
        this.disabled = parseSingleBooleanPercept(getFilteredPerceptMap().get(DISABLED_PERCEPT_NAME));
    }

    private void setThingList() {
        this.thingList = PerceptHandlerFactory.getThingPerceptHandler().mapAllPercepts(getFilteredPerceptMap().get(THING_PERCEPT_NAME));
    }

    private void setTerrainList() {
        this.terrainList = PerceptHandlerFactory.getTerrainPerceptHandler().mapAllPercepts(getFilteredPerceptMap().get(THING_PERCEPT_NAME));
    }

    private void setLastActionInfo() {

        this.lastAction = parseSingleStringPercept(getFilteredPerceptMap().get(LAST_ACTION_PERCEPT_NAME));
        this.lastActionResult = parseSingleStringPercept(getFilteredPerceptMap().get(LAST_ACTION_RESULT_PERCEPT_NAME));
        this.lastActionParams = parseSingleParameterListPercept(getFilteredPerceptMap().get(LAST_ACTION_PARAMS_PERCEPT_NAME));
    }

    public static AgentPerceptContainer parsePercepts(List<Percept> rawPercepts) {
        SharedPerceptContainer sharedPerceptContainer = SharedPerceptContainer.parsePercepts(rawPercepts);
        return new AgentPerceptContainer(filterValidPercepts(rawPercepts, VALID_PERCEPT_NAMES), sharedPerceptContainer);
    }

}
