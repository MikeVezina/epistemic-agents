package eis.bb;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;
import jason.bb.DefaultBeliefBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Set;

public class KBeliefBase extends ChainBBAdapter {

    public KBeliefBase()
    {
        super(new DefaultBeliefBase());
    }

    @Override
    public void init(Agent ag, String[] args) {
//        System.out.println("init");
        super.init(ag, args);
    }
    @Override
    public void stop() {
        System.out.println("stop");nextBB.stop();
    }

    @Override
    public void clear() {
        System.out.println("clear");
        nextBB.clear();
    }

    @Override
    public Set<Atom> getNameSpaces() {

        System.out.println("get ns");
        return nextBB.getNameSpaces();
    }

    @Override
    public boolean add(Literal l) {

       // System.out.println("add lit: " + l.toString());

        return nextBB.add(l);
    }

    @Override
    public boolean add(int index, Literal l) {

        System.out.println("add ind lit");
        return nextBB.add(index, l);
    }

    @Override
    public Literal contains(Literal l) {

        System.out.println("contains");
        return nextBB.contains(l);
    }

    @Override
    public Iterator<Literal> iterator() {

        System.out.println("iter");

        return nextBB.iterator();
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {

        System.out.println("cand bel");
        return nextBB.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {

//        System.out.println("cand bel unif: " + l.toString());

        return nextBB.getCandidateBeliefs(l, u);
    }

    @Override
    public Iterator<Literal> getPercepts() {

//        System.out.println("perceive");
        return nextBB.getPercepts();
    }

    @Override
    public boolean abolish(PredicateIndicator pi) {

        System.out.println("abol");
        return nextBB.abolish(pi);
    }

    @Override
    public boolean remove(Literal l) {

        System.out.println("remove");
        return nextBB.remove(l);
    }

    @Override
    public int size() {

        System.out.println("size");
        return nextBB.size();
    }

    @Override
    public Element getAsDOM(Document document) {

        System.out.println("dom");
        return nextBB.getAsDOM(document);
    }

    @Override
    public BeliefBase clone() {
        return this;
    }

    @Override
    public String toString() {
        return nextBB.toString();
    }
}
