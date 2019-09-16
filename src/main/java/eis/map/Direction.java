package eis.map;

import jason.asSyntax.Atom;

public enum Direction {
    NONE(0, 0, null),
    NORTH(0, -1, new Atom("n")),
    SOUTH(0, 1, new Atom("s")),
    WEST(-1, 0, new Atom("w")),
    EAST(1, 0, new Atom("e"));

    private Position position;
    private Atom atom;

    Direction(int x, int y, Atom atom) {
        position = new Position(x, y); this.atom = atom;
    }

    public Position getPosition() {
        return position;
    }

    public Atom getAtom()
    {
        return atom;
    }

    public static Direction GetDirection(int x, int y) {
        return GetDirection(new Position(x, y));
    }

    public static Direction GetDirection(Position position) {
        for (Direction dir : Direction.values()) {
            if (dir.getPosition().equals(position))
                return dir;
        }
        return NONE;
    }

    public static Direction[] validDirections() {
        return new Direction[] {NORTH, SOUTH, EAST, WEST};
    }

    public Position multiply(int scalar) {
        return getPosition().multiply(scalar);
    }
}
