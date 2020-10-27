package internal;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import localization.models.LocalizationMapModel;

public class update_best_move extends DefaultInternalAction {

    private static final Atom LEFT_ATOM = ASSyntax.createAtom("left");
    private static final Atom RIGHT_ATOM = ASSyntax.createAtom("right");
    private static final Atom DOWN_ATOM = ASSyntax.createAtom("down");
    private static final Atom UP_ATOM = ASSyntax.createAtom("up");

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        var res = ts.getAg().getBB().getCandidateBeliefs(ASSyntax.createLiteral("modelObject", ASSyntax.createVar("Model")), null);

        if(!res.hasNext())
            return false;

        Literal modelObjLit = res.next();
        ObjectTerm modelObjTerm = (ObjectTerm) modelObjLit.getTerm(0);
        LocalizationMapModel localizationMapModel = (LocalizationMapModel) modelObjTerm.getObject();


        return moveAction((Atom) args[0], localizationMapModel);
    }

    private boolean moveAction(Atom act, LocalizationMapModel model) {
        model.getView().getSettingsPanel().setBestMovement(act.getFunctor());
        return true;
    }
}
