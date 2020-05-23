package jason;

import epistemic.formula.EpistemicLiteral;
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
 * we should subscribe to the formula "(k (k "Alice"))". The reasoner should
 * recognize this subscription when we run the BUF, and will trigger the plan event
 * when the formula gets evaluated to true by the reasoner.
 *
 * The only methods that will be modified are the add/remove methods.
 */
public class EpistemicPlanLibraryProxy extends PlanLibrary {
    private final PlanLibrary proxyLibrary;
    private final Map<Plan, EpistemicLiteral> subscriptionPlans;

    public EpistemicPlanLibraryProxy(PlanLibrary pl) {
        this.proxyLibrary = pl;
        subscriptionPlans = new HashMap<>();

        // If the proxied library already has plans, iterate through them to add any subscriptions.
        for(Plan existing : this.proxyLibrary.getPlans())
            planAdded(existing);

    }

    private void planAdded(Plan newPlan) {
        if(newPlan == null || !EpistemicLiteral.isEpistemicLiteral(newPlan.getTrigger().getLiteral()))
            return;

        subscriptionPlans.put(newPlan, EpistemicLiteral.parseLiteral(newPlan.getTrigger().getLiteral()));
    }

    private void planRemoved(Plan removedPlan) {
        if(removedPlan == null || !EpistemicLiteral.isEpistemicLiteral(removedPlan.getTrigger().getLiteral()))
            return;

        subscriptionPlans.remove(removedPlan);
    }

    @Override
    public Plan add(Plan p, Term source, boolean before) throws JasonException {
        var newPlan = proxyLibrary.add(p, source, before);
        planAdded(newPlan);
        return newPlan;
    }

    @Override
    public Plan add(StringTerm stPlan, Term tSource) throws ParseException, JasonException {
        var newPlan = proxyLibrary.add(stPlan, tSource);
        planAdded(newPlan);
        return newPlan;
    }

    @Override
    public Plan add(StringTerm stPlan, Term tSource, boolean before) throws ParseException, JasonException {
        var newPlan = proxyLibrary.add(stPlan, tSource, before);
        planAdded(newPlan);
        return newPlan;
    }

    @Override
    public void add(Plan p, boolean before) throws JasonException {
        proxyLibrary.add(p, before);
        planAdded(p);
    }

    @Override
    public void add(Plan p) throws JasonException {
        proxyLibrary.add(p);
        planAdded(p);
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
            planRemoved(get(pLabel));
        return removedPlan;
    }

    @Override
    public Plan remove(Literal pLabel) {
        var removedPlan = proxyLibrary.remove(pLabel);
        planRemoved(removedPlan);
        return removedPlan;
    }

    @Override
    public void addAll(PlanLibrary pl) throws JasonException {
        proxyLibrary.addAll(pl);
    }

    @Override
    public void addAll(List<Plan> plans) throws JasonException {
        proxyLibrary.addAll(plans);
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

    public Collection<EpistemicLiteral> getSubscribedFormulas() {
        return subscriptionPlans.values();
    }
}
