package eis.percepts.terrain;

import eis.iilang.Percept;
import utils.Position;

public class Goal extends Terrain {

    protected Goal(Position pos)
    {
        super(pos);
    }

    protected Goal(int x, int y)
    {
        super(x, y);
    }

    @Override
    public Terrain clone() {
        return new Obstacle(this.getPosition());
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static boolean IsGoalPercept(Percept l)
    {
        return l != null && l.getName().equalsIgnoreCase("goal");
    }
}
