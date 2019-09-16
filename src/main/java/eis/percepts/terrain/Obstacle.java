package eis.percepts.terrain;

import eis.iilang.Percept;
import eis.map.Position;

public class Obstacle extends Terrain {

    public static final String PERCEPT_NAME = "obstacle";

    protected Obstacle(Position pos)
    {
        super(pos);
    }

    public Obstacle(int x, int y)
    {
        super(x, y);
    }

    @Override
    public Terrain clone() {
        return new Obstacle(this.getPosition());
    }

    @Override
    public boolean isBlocking() {
        return true;
    }


    public static boolean IsObstaclePercept(Percept l)
    {
        return l != null && l.getName().equalsIgnoreCase(PERCEPT_NAME);
    }


}
