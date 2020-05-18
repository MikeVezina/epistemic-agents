package jason;

import epistemic.EpistemicDistribution;
import epistemic.wrappers.Proposition;
import jason.asSemantics.Agent;
import jason.asSemantics.Intention;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

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

        if (this.epistemicDistribution != null)
            this.epistemicDistribution.buf(percepts);

        return result;
    }

    /**
     * Controls the addition / deletion of a belief. This function gets executed when +belief, -belief, or -+belief is executed by the ExecInt function.
     * <p>
     * We need to confirm that the model is correct. i.e. all proposition keys should have one (and only one) corresponding value in the BB.
     *
     * @see Agent#brf(Literal, Literal, Intention)
     */
    @Override
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {

        if (this.epistemicDistribution != null)
            this.epistemicDistribution.brf(beliefToAdd, beliefToDel);

        return super.brf(beliefToAdd, beliefToDel, i);
    }


    public void addNewKnowledge(Proposition newKnowledge) {
        try {
            this.addBel((Literal) newKnowledge.getValueLiteral().cloneNS(Atom.DefaultNS));
        } catch (RevisionFailedException e) {
            e.printStackTrace();
        }
    }
}
