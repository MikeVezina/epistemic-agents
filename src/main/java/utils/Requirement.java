package utils;

public class Requirement {

    private Position position;
    private String blockType;

    public Requirement(Position position, String blockType)
    {
        this.position = position;
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
}
