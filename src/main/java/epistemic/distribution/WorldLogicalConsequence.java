package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.*;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Acts as a proxy except for getBB, which returns a proxied BB with a world
 */
public class WorldLogicalConsequence extends CallbackLogicalConsequence {
    private ManagedWorlds managedWorlds;
    private World evaluationWorld;

    public WorldLogicalConsequence(EpistemicAgent epistemicAgent, ManagedWorlds managedWorlds) {
        super(epistemicAgent);
        this.managedWorlds = managedWorlds;
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
