package eis.percepts.things;

import eis.iilang.Percept;
import eis.percepts.terrain.Terrain;
import utils.Position;

public class Entity extends Thing {

    private static final String THING_TYPE = "entity";
    private static String TEAM;

    protected Entity(Position pos, String team)
    {
        super(pos, THING_TYPE, team);
    }

    public Entity(int x, int y, String details)
    {
        this(new Position(x, y), details);
    }

    public static void setTeam(String team)
    {
        TEAM = team;
    }

    public boolean isTeammate()
    {
        if(TEAM == null)
            return false;

        return TEAM.equalsIgnoreCase(this.getDetails());
    }

    @Override
    public Thing clone() {
        return new Entity(this.getPosition(), this.getDetails());
    }

    @Override
    public boolean isBlocking(Thing thing) {
        return thing instanceof Entity || thing instanceof Block;
    }

    public static boolean IsEntityPercept(String l)
    {
        return l != null && l.equalsIgnoreCase(THING_TYPE);
    }
}
