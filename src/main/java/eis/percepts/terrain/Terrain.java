package eis.percepts.terrain;

import eis.iilang.Percept;
import eis.percepts.ParsedPercept;
import utils.PerceptUtils;
import map.Position;

public abstract class Terrain extends ParsedPercept {

    private Position position;

    protected Terrain(Position pos)
    {
        this.position = pos;
    }

    protected Terrain(int x, int y)
    {
        this(new Position(x, y));
    }

    public Position getPosition() {
        return position;
    }

    public abstract Terrain clone();

    public void setPosition(Position position) {
        this.position = position;
    }

    public static Terrain parseTerrain(Percept l)
    {
        int x = PerceptUtils.GetNumberParameter(l, 0).intValue();
        int y = PerceptUtils.GetNumberParameter(l, 1).intValue();


        if(Goal.IsGoalPercept(l))
            return new Goal(x, y);

        if(Obstacle.IsObstaclePercept(l))
            return new Obstacle(x, y);

        throw new RuntimeException("Unknown percept: " + l);
    }

    public static boolean canParse(Percept l)
    {
        return Goal.IsGoalPercept(l) || Obstacle.IsObstaclePercept(l);
    }

    public abstract boolean isBlocking();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
