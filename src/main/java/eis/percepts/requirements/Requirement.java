package eis.percepts.requirements;

import eis.iilang.*;
import eis.map.Position;

import java.util.Objects;

public class Requirement {

    // Requirement Parsing Index
    private static final int INDEX_BLOCK = 2;
    private static final int INDEX_Y = 1;
    private static final int INDEX_X = 0;
    private static final String REQ_FUNCTION_NAME = "req";
    private static final int REQ_FUNCTION_PARAM_SIZE = 3;
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
        if (!(o instanceof Requirement)) return false;
        Requirement that = (Requirement) o;
        return position.equals(that.position) &&
                blockType.equals(that.blockType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, blockType);
    }

    public static Requirement parseRequirementParam(Function p)
    {
        if(!p.getName().equalsIgnoreCase(REQ_FUNCTION_NAME) || p.getParameters().size() != REQ_FUNCTION_PARAM_SIZE)
            throw new RuntimeException("Could not parse requirement: " + p.toProlog());

        int x = ((Numeral) p.getParameters().get(INDEX_X)).getValue().intValue();
        int y = ((Numeral) p.getParameters().get(INDEX_Y)).getValue().intValue();
        String block = ((Identifier) p.getParameters().get(INDEX_BLOCK)).getValue();
        return new Requirement(x, y, block);
    }
}
