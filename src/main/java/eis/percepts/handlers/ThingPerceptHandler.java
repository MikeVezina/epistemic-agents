package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.AgentMap;
import eis.percepts.things.Thing;

public class ThingPerceptHandler extends PerceptHandler {
    private AgentMap agentMap;
    private Thing thingPercept;

    public ThingPerceptHandler(String agentName, AgentMap agentMap) {
        super(agentName);
        this.agentMap = agentMap;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Thing.canParse(p);
    }

    @Override
    public void processPercepts() {
        getCollectedPercepts();
    }
}
