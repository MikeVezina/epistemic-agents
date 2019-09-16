package eis.percepts.things;

import eis.map.Position;

public class Entity extends Thing {

    private static final String THING_TYPE = "entity";

    protected Entity(Position pos, String team) {
        super(pos, THING_TYPE, team);
    }

    public Entity(int x, int y, String details) {
        this(new Position(x, y), details);
    }

    public boolean isOnTeam(String teamName) {
        return this.getDetails().equals(teamName);
    }

    @Override
    public Entity clone() {
        return new Entity(this.getPosition(), this.getDetails());
    }

    @Override
    public boolean isBlocking(Thing thing) {
        return thing instanceof Entity || (thing instanceof Block);
    }

    public static boolean IsEntityPercept(String l) {
        return l != null && l.equalsIgnoreCase(THING_TYPE);
    }
}
