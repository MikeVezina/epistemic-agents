package eis.percepts.terrain;

import map.Position;

public class FreeSpace extends Terrain {

    public FreeSpace(Position pos)
    {
        super(pos);
    }

    @Override
    public Terrain clone() {
        return new FreeSpace(getPosition());
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
