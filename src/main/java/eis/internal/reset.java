package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.percepts.Task;
import jason.asSemantics.*;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import map.MapPercept;
import map.Position;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class reset extends DefaultInternalAction {
    private static final long serialVersionUID = -6214881485708125130L;

    static ConcurrentMap<String, Map.Entry<AgentContainer, Position>> taskMeetingPoints;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ts.getAg().getBB().clear();

        Circumstance cir = ts.getC();

        cir.reset();
        cir.clearEvents();
        cir.clearPendingActions();
        cir.clearPendingEvents();
        cir.clearPendingIntentions();
        cir.clearRunningIntentions();

        // drop intentions in E
        Iterator<Event> ie = cir.getEventsPlusAtomic();
        while (ie.hasNext()) {
            Event e = ie.next();
            if (e.isInternal()) {
                cir.removeEvent(e);
            }
        }

        // drop intentions in PE
        for (String ek: cir.getPendingEvents().keySet()) {
            Event e = cir.getPendingEvents().get(ek);
            if (e.isInternal()) {
                cir.removePendingEvent(ek);
            }
        }

        return true;
    }

}
