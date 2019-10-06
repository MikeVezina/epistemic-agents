package map;


import eis.agent.AgentContainer;
import serializers.GsonInstance;
import eis.percepts.attachments.Attachable;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class should contain the type of the terrain, the absolute position, and the last perceived step
 */
public class MapPercept {
    private Position location;
    private String agentSource;
    private Terrain terrain;
    private List<Thing> thingList;
    private long lastStepPerceived;
    private String teamName;

    // Copy constructor
    private MapPercept(MapPercept percept) {
        this(percept.getLocation().clone(), percept.agentSource, percept.teamName, percept.lastStepPerceived);
        this.setTerrain(percept.terrain);
        this.setThingList(percept.thingList);
    }

    /**
     * Create a map percept.
     * @param location The ABSOLUTE location of the perception
     * @param agentContainer The container that owns the percept
     * @param lastStepPerceived The step that this object was perceived
     */
    public MapPercept(Position location, AgentContainer agentContainer, long lastStepPerceived) {
        this(location, agentContainer.getAgentName(), agentContainer.getSharedPerceptContainer().getTeamName(), lastStepPerceived);
        thingList = new ArrayList<>();
        this.location = location;
        this.agentSource = agentContainer.getAgentName();
        this.lastStepPerceived = lastStepPerceived;
    }

    public MapPercept(Position location, String agentSource, String team, long lastStepPerceived) {
        thingList = new ArrayList<>();
        this.location = location;
        this.agentSource = agentSource;
        this.teamName = team;
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

    public void addThing(Thing thing) {
        if (thing == null)
            return;

        thingList.add(thing);
    }

    public void setAgentSource(String agentSource) {
        this.agentSource = agentSource;
    }

    public void setLastStepPerceived(long lastStepPerceived) {
        this.lastStepPerceived = lastStepPerceived;
    }

    public boolean hasEntity() {
        return thingList.stream().anyMatch(t -> t instanceof Entity);
    }

    public boolean hasTeamEntity() {
        return thingList.stream().anyMatch(t -> t instanceof Entity && ((Entity) t).isOnTeam(teamName));
    }

    public boolean hasEnemyEntity() {
        return thingList.stream().anyMatch(t -> t instanceof Entity && !((Entity) t).isOnTeam(teamName));
    }

    public boolean hasDispenser() {
        return thingList.stream().anyMatch(t -> t instanceof Dispenser);
    }

    public boolean hasMarker() {
        return thingList.stream().anyMatch(t -> t instanceof Marker);
    }

    public boolean hasBlock() {
        return thingList.stream().anyMatch(t -> t instanceof Block);
    }

    public boolean hasThing(String type) {
        return thingList.stream().anyMatch(t -> t.getThingType().equals(type));
    }

    public boolean hasThing(String type, String details) {
        return thingList.stream().anyMatch(t -> t.getThingType().equals(type) && t.getDetails().equals(details));
    }

    /**
     * @return Any Thing that implements the attachable interface, or null if there are no attachable Things
     */
    public Thing getAttachableThing()
    {
        return thingList.stream().filter(t -> t instanceof Attachable).findAny().orElse(null);
    }

    public void setTerrain(Terrain terrain) {
        if (terrain == null) {
            this.terrain = null;
            return;
        }

        this.terrain = terrain.clone();
        setLocation(this.location);
    }

    public void setThingList(List<Thing> thingList) {
        if (thingList == null)
            return;

        this.thingList.clear();

        for (Thing thing : thingList)
            this.thingList.add(thing.clone());

        setLocation(this.location);
    }

    private boolean isTerrainBlocking() {
        return (terrain != null && terrain.isBlocking());
    }

    private boolean isThingBlocking(Thing otherThing) {
        if (otherThing == null)
            return false;

        // Check to see if any of our things block the other thing
        return thingList.stream().anyMatch(otherThing::isBlocking);
    }

    public boolean isBlocking(MapPercept otherPercept) {
        // This instance can not block itself.
        if (this == otherPercept)
            return false;

        if (otherPercept == null)
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
        for (Thing t : thingList)
            t.setPosition(newPos);

        if (terrain != null)
            terrain.setPosition(newPos);

        this.location = newPos;
    }

    public String getAgentSource() {
        return agentSource;
    }

    public boolean isExpired(long curStep) {
        return curStep - lastStepPerceived > 20;
    }

    public String toJsonString() {
        return GsonInstance.getInstance().toJson(this);
    }

    @Override
    public String toString() {
        return location + ", Source: " + agentSource + ". Thing: " + thingList.toString() + ". Terrain: " + terrain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapPercept)) return false;

        MapPercept percept = (MapPercept) o;

        return lastStepPerceived == percept.lastStepPerceived &&
                location.equals(percept.location) &&
                agentSource.equals(percept.agentSource) &&
                terrain.equals(percept.terrain) &&
                thingList.equals(percept.thingList) &&
                teamName.equals(percept.teamName);
    }

    @Override
    public int hashCode() {
        // We only need the location for the hash of this percept. Two percepts should not reside at the same location.
        return Objects.hash(location);
    }

    public Block getBlock() {
        return thingList.stream().filter(t -> t instanceof Block).map(t -> (Block) t).findAny().orElse(null);
    }

    public Entity getEntity() {
        return thingList.stream().filter(t -> t instanceof Entity).map(e -> (Entity) e).findAny().orElse(null);
    }
}
