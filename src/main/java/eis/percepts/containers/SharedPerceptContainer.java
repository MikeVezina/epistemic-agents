package eis.percepts.containers;

import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.parsers.PerceptHandlerFactory;

import java.util.*;

public class SharedPerceptContainer extends PerceptContainer {
    public static final String STEP_PERCEPT_NAME = "step";
    public static final String SCORE_PERCEPT_NAME = "score";
    public static final String VISION_PERCEPT_NAME = "vision";
    public static final String TEAM_PERCEPT_NAME = "team";
    public static final String TASK_PERCEPT_NAME = Task.PERCEPT_NAME;

    // Required percept names (all raw percept lists should have these).
    private static final Set<String> REQUIRED_PERCEPT_NAMES = Set.of(SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME, STEP_PERCEPT_NAME);
    private static final Set<String> VALID_PERCEPT_NAMES = Set.of(SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME, STEP_PERCEPT_NAME, TASK_PERCEPT_NAME);

    // Team (shared) Percepts
    private long step = -1;
    private int score;
    private int vision;
    private String teamName;
    private TaskMap taskMap;

    protected SharedPerceptContainer(Map<String, List<Percept>> filteredPerceptMap) {
        super(filteredPerceptMap);

        // Check to see if the filtered percepts contain a step percept
        if(!filteredPerceptMap.containsKey(SharedPerceptContainer.STEP_PERCEPT_NAME))
            throw new InvalidPerceptCollectionException("No Step perception was parsed. Evaluate further to check if this is an issue with parsing percepts (or it might just be the initial simulation message)", true);

        setStep();
        setScore();
        setVision();
        setTeamName();
        setTaskList();
    }

    @Override
    protected Set<String> getRequiredPerceptNames() {
        return REQUIRED_PERCEPT_NAMES;
    }

    private void setStep() {
        this.step = parseSingleNumberPercept(getFilteredPerceptMap().get(STEP_PERCEPT_NAME)).longValue();
    }

    private void setScore() {
        this.score = parseSingleNumberPercept(getFilteredPerceptMap().get(SCORE_PERCEPT_NAME)).intValue();
    }

    private void setVision() {
        this.vision = parseSingleNumberPercept(getFilteredPerceptMap().get(VISION_PERCEPT_NAME)).intValue();
    }

    private void setTeamName() {
        this.teamName = parseSingleStringPercept(getFilteredPerceptMap().get(TEAM_PERCEPT_NAME));
    }

    private void setTaskList() {
        // We need the step percept by this point to filter out expired tasks.
        if(step < 0)
            throw new InvalidPerceptCollectionException("Failed to set Step before filtering task map.");

        this.taskMap = PerceptHandlerFactory.getTaskHandler().mapTaskList(getFilteredPerceptMap().get(TASK_PERCEPT_NAME), step);
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

    public TaskMap getTaskMap() {
        return taskMap;
    }

    public static SharedPerceptContainer parsePercepts(List<Percept> rawPercepts) {
        return new SharedPerceptContainer(filterValidPercepts(rawPercepts, VALID_PERCEPT_NAMES));
    }

}
