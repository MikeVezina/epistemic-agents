package utils;

import java.util.Objects;

public class Requirement {

    private Position position;
    private String blockType;

    public Requirement(Position position, String blockType)
    {
        this.position = position;
        this.blockType = blockType;
    }

    public Requirement(int x, int y, String blockType)
    {
        this.position = new Position(x, y);
        this.blockType = blockType;
    }

    public String getBlockType() {
        return blockType;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString()
    {
        return this.position.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Requirement that = (Requirement) o;
        return position.equals(that.position) &&
                blockType.equals(that.blockType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, blockType);
    }
}
