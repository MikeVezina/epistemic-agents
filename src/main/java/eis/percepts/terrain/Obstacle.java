package eis.percepts.terrain;

import eis.iilang.Percept;
import utils.Position;

public class Obstacle extends Terrain {

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
        return l != null && l.getName().equalsIgnoreCase("obstacle");
    }


}
