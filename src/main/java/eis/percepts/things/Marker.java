package eis.percepts.things;

import eis.map.Position;

public class Marker extends Thing {

    private static final String THING_TYPE = "marker";

    protected Marker(Position pos, String details)
    {
        super(pos, THING_TYPE, details);
    }

    protected Marker(int x, int y, String details)
    {
        this(new Position(x, y), details);
    }

    @Override
    public Marker clone() {
        return new Marker(this.getPosition(), this.getDetails());
    }

    @Override
    public boolean isBlocking(Thing thing) {
        return false;
    }

    public static boolean IsMarkerPercept(String l)
    {
        return l != null && l.equalsIgnoreCase(THING_TYPE);
    }
}
