package eis.bb;

import eis.bb.event.BBEvent;
import eis.bb.event.BBEventType;
import eis.bb.event.BBListener;
import epi.CustomArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class KBeliefBase extends DefaultBeliefBase {
    private static final jason.asSyntax.Atom KB = ASSyntax.createAtom("kb");
    private Map<String, LinkedList<Literal>> knowledgeBeliefs;
    private Map<BBEventType, Set<BBListener>> listeners;

    private Agent agent;

    public KBeliefBase() {
        this.knowledgeBeliefs = new HashMap<>();
        this.listeners = new HashMap<>();

        for (BBEventType type : BBEventType.values()) {
            listeners.put(type, new HashSet<>());
        }
    }

    public void addListener(BBListener bbListener, BBEventType... eventTypes) {
        for (BBEventType eventType : eventTypes) {
            addListener(eventType, bbListener);

            if (eventType.equals(BBEventType.REGISTER_ALL))
                return;
        }
    }


    public void addListener(BBEventType type, BBListener listener) {
        listeners.computeIfPresent(type, (k, listeners) -> {
            listeners.add(listener);
            return listeners;
        });
    }

    private void sendEvent(BBEventType type, Literal belief) {
        this.sendEvent(new BBEvent(this, type, belief));
    }

    private void sendEvent(BBEvent event) {
        Set<BBListener> eventListeners = new HashSet<>(listeners.get(BBEventType.REGISTER_ALL));
        eventListeners.addAll(listeners.get(event.getType()));


        for (BBListener listener : eventListeners) {
            if (listener != null)
                listener.beliefEvent(event.getType(), event);
        }
    }

    @Override
    public void init(Agent ag, String[] args) {
        super.init(ag, args);
        this.agent = ag;
    }

    @Override
    public void stop() {
        // System.out.println("stop");
        super.stop();
    }

    @Override
    public void clear() {
        // System.out.println("clear");
        super.clear();
    }

    @Override
    public Set<Atom> getNameSpaces() {

        // System.out.println("get ns");
        return super.getNameSpaces();
    }

    @Override
    public boolean add(Literal l) {
        boolean res = super.add(l);

        sendEvent(BBEventType.ADD, l);

        return res;
    }


    /**
     * Add literal to bb with index.
     * This is typically used for beliefs added before any reasoning is done.
     */
    @Override
    public boolean add(int index, Literal l) {

        // System.out.println("add ind lit");
        var res = super.add(index, l);

        sendEvent(BBEventType.ADD, l);

        return res;
    }

    @Override
    public Literal contains(Literal l) {

        // System.out.println("contains");
        return super.contains(l);
    }

    @Override
    public Iterator<Literal> iterator() {

        // System.out.println("iter");

        return super.iterator();
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {

        // System.out.println("cand bel");
        return super.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {

//        // System.out.println("cand bel unif: " + l.toString());

        return super.getCandidateBeliefs(l, u);
    }

    @Override
    public Iterator<Literal> getPercepts() {

//        // System.out.println("perceive");
        return super.getPercepts();
    }

    @Override
    public boolean abolish(PredicateIndicator pi) {

        // System.out.println("abol");
//        sendEvent(BBEventType.ABOLISH, pi);
        return super.abolish(pi);
    }

    @Override
    public boolean remove(Literal l) {

        // System.out.println("remove");

        sendEvent(BBEventType.REMOVE, l);
        return super.remove(l);
    }

    @Override
    public int size() {

        // System.out.println("size");
        return super.size();
    }

    @Override
    public Element getAsDOM(Document document) {

        // System.out.println("dom");
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
