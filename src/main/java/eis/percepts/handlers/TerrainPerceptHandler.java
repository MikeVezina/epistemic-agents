package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.AgentMap;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;

import java.util.List;

public class TerrainPerceptHandler extends PerceptMapper<Terrain> {

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
    public void perceptProcessingFinished() {
        if(getMappedPercepts().size() < 11)
            System.out.println("Perceptions: " + getMappedPercepts().size());
    }

    public List<Terrain> getPerceivedTerrain()
    {
        return getMappedPercepts();
    }


    @Override
    public Terrain mapPercept(Percept p) {
        return Terrain.parseTerrain(p);
    }
}
