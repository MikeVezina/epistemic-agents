package eis.percepts.things;

import eis.percepts.agent.StaticInfo;
import utils.Position;

public class Entity extends Thing {

    private static final String THING_TYPE = "entity";

    protected Entity(Position pos, String team) {
        super(pos, THING_TYPE, team);
    }

    public Entity(int x, int y, String details) {
        this(new Position(x, y), details);
    }


    public boolean isSameTeam() {
        String team = StaticInfo.getInstance().getTeam();
        return team.equalsIgnoreCase(this.getDetails());
    }

    @Override
    public Thing clone() {
        return new Entity(this.getPosition(), this.getDetails());
    }

    @Override
    public boolean isBlocking(Thing thing) {
        return thing instanceof Entity || thing instanceof Block;
    }

    public static boolean IsEntityPercept(String l) {
        return l != null && l.equalsIgnoreCase(THING_TYPE);
    }
}
