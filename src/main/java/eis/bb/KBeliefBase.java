package eis.bb;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;
import jason.bb.DefaultBeliefBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class KBeliefBase extends DefaultBeliefBase {
    private static final jason.asSyntax.Atom KB = ASSyntax.createAtom("kb");
    private Map<String, LinkedList<Literal>> knowledgeBeliefs;

    public KBeliefBase() {
        this.knowledgeBeliefs = new HashMap<>();
    }

    @Override
    public void init(Agent ag, String[] args) {
        super.init(ag, args);
    }

    @Override
    public void stop() {
        System.out.println("stop");
        super.stop();
    }

    @Override
    public void clear() {
        System.out.println("clear");
        super.clear();
    }

    @Override
    public Set<Atom> getNameSpaces() {

        System.out.println("get ns");
        return super.getNameSpaces();
    }

    @Override
    public boolean add(Literal l) {
        filterAddKnowledgeBelief(l);
        return super.add(l);
    }


    /**
     * Add literal to bb with index.
     * This is typically used for beliefs added before any reasoning is done.
     */
    @Override
    public boolean add(int index, Literal l) {

        System.out.println("add ind lit");
        filterAddKnowledgeBelief(l);
        return super.add(index, l);
    }

    private void filterAddKnowledgeBelief(Literal l) {
        if (!l.getNS().equals(KB))
            return;

        for (Term t : l.getTerms()) {

            if (!t.isLiteral())
                continue;

            Literal termLit = (Literal) t;
            if (!termLit.getNS().equals(KB))
                continue;

            LinkedList<Literal> values = knowledgeBeliefs.getOrDefault(termLit.getFunctor(), null);

//            if (values == null)
//                throw new RuntimeException("Failed to expand on term: " + termLit + ". Please make sure it exists in the belief base.");

            // Check for unresolved terms
            System.out.println(t);

        }

        knowledgeBeliefs.compute(l.getFunctor(), (key, val) -> {
            LinkedList<Literal> list = val;

            if (list == null)
                list = new LinkedList<>();

            list.add(l);

            return list;
        });
    }

    @Override
    public Literal contains(Literal l) {

        System.out.println("contains");
        return super.contains(l);
    }

    @Override
    public Iterator<Literal> iterator() {

        System.out.println("iter");

        return super.iterator();
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {

        System.out.println("cand bel");
        return super.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {

//        System.out.println("cand bel unif: " + l.toString());

        return super.getCandidateBeliefs(l, u);
    }

    @Override
    public Iterator<Literal> getPercepts() {

//        System.out.println("perceive");
        return super.getPercepts();
    }

    @Override
    public boolean abolish(PredicateIndicator pi) {

        System.out.println("abol");
        return super.abolish(pi);
    }

    @Override
    public boolean remove(Literal l) {

        System.out.println("remove");
        return super.remove(l);
    }

    @Override
    public int size() {

        System.out.println("size");
        return super.size();
    }

    @Override
    public Element getAsDOM(Document document) {

        System.out.println("dom");
        return super.getAsDOM(document);
    }

    @Override
    public BeliefBase clone() {
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
