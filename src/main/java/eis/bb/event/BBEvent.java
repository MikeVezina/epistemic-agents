package eis.bb.event;

import eis.bb.KBeliefBase;
import jason.asSyntax.Literal;

import java.util.EventObject;

public class BBEvent extends EventObject {

    private final BBEventType type;
    private final Literal correspondingBelief;

    public BBEvent(KBeliefBase source, BBEventType type, Literal belief)
    {
        super(source);
        this.type = type;
        this.correspondingBelief = belief;
    }

    public KBeliefBase getSource()
    {
        return (KBeliefBase) super.getSource();
    }

    public BBEventType getType() {
        return type;
    }

    public Literal getBelief() {
        return correspondingBelief;
    }
}

