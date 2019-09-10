package eis.percepts.containers;

import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.parsers.PerceptHandlerFactory;

import java.util.*;

public class SharedPerceptContainer extends PerceptContainer {
    private static final String STEP_PERCEPT_NAME = "step";
    private static final String SCORE_PERCEPT_NAME = "score";
    private static final String VISION_PERCEPT_NAME = "vision";
    private static final String TEAM_PERCEPT_NAME = "team";
    private static final String TASK_PERCEPT_NAME = Task.PERCEPT_NAME;

    // Required percept names (all raw percept lists should have these).
    private static final Set<String> REQUIRED_PERCEPT_NAMES = Set.of(SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME, STEP_PERCEPT_NAME);
    private static final Set<String> VALID_PERCEPT_NAMES = Set.of(SCORE_PERCEPT_NAME, VISION_PERCEPT_NAME, TEAM_PERCEPT_NAME, STEP_PERCEPT_NAME, TASK_PERCEPT_NAME);

    // Team (shared) Percepts
    private long step;
    private int score;
    private int vision;
    private String teamName;
    private TaskSet taskSet;

    protected SharedPerceptContainer(Map<String, List<Percept>> filteredPerceptMap) {
        super(filteredPerceptMap);

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
        this.taskSet = PerceptHandlerFactory.getTaskHandler().mapTaskList(getFilteredPerceptMap().get(TASK_PERCEPT_NAME));
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

    public TaskSet getTaskSet() {
        return taskSet;
    }

    public static SharedPerceptContainer parsePercepts(List<Percept> rawPercepts) {
        return new SharedPerceptContainer(filterValidPercepts(rawPercepts, VALID_PERCEPT_NAMES));
    }

}
