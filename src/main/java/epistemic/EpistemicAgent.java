package epistemic;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Intention;
import jason.asSyntax.Literal;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class EpistemicAgent extends Agent {

    private EpistemicDistribution epistemicDistribution;

    @Override
    public void load(String asSrc) throws JasonException {
        super.load(asSrc);
        this.agentLoaded();
    }

    @Override
    public void load(InputStream in, String sourceId) throws JasonException {
        super.load(in, sourceId);
        this.agentLoaded();
    }

    /**
     * Called when the agent has been loaded and parsed from the source file.
     * The possible worlds distribution can be initialized here. (Once initial beliefs/rules are parsed)
     */
    private void agentLoaded() {
        System.out.println("Loaded");
        this.epistemicDistribution = new EpistemicDistribution(this);
    }

    /**
     * Update managed world props in this function. We can still operate on beliefs.
     * Use super.addBel(), etc. to trigger the BRF (and generate events) for inferred knowledge.
     */
    @Override
    public int buf(Collection<Literal> percepts) {
        // Update the BB with new percepts before updating the managed worlds
        int result = super.buf(percepts);

        this.epistemicDistribution.updateProps(percepts);

        return result;
    }

    /**
     * Controls the addition / deletion of a belief. This function gets executed when +belief, -belief, or -+belief is executed by the ExecInt function.
     *
     * We need to confirm that the model is correct. i.e. all proposition keys should have one (and only one) corresponding value in the BB.
     *
     * @see Agent#brf(Literal, Literal, Intention)
     */
    @Override
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {

        // Should we pass brf to managed worlds?
        return super.brf(beliefToAdd, beliefToDel, i);
    }


}
