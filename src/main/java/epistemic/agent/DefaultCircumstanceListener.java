package epistemic.agent;

import jason.asSemantics.CircumstanceListener;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;

public interface DefaultCircumstanceListener extends CircumstanceListener {
    @Override
    default void eventAdded(Event e) {
        CircumstanceListener.super.eventAdded(e);
    }

    @Override
    default void intentionAdded(Intention i) {
        CircumstanceListener.super.intentionAdded(i);
    }

    @Override
    default void intentionDropped(Intention i) {
        CircumstanceListener.super.intentionDropped(i);
    }

    @Override
    default void intentionSuspended(Trigger t, Intention i, Term reason) {
        CircumstanceListener.super.intentionSuspended(t, i, reason);
    }

    @Override
    default void intentionWaiting(Intention i, Term reason) {
        CircumstanceListener.super.intentionWaiting(i, reason);
    }

    @Override
    default void intentionResumed(Intention i, Term reason) {
        CircumstanceListener.super.intentionResumed(i, reason);
    }

    @Override
    default void intentionExecuting(Intention i, Term reason) {
        CircumstanceListener.super.intentionExecuting(i, reason);
    }
}
