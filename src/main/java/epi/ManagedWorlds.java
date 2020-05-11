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
    private final PropertyChangeSupport propsUpdatedSupport;

    // A Set of all Literal values that are managed (values of all worlds)
    private final Set<LiteralKey> managedValues;
    private final Set<LiteralKey> props;

    public ManagedWorlds() {
        this.worldKeysSet = new HashSet<>();
        this.managedValues = new HashSet<>();
        this.propsUpdatedSupport = new PropertyChangeSupport(this);
        this.props = new HashSet<>();
    }

    private ManagedWorlds(ManagedWorlds worlds) {
        this();
        this.addAll(worlds);
        this.worldKeysSet.addAll(worlds.worldKeysSet);
        this.managedValues.addAll(worlds.managedValues);

        // Copy over listeners
        for(var listener : worlds.propsUpdatedSupport.getPropertyChangeListeners())
        {
            this.propsUpdatedSupport.addPropertyChangeListener(listener);
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

    public Set<LiteralKey> getManagedLiterals()
    {
        var copySet = new HashSet<>(managedValues);
        copySet.addAll(worldKeysSet);

        return copySet;
    }

    private void addBelief(Literal belief) {
        this.props.add(new LiteralKey(belief));
        this.propsChanged();
    }

    private void removeBelief(Literal belief) {
        this.props.remove(new LiteralKey(belief));
        this.propsChanged();
    }



    /**
     * Checks if the provided belief is a possible value in the set of managed worlds.
     *
     * @param belief The belief to check.
     * @return
     */
    protected boolean isManagedBelief(LiteralKey belief) {
        return managedValues.contains(belief);
    }

    protected boolean isManagedBelief(Literal belief) {
        return this.isManagedBelief(new LiteralKey(belief));
    }

    /**
     * When an agent doesn't know something, we can generate the accessibility relation based off of the fact that what is known will be the same for accessible worlds.
     * For example, if an agent does not know what cards they hold, this means that we can generate an accessibility relation between all the other worlds where the other
     * variables are the same (aka. the other agent's cards).
     * <p>
     * This is necessary on the agent side so that we can establish the accessibility relation.
     *
     * @param notKnownLiterals This list can contain ungrounded LiteralKeys. Each literal must match a key used in the worlds.
     */
    public void dontKnow(Set<LiteralKey> notKnownLiterals)
    {
        createAccessibilityRelation(notKnownLiterals);
    }

    public ManagedWorlds getMatchingWorlds(Literal literal) {
        return this.stream()
                .filter(world -> world.evaluate(literal))
                .collect(ManagedWorlds.WorldCollector());
    }



    protected void createAccessibilityRelation(Set<LiteralKey> dontKnow) {
        Map<LiteralKey, Set<World>> binnedWorlds = new HashMap<>();

        for (World world : this) {
            for (var entry : world.entrySet()) {
                var key = entry.getKey();

                if(dontKnow.contains(key))
                    continue;

                var val = entry.getValue();

                binnedWorlds.compute(new LiteralKey(val), ((literalKey, worlds) ->
                {
                    if (worlds == null)
                        worlds = new HashSet<>();

                    worlds.add(world);
                    return worlds;
                }));
            }
        }
        // We want to bin the worlds based on values

        for(var world : this)
            world.createAccessibility("self", binnedWorlds);
    }

    public ManagedWorlds clone()
    {
        return new ManagedWorlds(this);
    }

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

    public void addPropositionUpdateListener(PropertyChangeListener pcl)
    {
        this.propsUpdatedSupport.addPropertyChangeListener(pcl);
    }

    public void removePropositionUpdateListener(PropertyChangeListener pcl)
    {
        this.propsUpdatedSupport.removePropertyChangeListener(pcl);
    }

    private void propsChanged()
    {
        this.propsUpdatedSupport.firePropertyChange(new PropertyChangeEvent(this, PROPS_PROPERTY, null,  this.props));
    }

    @Override
    public void beliefEvent(BBEventType type, BBEvent bbEvent) {

        // Do not process if belief is null or not managed by us
        if(bbEvent.getBelief() == null || !isManagedBelief(bbEvent.getBelief()))
            return;

        if(type.equals(BBEventType.ADD))
            addBelief(bbEvent.getBelief());
        else if(type.equals(BBEventType.REMOVE))
            removeBelief(bbEvent.getBelief());
    }
}
