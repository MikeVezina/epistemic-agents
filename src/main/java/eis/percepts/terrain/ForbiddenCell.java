package eis.percepts.terrain;

import eis.map.Position;

public class ForbiddenCell extends Terrain {
    public ForbiddenCell(Position pos)
    {
        super(pos);
    }

    public ForbiddenCell(int x, int y)
    {
        super(x, y);
    }

    @Override
    public Terrain clone() {
        return new ForbiddenCell(this.getPosition());
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
