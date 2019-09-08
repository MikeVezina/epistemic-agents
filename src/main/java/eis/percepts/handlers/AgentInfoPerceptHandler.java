package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.agent.AgentMap;
import eis.percepts.things.Entity;
import utils.PerceptUtils;

public class AgentInfoPerceptHandler extends PerceptHandler {

    private static final String PERCEPT_VISION = "vision";
    private static final String PERCEPT_TEAM = "team";
    private static final String PERCEPT_SCORE = "score";

    private long score;
    private String team;
    private int vision;

    public AgentInfoPerceptHandler() {
        this(null);
    }

    public AgentInfoPerceptHandler(String agentName) {
        super(agentName);
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return p.getName().equals(PERCEPT_VISION) || p.getName().equals(PERCEPT_TEAM) || p.getName().equals(PERCEPT_SCORE);
    }

    @Override
    public void perceptProcessingFinished() {
        for (Percept p : getCollectedPercepts()) {
            if (p.getName().equalsIgnoreCase(PERCEPT_VISION))
                vision = PerceptUtils.GetNumberParameter(p, 0).intValue();

            if (p.getName().equals(PERCEPT_TEAM))
                team = PerceptUtils.GetStringParameter(p, 0);

            if (p.getName().equals(PERCEPT_SCORE))
                score = PerceptUtils.GetNumberParameter(p, 0).longValue();
        }
    }

    public long getScore() {
        return score;
    }

    public String getTeam() {
        return team;
    }

    public int getVision() {
        return vision;
    }
}
