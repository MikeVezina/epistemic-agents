package jason;

import epistemic.EpistemicDistribution;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class EpistemicAgent extends Agent {

    private EpistemicDistribution epistemicDistribution;

    public EpistemicAgent() {
        super.pl = new EpistemicPlanLibraryProxy(new PlanLibrary());
    }

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

    @Override
    public void initAg() {
        super.initAg();

    }

    @Override
    public void setPL(PlanLibrary pl) {
        // Ensure we always have a proxy plan library in place
        if(!(pl instanceof EpistemicPlanLibraryProxy))
            pl = new EpistemicPlanLibraryProxy(pl);

        super.setPL(pl);
    }

    @Override
    public EpistemicPlanLibraryProxy getPL() {
        return (EpistemicPlanLibraryProxy) super.getPL();
    }

    /**
     * Called when the agent has been loaded and parsed from the source file.
     * The possible worlds distribution can be initialized here. (Once initial beliefs/rules are parsed)
     */
    private void agentLoaded() {
        System.out.println("Loaded");
        this.epistemicDistribution = new EpistemicDistribution(this);
        this.setBB(new ChainedEpistemicBB(this.getBB(), this.epistemicDistribution));
    }

    @Override
    public boolean believes(LogicalFormula bel, Unifier un) {
        return super.believes(bel, un);
    }

    @Override
    public Literal findBel(Literal bel, Unifier un) {
        return super.findBel(bel, un);
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
            this.epistemicDistribution.buf(this.getPL().getSubscribedFormulas());

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


    /**
     * Creates events for belief plans (+knows(knows(hello)))
     * @param newKnowledge
     */
    public void createKnowledgeEvent(Trigger.TEOperator operator, EpistemicFormula newKnowledge) {
        Trigger te = new Trigger(operator, Trigger.TEType.belief, newKnowledge.getOriginalLiteral());
        this.getTS().updateEvents(new Event(te, Intention.EmptyInt));
    }
}
