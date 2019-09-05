package eis.percepts.handlers;

import eis.iilang.Numeral;
import eis.iilang.Percept;
import eis.percepts.AgentMap;
import eis.percepts.things.Entity;
import utils.PerceptUtils;

public class AgentInfoPerceptHandler extends PerceptHandler {

    private String agentName;
    private String agentTeam;

    public AgentInfoPerceptHandler(String agentSource) {
        super(agentSource);
        agentName = agentSource;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return p.getName().equals("name") || p.getName().equals("team");
    }

    @Override
    public void processPercepts() {
        for (Percept p : getCollectedPercepts()) {
            if (AgentMap.GetVision() == -1 && p.getName().equalsIgnoreCase("vision"))
                AgentMap.SetVision(PerceptUtils.GetNumberParameter(p, 0).intValue());

            if (p.getName().equals("team")) {
                if (Entity.getTeam() == null)
                    Entity.setTeam(PerceptUtils.GetStringParameter(p, 0));
            }
        }
    }
}
