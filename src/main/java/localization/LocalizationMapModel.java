package localization;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class LocalizationMapModel extends GridWorldModel implements KeyListener {

    public static final int POSSIBLE = 16;
    public static final int GOAL = 8;
    private static final Atom OBSTACLE = new Atom("obstacle");
    private static final Atom NONE = new Atom("none");
    private static final int AGENT_IDX = 0;
    private List<MapEventListener> mapEventListeners;
    private List<Location> possibleLocations;

    public LocalizationMapModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        this.mapEventListeners = new ArrayList<>();
        this.possibleLocations = new ArrayList<>();
    }

    public void addMapListener(MapEventListener listener)
    {
        this.mapEventListeners.add(listener);
    }

    void populateModel(Location agentLoc) {
        this.add(GridWorldModel.OBSTACLE, new Location(1,2));
        this.add(GridWorldModel.OBSTACLE, new Location(2,2));

        this.setAgPos(AGENT_IDX, agentLoc);
        this.set(GOAL, 0,1);

        this.notifyListeners(agentLoc);
    }

    private synchronized void addPossible()
    {
        for(var location : possibleLocations)
            this.add(POSSIBLE, location);

        this.view.getCanvas().invalidate();
    }

    private synchronized void clearPossible()
    {
        for(var location : possibleLocations)
            this.remove(POSSIBLE, location);

        this.possibleLocations.clear();

    }

    public synchronized void setPossible(List<Location> newPossible)
    {
        this.clearPossible();
        this.possibleLocations.addAll(newPossible);
        this.addPossible();
    }

    @Override
    public void keyTyped(KeyEvent e) {

        if(e.getKeyChar() == 'w')
            moveUp();
        else if(e.getKeyChar() == 'a')
            moveLeft();
        else if(e.getKeyChar() == 's')
            moveDown();
        else if(e.getKeyChar() == 'd')
            moveRight();
    }

    private void notifyListeners(Location agentLoc)
    {
        List<Literal> newPercepts = getPercepts(agentLoc);

        for(var listener : mapEventListeners)
            listener.agentMoved(newPercepts);
    }

    private Atom getPerceptAtom(int x, int y)
    {
        Location loc = new Location(x, y);
        if(!inGrid(loc) || isFreeOfObstacle(loc))
            return NONE;

        return OBSTACLE;
    }

    private List<Literal> getPercepts(Location agentPos) {
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

    private void move(Location delta)
    {
        Location agentPos = this.getAgPos(AGENT_IDX);

        agentPos.x += delta.x;
        agentPos.y += delta.y;

        if(this.inGrid(agentPos) && this.isFree(agentPos))
        {
            this.setAgPos(0, agentPos);
            notifyListeners(agentPos);
        }

        this.view.getCanvas().invalidate();
    }

    public void moveLeft() {
        move(new Location(-1,0));
    }

    public void moveDown() {
        move(new Location(0,1));
    }

    public void moveUp() {
        move(new Location(0,-1));
    }

    public void moveRight() {
        move(new Location(1,0));
    }

    /**
     * Are locations left/right/up/down adjacent cells?
     * @param firstLoc
     * @param secondLoc
     * @return
     */
    public boolean isAdjacent(Location firstLoc, Location secondLoc) {
        if(!isFreeOfObstacle(firstLoc) || !isFreeOfObstacle(secondLoc))
            return false;

        return firstLoc.distance(secondLoc) == 1;
    }
}
