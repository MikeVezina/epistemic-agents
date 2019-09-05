package eis.percepts.handlers;

import eis.iilang.Percept;
import utils.PerceptUtils;

public class AgentInfoPerceptHandler extends PerceptHandler {

    private String agentName;
    private String agentTeam;

    protected AgentInfoPerceptHandler(String agentSource) {
        super(agentSource);
        agentName = agentSource;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return p.getName().equals("name") || p.getName().equals("team");
    }

    @Override
    public void processPercepts() {
        for(Percept p : getCollectedPercepts())
        {
            if(p.getName().equals("team"))
            {
                agentTeam = PerceptUtils.GetStringParameter(p, 0);
            }
        }
    }
}
