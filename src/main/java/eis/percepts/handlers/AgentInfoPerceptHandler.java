package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.AgentMap;
import eis.percepts.things.Entity;
import utils.PerceptUtils;

public class AgentInfoPerceptHandler extends PerceptHandler {

    private static final String PERCEPT_VISION = "vision";
    private static final String PERCEPT_TEAM = "team";

    public AgentInfoPerceptHandler(String agentSource) {
        super(agentSource);
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return p.getName().equals(PERCEPT_VISION) || p.getName().equals(PERCEPT_TEAM);
    }

    @Override
    public void perceptProcessingFinished() {
        for (Percept p : getCollectedPercepts()) {
            if (p.getName().equalsIgnoreCase(PERCEPT_VISION))
                AgentMap.setVision(PerceptUtils.GetNumberParameter(p, 0).intValue());

            if (p.getName().equals(PERCEPT_TEAM)) {
                if (Entity.getTeam() == null)
                    Entity.setTeam(PerceptUtils.GetStringParameter(p, 0));
            }
        }
    }
}
