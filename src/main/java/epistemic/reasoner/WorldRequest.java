package epistemic.reasoner;

import epistemic.ManagedWorlds;
import epistemic.wrappers.Proposition;
import epistemic.formula.EpistemicLiteral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

    public Set<Proposition> updateProps(Collection<Proposition> propositionSet, Collection<EpistemicLiteral> epistemicFormulas) {
        var propositionStrings = new ArrayList<String>();

        for (var literalKey : propositionSet) {
            if (literalKey == null)
                continue;

            propositionStrings.add(literalKey.getValue().toSafePropName());
        }

        var result = reasoner.updateProps(propositionStrings, epistemicFormulas);
        Set<Proposition> knowledgeSet = new HashSet<>();

        for(String newKnowledge : result)
        {
            knowledgeSet.add(managedWorlds.getLiteral(newKnowledge));
        }

        System.out.println("Prop " + propositionStrings.toString() + " update success: " + result);
        return knowledgeSet;
    }


    public boolean evaluate(EpistemicLiteral epistemicLiteral) {
        return reasoner.evaluateFormula(epistemicLiteral.toFormulaJSON());
    }
}

