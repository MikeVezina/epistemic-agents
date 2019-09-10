package eis.percepts.containers;

import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.parsers.PerceptHandlerFactory;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.PerceptUtils;

import java.util.*;

public class PerceptContainer {
    private static final String ENERGY_PERCEPT_NAME = "energy";
    private static final String DISABLED_PERCEPT_NAME = "disabled";
    private static final String THING_PERCEPT_NAME = Thing.PERCEPT_NAME;
    private static final String OBSTACLE_PERCEPT_NAME = Obstacle.PERCEPT_NAME;
    private static final String GOAL_PERCEPT_NAME = Goal.PERCEPT_NAME;
    private static final String STEP_PERCEPT_NAME = "step";
    private static final String SCORE_PERCEPT_NAME = "score";
    private static final String VISION_PERCEPT_NAME = "vision";
    private static final String TEAM_PERCEPT_NAME = "team";
    private static final String LAST_ACTION_PERCEPT_NAME = "lastAction";
    private static final String LAST_ACTION_RESULT_PERCEPT_NAME = "lastActionResult";
    private static final String LAST_ACTION_PARAMS_PERCEPT_NAME = "lastActionParams";
    private static final String TASK_PERCEPT_NAME = Task.PERCEPT_NAME;

    // Percept names that are contained within this container.
    private static final Set<String> VALID_PERCEPT_NAMES = Set.of(STEP_PERCEPT_NAME, ENERGY_PERCEPT_NAME, DISABLED_PERCEPT_NAME, THING_PERCEPT_NAME, OBSTACLE_PERCEPT_NAME,
            GOAL_PERCEPT_NAME, SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME, TASK_PERCEPT_NAME,
            LAST_ACTION_PERCEPT_NAME, LAST_ACTION_RESULT_PERCEPT_NAME, LAST_ACTION_PARAMS_PERCEPT_NAME);

    // Required percept names (all raw percept lists should have these).
    private static final Set<String> REQUIRED_PERCEPT_NAMES = Set.of(SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME);

    private Map<String, List<Percept>> filteredPerceptMap;

    // Team (shared) Percepts
    private long step;
    private int score;
    private int vision;
    private String teamName;
    private TaskList taskList;

    // Individual agent percepts
    private int energy;
    private boolean disabled;
    private List<Thing> thingList;
    private List<Terrain> terrainList;
    private String lastAction;
    private String lastActionResult;
    private ParameterList lastActionParams;


    private void setFilteredPerceptMap(Map<String, List<Percept>> filteredPerceptMap) {
        // Validate perceptions, check that required percepts exist in the map.
        if (!REQUIRED_PERCEPT_NAMES.stream().allMatch(filteredPerceptMap::containsKey))
            throw new RuntimeException("Invalid raw percepts, or percepts mapped incorrectly. Filtered Percepts: " + filteredPerceptMap.toString());

        this.filteredPerceptMap = filteredPerceptMap;
    }

    private PerceptContainer(Map<String, List<Percept>> filteredPerceptMap) {
        setFilteredPerceptMap(filteredPerceptMap);

        // Private constructor
        setStep();
        setScore();
        setVision();
        setTeamName();
        setEnergy();
        setDisabled();
        setLastActionInfo();

        // Set Other percept info
        setThingList();
        setTerrainList();
        setTaskList();
    }


    public long getStep() {
        return step;
    }

    public int getScore() {
        return score;
    }

    public int getVision() {
        return vision;
    }

    public String getTeamName() {
        return teamName;
    }

    public TaskList getTaskList() {
        return taskList;
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

    private void setStep() {
        this.step = parseSingleNumberPercept(filteredPerceptMap.get(STEP_PERCEPT_NAME)).longValue();
    }

    private void setScore() {
        this.score = parseSingleNumberPercept(filteredPerceptMap.get(SCORE_PERCEPT_NAME)).intValue();
    }

    private void setVision() {
        this.vision = parseSingleNumberPercept(filteredPerceptMap.get(VISION_PERCEPT_NAME)).intValue();
    }

    private void setTeamName() {
        this.teamName = parseSingleStringPercept(filteredPerceptMap.get(TEAM_PERCEPT_NAME));
    }

    private void setEnergy() {
        this.energy = parseSingleNumberPercept(filteredPerceptMap.get(ENERGY_PERCEPT_NAME)).intValue();
    }

    private void setDisabled() {
        this.disabled = parseSingleBooleanPercept(filteredPerceptMap.get(DISABLED_PERCEPT_NAME));
    }

    private void setThingList() {
        this.thingList = PerceptHandlerFactory.getThingPerceptHandler().mapAllPercepts(filteredPerceptMap.get(THING_PERCEPT_NAME));
    }

    private void setTerrainList() {
        this.terrainList = PerceptHandlerFactory.getTerrainPerceptHandler().mapAllPercepts(filteredPerceptMap.get(THING_PERCEPT_NAME));
    }

    private void setTaskList() {
        this.taskList = PerceptHandlerFactory.getTaskHandler().mapTaskList(filteredPerceptMap.get(TASK_PERCEPT_NAME));
    }

    private void setLastActionInfo() {

        this.lastAction = parseSingleStringPercept(filteredPerceptMap.get(LAST_ACTION_PERCEPT_NAME));
        this.lastActionResult = parseSingleStringPercept(filteredPerceptMap.get(LAST_ACTION_RESULT_PERCEPT_NAME));
        this.lastActionParams = parseSingleParameterListPercept(filteredPerceptMap.get(LAST_ACTION_PARAMS_PERCEPT_NAME));
    }

    public static PerceptContainer parsePercepts(List<Percept> rawPercepts) {

        Map<String, List<Percept>> filteredPerceptMap = new HashMap<>();

        rawPercepts.stream().filter(p -> VALID_PERCEPT_NAMES.contains(p.getName())).forEach(percept -> {
            // Get existing percept list (keyed by percept name)
            List<Percept> matchingPercepts = filteredPerceptMap.getOrDefault(percept.getName(), new ArrayList<>());
            matchingPercepts.add(percept);
            filteredPerceptMap.put(percept.getName(), matchingPercepts);
        });


        return new PerceptContainer(filteredPerceptMap);
    }

    private static Percept getSinglePercept(List<Percept> percepts) {
        if (percepts == null || percepts.size() != 1)
            throw new RuntimeException("Percepts are null or do not have a size of one. Percepts: " + percepts);

        return percepts.get(0);
    }

    private static Number parseSingleNumberPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetNumberParameter(percept, 0);
    }

    private static String parseSingleStringPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetStringParameter(percept, 0);
    }

    private static boolean parseSingleBooleanPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetBooleanParameter(percept, 0);
    }

    private static ParameterList parseSingleParameterListPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return (ParameterList) PerceptUtils.GetParameter(percept, 0);
    }
}
