package eis.percepts.attachments;

import map.Position;
import eis.percepts.things.Entity;
import eis.percepts.things.Thing;

import java.util.ArrayList;
import java.util.List;

public class AttachedThing {
    public static final String ATTACHED_PERCEPT_NAME = "attached";
    private Thing underlyingThing;
    private Position attachedPosition;
    private List<Entity> connectedEntities;

    public AttachedThing(Position relativeAttachedPosition, Thing attachedThing)
    {
        this.connectedEntities = new ArrayList<>();
        this.underlyingThing = attachedThing;
        this.attachedPosition = relativeAttachedPosition;
    }

    public void addConnectedEntity(Entity connectedEntity)
    {
        this.connectedEntities.add(connectedEntity);
    }

    public Position getAttachedPosition() {
        return attachedPosition;
    }

    public Thing getThing() {
        return underlyingThing;
    }

    public List<Entity> getConnectedEntities() {
        return connectedEntities;
    }
}
