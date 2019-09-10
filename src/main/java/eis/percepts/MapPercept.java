package eis.percepts;


import eis.percepts.terrain.Terrain;
import eis.percepts.things.*;
import utils.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * This class should contain the type of the terrain, the absolute position, and the last perceived step
 */
public class MapPercept {
    private Position location;
    private String agentSource;
    private Terrain terrain;
    private List<Thing> thingList;
    private long lastStepPerceived;

    public MapPercept(MapPercept percept)
    {
        this(percept.getLocation().clone(), percept.agentSource, percept.lastStepPerceived);
        this.setTerrain(percept.terrain);
        this.setThingList(percept.thingList);
    }

    public MapPercept(Position location, String agentSource, long lastStepPerceived)
    {
        thingList = new ArrayList<>();
        this.location = location;
        this.agentSource = agentSource;
        this.lastStepPerceived = lastStepPerceived;
    }

    public Position getLocation() {
        return location;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public List<Thing> getThingList() {
        return thingList;
    }

    public void addThing(Thing thing)
    {
        if(thing == null)
            return;

        thingList.add(thing);
    }

    public boolean hasEntity()
    {
        return thingList.stream().anyMatch(t -> t instanceof Entity);
    }

    public boolean hasTeamEntity(Entity otherEntity)
    {
        return thingList.stream().anyMatch(t -> t instanceof Entity && ((Entity) t).isSameTeam(otherEntity));
    }

    public boolean hasEnemyEntity(Entity otherEntity)
    {
        return thingList.stream().anyMatch(t -> t instanceof Entity && !((Entity) t).isSameTeam(otherEntity));
    }

    public boolean hasDispenser()
    {
        return thingList.stream().anyMatch(t -> t instanceof Dispenser);
    }

    public boolean hasMarker()
    {
        return thingList.stream().anyMatch(t -> t instanceof Marker);
    }

    public boolean hasBlock()
    {
        return thingList.stream().anyMatch(t -> t instanceof Block);
    }

    public boolean hasThing(String type) { return thingList.stream().anyMatch(t -> t.getType().equals(type));}

    public boolean hasThing(String type, String details) { return thingList.stream().anyMatch(t -> t.getType().equals(type) && t.getDetails().equals(details));}

    public void setTerrain(Terrain terrain) {
        if(terrain == null)
        {
            this.terrain = null;
            return;
        }

        this.terrain = terrain.clone();
        setLocation(this.location);
    }

    public void setThingList(List<Thing> thingList) {
        if(thingList == null)
            return;

        this.thingList.clear();

        for(Thing thing : thingList)
            this.thingList.add(thing.clone());

        setLocation(this.location);
    }

    private boolean isTerrainBlocking()
    {
        return (terrain != null && terrain.isBlocking());
    }

    private boolean isThingBlocking(Thing otherThing)
    {
        if(otherThing == null)
            return false;

        // Check to see if any of our things block the other thing
        return thingList.stream().anyMatch(otherThing::isBlocking);
    }

    public boolean isBlocking(MapPercept otherPercept)
    {
        // This instance can not block itself.
        if(this == otherPercept)
            return false;

        if(otherPercept == null)
            return isTerrainBlocking();

        return isTerrainBlocking() || otherPercept.getThingList().stream().anyMatch(this::isThingBlocking);
    }

    public long getLastStepPerceived() {
        return lastStepPerceived;
    }

    public MapPercept copyToAgent(Position translation) {
        MapPercept newPercept = new MapPercept(this);
        newPercept.setLocation(this.getLocation().subtract(translation));

        return newPercept;
    }

    public void setLocation(Position newPos) {
        for(Thing t : thingList)
            t.setPosition(newPos);

        if(terrain != null)
            terrain.setPosition(newPos);

        this.location = newPos;
    }

    public String getAgentSource() {
        return agentSource;
    }

    public boolean isExpired(long curStep) {
        return curStep - lastStepPerceived > 20;
    }

    @Override
    public String toString()
    {
        return location + ", Source: " + agentSource + ". Thing: " + thingList.toString() + ". Terrain: " + terrain;
    }

    public Block getBlock() {
        return thingList.stream().filter(t -> t instanceof Block).map(t -> (Block)t).findAny().orElse(null);
    }
}
