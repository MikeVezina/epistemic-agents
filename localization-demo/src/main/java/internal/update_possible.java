package internal;

import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import localization.models.LocalizationMapModel;

import java.util.ArrayList;
import java.util.List;

public class update_possible extends DefaultInternalAction {
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {


        var res = ts.getAg().getBB().getCandidateBeliefs(ASSyntax.createLiteral("modelObject", ASSyntax.createVar("Model")), null);

        if(!res.hasNext())
            return false;

        Literal modelObjLit = res.next();
        ObjectTerm modelObjTerm = (ObjectTerm) modelObjLit.getTerm(0);
        LocalizationMapModel localizationMapModel = (LocalizationMapModel) modelObjTerm.getObject();


        ListTerm possibleLocs = (ListTerm) args[0];
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
}
