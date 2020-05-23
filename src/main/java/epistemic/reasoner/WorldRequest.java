package epistemic.reasoner;

import epistemic.ManagedWorlds;
import epistemic.wrappers.Proposition;
import epistemic.formula.EpistemicFormula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class WorldRequest {
    private final ReasonerSDK reasoner;
    private final ManagedWorlds managedWorlds;

    public WorldRequest(ManagedWorlds managedWorlds) {
        this.managedWorlds = managedWorlds;
        this.reasoner = new ReasonerSDK();


        // Create the managed worlds
        reasoner.createModel(managedWorlds);
    }

    public Map<EpistemicFormula, Boolean> updateProps(Collection<Proposition> propositionSet, Collection<EpistemicFormula> epistemicFormulas) {
        var propositionStrings = new ArrayList<String>();

        for (var literalKey : propositionSet) {
            if (literalKey == null)
                continue;

            propositionStrings.add(literalKey.getValue().toSafePropName());
        }

        return reasoner.updateProps(propositionStrings, epistemicFormulas);
    }


    public boolean evaluate(EpistemicFormula epistemicFormula) {
        return reasoner.evaluateFormula(epistemicFormula);
    }
}

