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


    private final LocalizationMapView localizationMapView;
    private final LocalizationMapModel localizationMapModel;
    private List<Literal> curPercepts;
    private boolean hasMoved = false;

    public LocalizationMapEnvironment() {
        this.curPercepts = new ArrayList<>();
        localizationMapView = new LocalizationMapView();
        localizationMapModel = localizationMapView.getModel();
        localizationMapModel.addMapListener(this);

        localizationMapView.populateModel(new Location(1,1));



        localizationMapView.setVisible(true);
    }

    @Override
    public boolean executeAction(String agName, Structure act) {

        if(act.getFunctor().equals("updatePossible"))
        {
            ListTerm possibleLocs = ((ListTerm)act.getTerm(0));
            List<Location> locationList = new ArrayList<>();

            for(var location : possibleLocs)
            {
                Literal locLiteral = (Literal) location;
                int x = 0;
                int y = 0;
                try {
                    x = (int) ((NumberTerm) locLiteral.getTerm(0)).solve();
                    y = (int) ((NumberTerm) locLiteral.getTerm(1)).solve();
                } catch (NoValueException e) {
                    e.printStackTrace();
                    throw new NullPointerException("Failed to solve()");
                }

                locationList.add(new Location(x, y));
            }

            localizationMapModel.setPossible(locationList);

            return true;
        }

        return super.executeAction(agName, act);
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

        if(hasMoved)
        {
            curPercepts.add(ASSyntax.createAtom("moved"));
            hasMoved = false;
        }
        else
            curPercepts.add(ASSyntax.createLiteral("moved").setNegated(Literal.LNeg));

        return curPercepts;
    }


    @Override
    public void agentMoved(List<Literal> newPercepts) {
        hasMoved = true;
        this.curPercepts = newPercepts;
    }
}
