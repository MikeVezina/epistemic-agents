package eis.percepts.things;

import eis.iilang.Percept;
import eis.percepts.ParsedPercept;
import utils.PerceptUtils;
import utils.Position;

public abstract class Thing extends ParsedPercept {

    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;
    private static final int TYPE_INDEX = 2;
    private static final int DETAILS_INDEX = 3;
    public static final String PERCEPT_NAME = "thing";

    private Position position;
    private String thingType;
    private String details;

    protected Thing(Position pos, String thingType, String details)
    {
        this.position = pos;
        this.thingType = thingType;
        this.details = details;
    }

    public String getThingType() {
        return thingType;
    }

    public String getDetails() {
        return details;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public abstract Thing clone();

    public abstract boolean isBlocking(Thing thing);

    public static Thing ParseThing(Percept l)
    {
        int x = PerceptUtils.GetNumberParameter(l, X_INDEX).intValue();
        int y = PerceptUtils.GetNumberParameter(l, Y_INDEX).intValue();
        String type = PerceptUtils.GetStringParameter(l, TYPE_INDEX);
        String details = PerceptUtils.GetStringParameter(l, DETAILS_INDEX);


        if(Entity.IsEntityPercept(type))
            return new Entity(x, y, details);

        if(Dispenser.IsObstaclePercept(type))
            return new Dispenser(x, y, details);

        if(Block.IsBlockPercept(type))
            return new Block(x, y, details);

        if(Marker.IsMarkerPercept(type))
            return new Marker(x, y, details);

        return null;
       // throw new RuntimeException("Unknown percept: " + l);
    }

    public static boolean canParse(Percept l)
    {
        return l != null && l.getName().equalsIgnoreCase(PERCEPT_NAME);
    }

    @Override
    public String toString() {
        return "(Type: " + getThingType() + ", Details: " + getDetails() + ")";
    }
}
