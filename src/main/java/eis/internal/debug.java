package eis.internal;

import eis.EISAdapter;
import eis.agent.AgentContainer;
import eis.percepts.terrain.Obstacle;
import eis.percepts.things.Block;
import eis.watcher.SynchronizedPerceptWatcher;
import jason.JasonException;
import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import map.AgentMap;
import map.Position;
import utils.Utils;

import java.util.Iterator;

public class debug extends DefaultInternalAction {

    private static final long serialVersionUID = -6214881485708125130L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        assert ts != null;


        Iterator<Literal> iter = ts.getAg().getBB().getCandidateBeliefs(ASSyntax.parseLiteral("percept::obstacle(X, Y)"), un);

        if(iter == null)
            return false;

            iter.forEachRemaining(perLiteral -> {

                // Filter out any non-percepts
                if(!perLiteral.hasSource(ASSyntax.createAtom("percept")))
                    return;

                NumberTerm xTerm = (NumberTerm) perLiteral.getTerm(0);
                NumberTerm yTerm = (NumberTerm) perLiteral.getTerm(1);
                Term typeTerm = perLiteral.getTerm(2);
                Term detailsTerm = perLiteral.getTerm(3);

                try {
                    int obsX = (int) xTerm.solve();
                    int obsY = (int) yTerm.solve();
                    String thingType = typeTerm.toString();
                    String details = detailsTerm.toString();

                    if (!thingType.equalsIgnoreCase("block"))
                        return;

                    ts.getLogger().info("Found block: (" + obsX + ", " + obsY + ") with type " + details);
                } catch (NoValueException noValEx) {
                    ts.getLogger().warning("The number terms failed to resolve a value");
                }

            });

        AgentContainer container = SynchronizedPerceptWatcher.getInstance().getAgentContainer(ts.getUserAgArch().getAgName());

        container.getAgentMap().getCurrentPercepts().values().forEach(mapPercept -> {
            if (mapPercept.hasBlock()) {
                Block blockThing = mapPercept.getBlock();
                ts.getLogger().info("Found a block at: " + blockThing.getPosition() + " with type " + blockThing.getDetails());
            }
        });

        // execute the internal action
        AgentMap agentMap = EISAdapter.getSingleton().getAgentMap(ts.getUserAgArch().getAgName());
        return true;
    }
}
