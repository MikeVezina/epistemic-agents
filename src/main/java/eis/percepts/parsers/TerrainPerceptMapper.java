package eis.percepts.parsers;

import eis.iilang.Percept;
import eis.percepts.terrain.Terrain;

public class TerrainPerceptMapper extends PerceptMapper<Terrain> {

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Terrain.canParse(p);
    }

    @Override
    protected Terrain mapPercept(Percept p) {
        return Terrain.parseTerrain(p);
    }
}
