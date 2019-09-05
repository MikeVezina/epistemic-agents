package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.AgentLocation;

public class AgentLocationPerceptHandler extends PerceptHandler {
    private AgentLocation agentLocation;

    public AgentLocationPerceptHandler(String agentName, AgentLocation agentLocation)
    {
        super(agentName);
        this.agentLocation = agentLocation;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        for(AgentLocation.ActionResultPerception actionPercept : AgentLocation.ActionResultPerception.values())
        {
            if(actionPercept.getPerceptName().equals(p.getName()))
                return true;
        }
        return false;
    }

    @Override
    public void processPercepts() {
        System.out.println(getCollectedPercepts());
    }
}
