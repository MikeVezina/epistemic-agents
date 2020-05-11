package epi;

import eis.bb.event.BBEvent;
import eis.bb.event.BBEventType;
import eis.bb.event.BBListener;
import jason.asSyntax.Literal;
import wrappers.LiteralKey;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collector;

public class ManagedWorlds extends HashSet<World> implements BBListener {

    public static final String PROPS_PROPERTY = "props";
    private final Set<LiteralKey> worldKeysSet;
    private final PropertyChangeSupport propertyChangeSupport;

    // A Set of all Literal values that are managed (values of all worlds)
    private final Set<LiteralKey> managedValues;
    private final Set<LiteralKey> props;

    public ManagedWorlds() {
        this.worldKeysSet = new HashSet<>();
        this.managedValues = new HashSet<>();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.props = new HashSet<>();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this();
        this.addAll(worlds);
        this.worldKeysSet.addAll(worlds.worldKeysSet);
        this.managedValues.addAll(worlds.managedValues);

        // Copy over listeners
        for(var listener : worlds.propertyChangeSupport.getPropertyChangeListeners())
        {
            this.propertyChangeSupport.addPropertyChangeListener(listener);
        }

        // Copy over props
        this.props.addAll(worlds.props);
    }


    @Override
    public boolean add(World world) {
        worldKeysSet.addAll(world.keySet());
        managedValues.addAll(world.wrappedValues());
        return super.add(world);
    }

    /**
     * Handles when a belief managed by this object has been added to the belief base.
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
        return managedValues.contains(belief);
    }

    /**
     * Checks if the provided belief is a possible value in the set of managed worlds. Wraps the literal in a LiteralKey object and then calls the overloaded method that accepts a LiteralKey.
     * @see ManagedWorlds#isManagedBelief(LiteralKey)
     *
     * @param belief The belief LiteralKey to check.
     * @return True if the belief is a managed literal
     */
    public boolean isManagedBelief(Literal belief) {
        return this.isManagedBelief(new LiteralKey(belief));
    }


    /**
     * @return a clone of the current managed worlds object. Copies over any {@link PropertyChangeListener} listeners and current propositions.
     * This will only add the contained worlds to the cloned object, this will not clone any of the contained worlds.
     */
    public ManagedWorlds clone()
    {
        return new ManagedWorlds(this);
    }

    /**
     * @return A collector that can be used to create a ManagedWorld object from collected worlds.
     */
    public static Collector<World, ManagedWorlds, ManagedWorlds> WorldCollector() {
        return Collector.of(
                ManagedWorlds::new,
                ManagedWorlds::add,
                (result1, result2) -> {
                    result1.addAll(result2);
                    return result1;
                }
        );
    }

    public void addPropertyListener(PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyListener(PropertyChangeListener pcl)
    {
        this.propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    private void propertyChanged(String propertyName, Object newValue)
    {
        this.propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, null,  newValue));
    }

    @Override
    public void beliefEvent(BBEventType type, BBEvent bbEvent) {

        // Do not process if belief is null or not managed by us
        if(bbEvent.getBelief() == null || !isManagedBelief(bbEvent.getBelief()))
            return;

        if(type.equals(BBEventType.ADD))
            addBelief(new LiteralKey(bbEvent.getBelief()));
        else if(type.equals(BBEventType.REMOVE))
            removeBelief(new LiteralKey(bbEvent.getBelief()));
    }
}
