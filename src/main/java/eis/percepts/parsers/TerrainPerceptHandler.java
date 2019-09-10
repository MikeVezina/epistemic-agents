package eis.percepts.parsers;

import eis.iilang.Percept;
import eis.percepts.agent.AgentMap;
import eis.percepts.parsers.PerceptMapper;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;

import java.util.List;

public class TerrainPerceptHandler extends PerceptMapper<Terrain> {

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Terrain.canParse(p);
    }

    @Override
    protected Terrain mapPercept(Percept p) {
        return Terrain.parseTerrain(p);
    }
}
