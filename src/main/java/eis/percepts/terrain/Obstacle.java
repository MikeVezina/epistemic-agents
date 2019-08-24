package eis.percepts.terrain;

import eis.iilang.Percept;
import utils.Position;

public class Obstacle extends Terrain {

    protected Obstacle(Position pos)
    {
        super(pos);
    }

    protected Obstacle(int x, int y)
    {
        super(x, y);
    }


    public static boolean IsObstaclePercept(Percept l)
    {
        return l != null && l.getName().equalsIgnoreCase("obstacle");
    }


}
