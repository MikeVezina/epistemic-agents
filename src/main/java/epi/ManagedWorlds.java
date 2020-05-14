package epi;

import eis.bb.KBeliefBase;
import eis.bb.event.BBEvent;
import eis.bb.event.BBEventType;
import eis.bb.event.BBListener;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import wrappers.LiteralKey;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collector;

public class ManagedWorlds extends HashSet<World> implements BBListener {

    public static final String PROPS_PROPERTY = "props";

    private final PropertyChangeSupport propertyChangeSupport;

    private ManagedLiterals managedLiterals;

    private final Set<LiteralKey> props;

    private final KBeliefBase beliefBase;

    public ManagedWorlds(KBeliefBase beliefBase) {
        this.beliefBase = beliefBase;
        this.beliefBase.addListener(this, BBEventType.REGISTER_ALL);

        this.managedLiterals = new ManagedLiterals();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.props = new HashSet<>();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this(worlds.beliefBase);
        this.addAll(worlds);
        this.managedLiterals = worlds.managedLiterals.clone();

        // Copy over listeners
        for (var listener : worlds.propertyChangeSupport.getPropertyChangeListeners()) {
            this.propertyChangeSupport.addPropertyChangeListener(listener);
        }

        // Copy over props
        this.props.addAll(worlds.props);
    }


    @Override
    public boolean add(World world) {
        managedLiterals.addWorld(world);
        return super.add(world);
    }

    /**
     * Handles when a belief managed by this object has been added to the belief base.
     *
     * @param belief The managed belief
     */
    private void addBelief(LiteralKey belief) {
        this.props.add(belief);
        this.propertyChanged(PROPS_PROPERTY, this.props);
    }

    private void removeBelief(LiteralKey belief) {
        this.props.remove(belief);
        this.propertyChanged(PROPS_PROPERTY, this.props);
    }


    /**
     * Checks if the provided belief is a possible value in the set of managed worlds.
     *
     * @param belief The belief LiteralKey to check.
     * @return True if the belief is a managed literal
     */
    public boolean isManagedBelief(LiteralKey belief) {
        return managedLiterals.isManagedBelief(belief);
    }

    /**
     * Checks if the provided belief is a possible value in the set of managed worlds. Wraps the literal in a LiteralKey object and then calls the overloaded method that accepts a LiteralKey.
     *
     * @param belief The belief LiteralKey to check.
     * @return True if the belief is a managed literal
     * @see ManagedWorlds#isManagedBelief(LiteralKey)
     */
    public boolean isManagedBelief(Literal belief) {
        return this.isManagedBelief(new LiteralKey(belief));
    }


    /**
     * @return a clone of the current managed worlds object. Copies over any {@link PropertyChangeListener} listeners and current propositions.
     * This will only add the contained worlds to the cloned object, this will not clone any of the contained worlds.
     */
    public ManagedWorlds clone() {
        return new ManagedWorlds(this);
    }

    /**
     * @return A collector that can be used to create a ManagedWorld object from collected worlds.
     */
    public static Collector<World, ManagedWorlds, ManagedWorlds> WorldCollector(KBeliefBase beliefBase) {
        return Collector.of(
                () -> new ManagedWorlds(beliefBase),
                ManagedWorlds::add, (result1, result2) -> {
                    result1.addAll(result2);
                    return result1;
                });
    }

    public void addPropertyListener(PropertyChangeListener pcl) {
        this.propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyListener(PropertyChangeListener pcl) {
        this.propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    private void propertyChanged(String propertyName, Object newValue) {
        this.propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, null, newValue));
    }

    @Override
    public void beliefEvent(BBEventType type, BBEvent bbEvent) {

        // Do not process if belief is null or not managed by us
        if (bbEvent.getBelief() == null || !isManagedBelief(bbEvent.getBelief()))
            return;

        if (type.equals(BBEventType.ADD))
            addBelief(new LiteralKey(bbEvent.getBelief()));
        else if (type.equals(BBEventType.REMOVE))
            removeBelief(new LiteralKey(bbEvent.getBelief()));
    }

    /**
     * Todo: add checks for BB consistency.
     * Todo: This needs more work. This adds beliefs but does not remove any if a prop is false.
     *
     *
     * @param newKnowledge
     */
    public void addKnowledge(Set<String> newKnowledge) {
        for (String prop : newKnowledge) {
            var literal = this.managedLiterals.getPropositionLiteral(prop);
            if (literal != null)
                this.beliefBase.add((Literal) literal.getLiteral().cloneNS(Atom.DefaultNS));
        }

    }
}
