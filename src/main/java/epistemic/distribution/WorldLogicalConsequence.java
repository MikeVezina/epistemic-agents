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
public class WorldLogicalConsequence extends Agent {
    private EpistemicAgent agent;
    private ManagedWorlds managedWorlds;
    private World evaluationWorld;

    public WorldLogicalConsequence(EpistemicAgent epistemicAgent, ManagedWorlds managedWorlds) {
        this.agent = epistemicAgent;
        this.managedWorlds = managedWorlds;
    }

    @Override
    public TransitionSystem getTS() {
        return agent.getTS();
    }

    @Override
    public Logger getLogger() {
        return agent.getLogger();
    }

    @Override
    public String getASLSrc() {
        return agent.getASLSrc();
    }

    @Override
    public InternalAction getIA(String iaName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        return agent.getIA(iaName);
    }

    @Override
    public ArithFunction getFunction(String function, int arity) {
        return agent.getFunction(function, arity);
    }

    @Override
    public List<Literal> getInitialBels() {
        return agent.getInitialBels();
    }

    @Override
    public Collection<Literal> getInitialGoals() {
        return agent.getInitialGoals();
    }

    @Override
    public PlanLibrary getPL() {
        return agent.getPL();
    }

    @Override
    public synchronized Document getAgState() {
        return agent.getAgState();
    }

    @Override
    public Element getAsDOM(Document document) {
        return agent.getAsDOM(document);
    }

    @Override
    public Document getAgProgram() {
        return agent.getAgProgram();
    }

    @Override
    public BeliefBase getBB() {
        return new DefaultBeliefBase() {
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
        };
    }

    public void setEvaluationWorld(World world) {
        this.evaluationWorld = world;
    }
}
