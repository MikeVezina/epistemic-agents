import jason.NoValueException;
import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import localization.LocalizationMapView;
import localization.LocalizationMapModel;
import localization.MapEventListener;

import java.util.ArrayList;
import java.util.List;

public class LocalizationMapEnvironment extends Environment implements MapEventListener {


    private static final Atom LEFT_ATOM = ASSyntax.createAtom("left");
    private static final Atom RIGHT_ATOM = ASSyntax.createAtom("right");
    private static final Atom DOWN_ATOM = ASSyntax.createAtom("down");
    private static final Atom UP_ATOM = ASSyntax.createAtom("up");
    private final LocalizationMapView localizationMapView;
    private final LocalizationMapModel localizationMapModel;
    private List<Literal> curPercepts;
    private boolean hasMoved = false;

    public LocalizationMapEnvironment() {
        this.curPercepts = new ArrayList<>();
        localizationMapView = new LocalizationMapView();
        localizationMapModel = localizationMapView.getModel();
        localizationMapModel.addMapListener(this);

        localizationMapView.populateModel(new Location(2, 0));


        localizationMapView.setVisible(true);
    }

    @Override
    public boolean executeAction(String agName, Structure act) {

        if (act.getFunctor().equals("updatePossible"))
           return updatePossibleLocations(act);


        if (act.getFunctor().equals("move"))
            return moveAction(act);


        return super.executeAction(agName, act);
    }

    private boolean moveAction(Structure act) {
        if (act.getTerm(0).equals(LEFT_ATOM))
            localizationMapModel.moveLeft();
        else if (act.getTerm(0).equals(RIGHT_ATOM))
            localizationMapModel.moveRight();
        else if (act.getTerm(0).equals(DOWN_ATOM))
            localizationMapModel.moveDown();
        else if (act.getTerm(0).equals(UP_ATOM))
            localizationMapModel.moveUp();

        return true;
    }

    private boolean updatePossibleLocations(Structure act) {
        ListTerm possibleLocs = ((ListTerm) act.getTerm(0));
        List<Location> locationList = new ArrayList<>();

        for (var location : possibleLocs) {
            Literal locLiteral = (Literal) location;

            // Parse the location terms into Location objects for the grid GUI
            try {
                int x = (int) ((NumberTerm) locLiteral.getTerm(0)).solve();
                int y = (int) ((NumberTerm) locLiteral.getTerm(1)).solve();
                locationList.add(new Location(x, y));
            } catch (NoValueException e) {
                throw new RuntimeException("Failed to solve() location X/Y.", e);
            }

        }

        // Sets the model's possible locations (to show on the GUI)
        localizationMapModel.setPossible(locationList);

        return true;
    }

    /**
     * Called before the end of MAS execution
     */
    @Override
    public void stop() {
        super.stop();
    }


    @Override
    public List<Literal> getPercepts(String agName) {
        var curPercepts = new ArrayList<>(this.curPercepts);
        super.clearPercepts(agName);

        if (hasMoved) {
            curPercepts.add(ASSyntax.createAtom("moved"));
            hasMoved = false;
        } else
            curPercepts.add(ASSyntax.createLiteral("moved").setNegated(Literal.LNeg));


        curPercepts.add(ASSyntax.createLiteral("modelObject", new ObjectTermImpl(localizationMapModel)));
        return curPercepts;
    }


    @Override
    public void agentMoved(List<Literal> newPercepts) {
        hasMoved = true;
        this.curPercepts = newPercepts;
    }
}
