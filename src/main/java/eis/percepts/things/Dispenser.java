package eis.percepts.things;

import map.Position;

public class Dispenser extends Thing {

    private static final String THING_TYPE = "dispenser";

    protected Dispenser(Position pos, String details)
    {
        super(pos, THING_TYPE, details);
    }

    protected Dispenser(int x, int y, String details)
    {
        this(new Position(x, y), details);
    }

    @Override
    public Dispenser clone() {
        return new Dispenser(this.getPosition(), this.getDetails());
    }

    @Override
    public boolean isBlocking(Thing thing) {
        return thing instanceof Block;
    }

    public static boolean IsObstaclePercept(String l)
    {
        return l != null && l.equalsIgnoreCase(THING_TYPE);
    }


}
