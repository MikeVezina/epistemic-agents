package utils;

public enum Direction {
    NONE(0, 0),
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private Position position;

    Direction(int x, int y) {
        position = new Position(x, y);
    }

    public Position getPosition() {
        return position;
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
}
