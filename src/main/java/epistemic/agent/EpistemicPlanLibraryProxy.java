package epistemic.agent;

import epistemic.formula.EpistemicFormula;
import jason.JasonException;
import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.function.Consumer;


/**
 * A proxy for the plan library to hook any plans for knowledge events
 * so that we can subscribe to the appropriate formulas.
 *
 * For example, if the plan +know(know("Alice")) is added to the plan library,
 * we should subscribe to the formula "(k (k "Alice"))".
 *
 * The subscribed formulas can be obtained and sent to the reasoner
 * for evaluation and Agent event generation.
 *
 * The only methods that will be changed are the add/remove methods,
 * all other methods call the proxied library without any modifications.
 */
public class EpistemicPlanLibraryProxy extends PlanLibrary {
    private final PlanLibrary proxyLibrary;
    private final Map<Plan, EpistemicFormula> subscriptionPlans;

    public EpistemicPlanLibraryProxy(PlanLibrary pl) {
        this.proxyLibrary = pl;
        subscriptionPlans = new HashMap<>();

        // If the proxied library already has plans, iterate through them to add any subscriptions.
        addEpistemicPlan(pl);
    }

    public Collection<EpistemicFormula> getSubscribedFormulas() {
        return subscriptionPlans.values();
    }

    /**
     * Looks for event plans for epistemic formulas literal in the event trigger, creates
     * an EpistemicFormula object from the literal, and adds the formula to the subscribed formulas map.
     *
     * @param newPlan The plan that was added to the plan library.
     */
    private void addEpistemicPlan(Plan newPlan) {
        // We should not subscribe to any achieve/goal plans
        if(newPlan == null || !EpistemicFormula.isEpistemicLiteral(newPlan.getTrigger().getLiteral()) || !newPlan.getTrigger().getType().equals(Trigger.TEType.belief))
            return;

        subscriptionPlans.put(newPlan, EpistemicFormula.fromLiteral(newPlan.getTrigger().getLiteral()));
    }

    /**
     * Checks for knowledge formulas from the plan library.
     * @param planLibrary The plan library
     */
    private void addEpistemicPlan(PlanLibrary planLibrary)
    {
        if(planLibrary == null)
            return;

        addEpistemicPlan(planLibrary.getPlans());
    }

    /**
     * Checks for knowledge formulas from the list of plans.
     * @param plans The list of plans
     */
    private void addEpistemicPlan(List<Plan> plans) {
        if(plans == null)
            return;

        for(Plan plan : plans)
            this.addEpistemicPlan(plan);
    }

    private void removeEpistemicPlan(Plan removedPlan) {
        if(removedPlan == null || !EpistemicFormula.isEpistemicLiteral(removedPlan.getTrigger().getLiteral()))
            return;

        subscriptionPlans.remove(removedPlan);
    }

    @Override
    public Plan add(Plan p, Term source, boolean before) throws JasonException {
        var newPlan = proxyLibrary.add(p, source, before);
        addEpistemicPlan(newPlan);
        return newPlan;
    }

    @Override
    public Plan add(StringTerm stPlan, Term tSource) throws ParseException, JasonException {
        var newPlan = proxyLibrary.add(stPlan, tSource);
        addEpistemicPlan(newPlan);
        return newPlan;
    }

    @Override
    public Plan add(StringTerm stPlan, Term tSource, boolean before) throws ParseException, JasonException {
        var newPlan = proxyLibrary.add(stPlan, tSource, before);
        addEpistemicPlan(newPlan);
        return newPlan;
    }

    @Override
    public Plan add(Plan p, boolean before) throws JasonException {
        proxyLibrary.add(p, before);
        addEpistemicPlan(p);
        return p;
    }

    @Override
    public Plan add(Plan p) throws JasonException {
        proxyLibrary.add(p);
        addEpistemicPlan(p);
        return p;
    }

    @Override
    public void addAll(PlanLibrary pl) throws JasonException {
        proxyLibrary.addAll(pl);
        addEpistemicPlan(pl);
    }

    @Override
    public void addAll(List<Plan> plans) throws JasonException {
        proxyLibrary.addAll(plans);
        addEpistemicPlan(plans);
    }

    @Override
    public void clear() {
        proxyLibrary.clear();
        subscriptionPlans.clear();
    }

    @Override
    public boolean remove(Literal pLabel, Term source) {
        var removedPlan = proxyLibrary.remove(pLabel, source);
        if(removedPlan)
            removeEpistemicPlan(get(pLabel));
        return removedPlan;
    }

    @Override
    public Plan remove(Literal pLabel) {
        var removedPlan = proxyLibrary.remove(pLabel);
        removeEpistemicPlan(removedPlan);
        return removedPlan;
    }

    @Override
    public boolean hasMetaEventPlans() {
        return proxyLibrary.hasMetaEventPlans();
    }

    @Override
    public boolean hasUserKqmlReceivedPlans() {
        return proxyLibrary.hasUserKqmlReceivedPlans();
    }

    @Override
    public Plan get(String label) {
        return proxyLibrary.get(label);
    }

    @Override
    public Plan get(Literal label) {
        return proxyLibrary.get(label);
    }

    @Override
    public int size() {
        return proxyLibrary.size();
    }

    @Override
    public List<Plan> getPlans() {
        return proxyLibrary.getPlans();
    }

    @Override
    public Iterator<Plan> iterator() {
        return proxyLibrary.iterator();
    }

    @Override
    public boolean isRelevant(Trigger te) {
        return proxyLibrary.isRelevant(te);
    }

    @Override
    public boolean hasCandidatePlan(Trigger te) {
        return proxyLibrary.hasCandidatePlan(te);
    }

    @Override
    public List<Plan> getAllRelevant(Trigger te) {
        return proxyLibrary.getAllRelevant(te);
    }

    @Override
    public List<Plan> getCandidatePlans(Trigger te) {
        return proxyLibrary.getCandidatePlans(te);
    }

    @Override
    public PlanLibrary clone() {
        var clone = new EpistemicPlanLibraryProxy(proxyLibrary.clone());
        clone.subscriptionPlans.putAll(this.subscriptionPlans);
        return clone;
    }

    @Override
    public String toString() {
        return proxyLibrary.toString();
    }

    @Override
    public String getAsTxt(boolean includeKQMLPlans) {
        return proxyLibrary.getAsTxt(includeKQMLPlans);
    }

    @Override
    public Element getAsDOM(Document document) {
        return proxyLibrary.getAsDOM(document);
    }

    @Override
    public void forEach(Consumer<? super Plan> action) {
        proxyLibrary.forEach(action);
    }

    @Override
    public Spliterator<Plan> spliterator() {
        return proxyLibrary.spliterator();
    }

    @Override
    public int hashCode() {
        return proxyLibrary.hashCode();
    }

    @Override
    public Object getLock() {
        return proxyLibrary.getLock();
    }

    @Override
    public boolean equals(Object obj) {
        return proxyLibrary.equals(obj);
    }

}
