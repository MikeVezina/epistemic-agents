package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.Proposition;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.distribution.processor.LogicalConsequenceCallback;
import jason.asSemantics.*;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
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
public class CallbackLogicalConsequence extends Agent {
    private final EpistemicAgent agent;
    private final LogicalConsequenceCallback callback;

    public CallbackLogicalConsequence(EpistemicAgent epistemicAgent, LogicalConsequenceCallback callback) {
        this.agent = epistemicAgent;
        this.callback = callback;
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
                return callback.getCandidateBeliefs(l, u);
            }
        };
    }

}
