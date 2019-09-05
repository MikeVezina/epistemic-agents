package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.AgentMap;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;

public class TerrainPerceptHandler extends PerceptHandler {
    private AgentMap agentMap;

    public TerrainPerceptHandler(String agentName, AgentMap agentMap) {
        super(agentName);
        this.agentMap = agentMap;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Terrain.canParse(p);
    }

    @Override
    public void processPercepts() {
        getCollectedPercepts();
    }
}
