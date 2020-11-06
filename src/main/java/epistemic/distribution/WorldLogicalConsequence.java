package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.distribution.propositions.Proposition;
import epistemic.distribution.propositions.SingleValueProposition;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.processor.LogicalConsequenceCallback;
import jason.asSemantics.*;
import jason.asSyntax.Literal;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Acts as a proxy except for getBB, which returns a proxied BB with a world
 */
public class WorldLogicalConsequence extends CallbackLogicalConsequence implements LogicalConsequenceCallback {
    private ManagedWorlds managedWorlds;
    private World evaluationWorld;

    public WorldLogicalConsequence(EpistemicAgent epistemicAgent, ManagedWorlds managedWorlds) {
        super(epistemicAgent, null);
        this.managedWorlds = managedWorlds;
        throw new NullPointerException("Broken implementation..");
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        // if this is a literal that is managed by the worlds then we need to obtain logical consequences of that world
        // Otherwise, we can forward it to the original BB

        ArrayList<Literal> list = new ArrayList<>();
        Proposition managedProp = managedWorlds.getManagedProposition(l);

        // If this proposition is managed by us, then check its evaluation
        if (managedProp != null) {

            if (evaluationWorld.evaluate(l)) {
                list.add(l);
                return list.listIterator();
            }


        } else {
            // Otherwise, only return it if it is ground (already unified by the BB)
            if (l.isGround()) {
                list.add(l);
                return list.listIterator();
            }
        }
        // Return
        return null;
    }

    public void setEvaluationWorld(World world) {
        this.evaluationWorld = world;
    }
}
