package localization.models;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import jason.asSyntax.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import localization.MapEventListener;
import localization.view.LocalizationMapView;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class LocalizationMapModel extends GridWorldModel implements KeyListener {

    public static final int POSSIBLE = 16;
    public static final int GOAL = 8;
    private static final Atom OBSTACLE = new Atom("obstacle");
    public static final Atom NONE = new Atom("none");
    private static final int AGENT_IDX = 0;


    private List<MapEventListener> mapEventListeners;
    private List<Location> possibleLocations;
    private List<Location> goalLocations;


    private Location lastPosition;

    public LocalizationMapModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.goalLocations = new ArrayList<>();
        this.mapEventListeners = new ArrayList<>();
        this.possibleLocations = new ArrayList<>();
    }

    public LocalizationMapModel(LocalizationMap map) {
        this(map.getWidth(), map.getHeight(), 1);
        this.setAgPos(AGENT_IDX, map.getAgentStart());
        for (var marker : map.getMarkers()) {
            if (marker.getType() == GOAL)
                goalLocations.add(marker.getLocation());
            this.add(marker.getType(), marker.getLocation().x, marker.getLocation().y);
        }

        this.dumpMapBeliefs();
    }

    public void addMapListener(MapEventListener listener) {
        this.mapEventListeners.add(listener);

        // Notify the listener of the current agent position
        this.notifyListeners(this.getAgPos(AGENT_IDX));
    }

    public Atom getLastDirection() {
        if (lastPosition == null)
            return NONE;

        return getDirectionAtom(lastPosition, getAgPos(AGENT_IDX));
    }

    @Override
    public void setAgPos(int ag, Location l) {

        // Set last position if agent 0
        if (ag == AGENT_IDX)
            this.lastPosition = getAgPos(ag);

        super.setAgPos(ag, l);
    }

    private Location getNearestGoal(Location start) {
        int minDist = Integer.MAX_VALUE;
        Location minGoal = null;

        for (var goal : goalLocations) {
            int dist = goal.distance(start);
            if (dist < minDist) {
                minDist = dist;
                minGoal = goal;
            }
        }

        return minGoal;
    }

    private List<Literal> getNearestGoalDirections(Location curLocation) {
        List<Literal> goalDirections = new ArrayList<>();
        Location nearestGoal = getNearestGoal(curLocation);
        Location goalDelta = new Location(nearestGoal.x - curLocation.x, nearestGoal.y - curLocation.y);

        if (goalDelta.equals(new Location(0, 0))) {
            goalDirections.add(ASSyntax.createAtom("none"));
            return goalDirections;
        }

        if (goalDelta.x > 0)
            goalDirections.add(ASSyntax.createAtom("right"));
        if (goalDelta.x < 0)
            goalDirections.add(ASSyntax.createAtom("left"));
        if (goalDelta.y < 0)
            goalDirections.add(ASSyntax.createAtom("up"));
        if (goalDelta.y > 0)
            goalDirections.add(ASSyntax.createAtom("down"));


        return goalDirections;
    }

    private synchronized void addPossible() {
        for (var location : possibleLocations)
            this.add(POSSIBLE, location);

        this.view.getCanvas().invalidate();
    }

    private synchronized void clearPossible() {
        for (var location : possibleLocations)
            this.remove(POSSIBLE, location);

        this.possibleLocations.clear();

    }

    public synchronized void setPossible(List<Location> newPossible) {
        this.clearPossible();
        this.possibleLocations.addAll(newPossible);
        this.addPossible();
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if (e.getKeyChar() == 'w')
            moveUp();
        else if (e.getKeyChar() == 'a')
            moveLeft();
        else if (e.getKeyChar() == 's')
            moveDown();
        else if (e.getKeyChar() == 'd')
            moveRight();
    }

    private void notifyListeners(Location agentLoc) {
        List<Literal> newPercepts = getPercepts(agentLoc);
        Atom moveDirection = getLastDirection();

        for (var listener : mapEventListeners)
            listener.agentMoved(new MapEvent(this, agentLoc, moveDirection));
    }

    private Atom getPerceptAtom(int x, int y) {
        Location loc = new Location(x, y);
        if (!inGrid(loc) || isFreeOfObstacle(loc))
            return NONE;

        return OBSTACLE;
    }

    private List<Location> getAllLocations() {
        List<Location> allLocations = new ArrayList<>();
        for (int x = 0; x < this.getWidth(); x++)
            for (int y = 0; y < this.getHeight(); y++)
                allLocations.add(new Location(x, y));
        return allLocations;
    }

    public void dumpMapBeliefs() {
        Map<Location, Literal> locationPercepts = new LinkedHashMap<>();
        Map<Location, Literal> adjBeliefs = new LinkedHashMap<>();
        Map<Location, Literal> dirBeliefs = new LinkedHashMap<>();

        for (Location location : getAllLocations()) {
            if (!inGrid(location) || !isFreeOfObstacle(location))
                continue;

            locationPercepts.put(location, getLocationPercepts(location));
            adjBeliefs.put(location, getAdjacentBelief(location));

            Literal dirListTerm = getDirectionsToGoal(location);
            dirBeliefs.put(location, dirListTerm);
        }

        printLocationBeliefs("Map Location Mappings", locationPercepts.values());
        printLocationBeliefs("Adjacent Location Mappings", adjBeliefs.values());
        printLocationBeliefs("Location Direction Mappings", dirBeliefs.values());
    }

    private void printLocationBeliefs(String heading, Collection<Literal> mapping) {
        System.out.println("// " + heading);

        // Print beliefs one X coordinate at a time
        for (var belief : mapping)
            System.out.println(belief.toString() + ".");

        System.out.println();
    }

    @NotNull
    private Literal getDirectionsToGoal(Location location) {
        var dirListTerm = new ListTermImpl();
        dirListTerm.addAll(getNearestGoalDirections(location));
        return ASSyntax.createLiteral("locDirToGoal", getLocationLiteral(location), dirListTerm);
    }

    private Literal getLocationPercepts(Location location) {
        Literal locationLit = getLocationLiteral(location);
        List<Literal> locationPercepts = getPercepts(location);

        // Get percepts for this location
        var percepts = getPercepts(location);

        // Add Percept beliefs
        return ASSyntax.createLiteral("locPercept", locationLit, percepts.get(3), percepts.get(2), percepts.get(0), percepts.get(1));
    }

    private Literal getAdjacentBelief(Location location) {
        Literal locationLit = getLocationLiteral(location);
        var adjacent = getAdjacentLocations(location);

        var adjListTerm = new ListTermImpl();
        adjListTerm
                .addAll(adjacent.stream().map((adj) -> {
                    Literal adjLocLit = getLocationLiteral(adj);
                    Atom dirAtom = getDirectionAtom(location, adj);

                    return ASSyntax.createLiteral("adjacent", dirAtom, adjLocLit);
                }).collect(Collectors.toSet()));
        return ASSyntax.createLiteral("locAdjacent", locationLit, adjListTerm);
    }

    private Atom getDirectionAtom(Location src, Location dst) {
        Location delta = new Location(dst.x - src.x, dst.y - src.y);
        if ((Math.abs(delta.x) != 1 && Math.abs(delta.y) != 1) || (delta.x == delta.y)) {
            System.out.println("Invalid Direction? " + delta);
            throw new NullPointerException();
        }

        if (delta.x == 1)
            return ASSyntax.createAtom("right");
        if (delta.x == -1)
            return ASSyntax.createAtom("left");
        if (delta.y == -1)
            return ASSyntax.createAtom("up");
        if (delta.y == 1)
            return ASSyntax.createAtom("down");

        throw new NullPointerException("Huh?");
    }

    private Literal getLocationLiteral(Location location) {
        return ASSyntax.createLiteral("location", ASSyntax.createNumber(location.x), ASSyntax.createNumber(location.y));
    }

    private Set<Location> getAdjacentLocations(Location current) {
        int x = current.x;
        int y = current.y;

        Set<Location> adjSet = new HashSet<>();

        Location left = new Location(x - 1, y);
        Location right = new Location(x + 1, y);
        Location up = new Location(x, y - 1);
        Location down = new Location(x, y + 1);
        if (isAdjacent(current, left))
            adjSet.add(left);
        if (isAdjacent(current, right))
            adjSet.add(right);
        if (isAdjacent(current, up))
            adjSet.add(up);
        if (isAdjacent(current, down))
            adjSet.add(down);


        return adjSet;
    }

    public static LocalizationMapModel loadFromFile() {
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader("map.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load map!", e);
        }
        LocalizationMap map = gson.fromJson(reader, LocalizationMap.class);
        System.out.println(map);

        return new LocalizationMapModel(map);
    }

    public List<Literal> getPercepts(Location agentPos) {
        int x = agentPos.x;
        int y = agentPos.y;

        // Get directional percepts
        var arrList = new ArrayList<Literal>();

        arrList.add(ASSyntax.createLiteral("up", getPerceptAtom(x, y - 1)));
        arrList.add(ASSyntax.createLiteral("down", getPerceptAtom(x, y + 1)));
        arrList.add(ASSyntax.createLiteral("right", getPerceptAtom(x + 1, y)));
        arrList.add(ASSyntax.createLiteral("left", getPerceptAtom(x - 1, y)));

        return arrList;
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private synchronized void move(Location delta) {
        Location agentPos = this.getAgPos(AGENT_IDX);

        agentPos.x += delta.x;
        agentPos.y += delta.y;

        if (this.inGrid(agentPos) && this.isFree(agentPos)) {
            this.setAgPos(0, agentPos);
            notifyListeners(agentPos);
        }

        this.view.invalidate();
    }

    public void moveLeft() {
        move(new Location(-1, 0));
    }

    public void moveDown() {
        move(new Location(0, 1));
    }

    public void moveUp() {
        move(new Location(0, -1));
    }

    public void moveRight() {
        move(new Location(1, 0));
    }

    /**
     * Are locations left/right/up/down adjacent cells?
     *
     * @param firstLoc
     * @param secondLoc
     * @return
     */
    public boolean isAdjacent(Location firstLoc, Location secondLoc) {
        if (!isFreeOfObstacle(firstLoc) || !isFreeOfObstacle(secondLoc))
            return false;

        return firstLoc.distance(secondLoc) == 1;
    }

    public LocalizationMapView getView() {
        return (LocalizationMapView) this.view;
    }
}