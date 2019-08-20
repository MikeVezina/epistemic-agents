package eis;

import eis.percepts.AgentLocation;
import jason.JasonException;
import jason.NoValueException;
import jason.asSyntax.*;
import eis.AgentListener;
import eis.EnvironmentInterfaceStandard;
import eis.EnvironmentListener;
import eis.exceptions.*;
import eis.iilang.*;
import jason.environment.Environment;
import massim.eismassim.EnvironmentInterface;
import utils.Position;
import utils.Utils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class functions as a Jason environment, using EISMASSim to connect to a MASSim server.
 * (see http://cig.in.tu-clausthal.de/eis)
 * (see also https://multiagentcontest.org)
 *
 * @author Jomi
 * - adapted by ta10
 */
public class EISAdapter extends Environment implements AgentListener {

    private Logger logger = Logger.getLogger("EISAdapter." + EISAdapter.class.getName());

    private EnvironmentInterfaceStandard ei;

    private Map<String, AgentLocation> agentLocations;
    private Map<String, List<Literal>> recentPerceptions;

    public EISAdapter() {
        super(20);
    }

    @Override
    public void init(String[] args) {

        ei = new EnvironmentInterface("conf/eismassimconfig.json");

        agentLocations = new HashMap<>();
        recentPerceptions = new HashMap<>();

        try {
            ei.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }

        ei.attachEnvironmentListener(new EnvironmentListener() {
            public void handleNewEntity(String entity) {
            }

            public void handleStateChange(EnvironmentState s) {
                logger.info("new state " + s);
            }

            public void handleDeletedEntity(String arg0, Collection<String> arg1) {
            }

            public void handleFreeEntity(String arg0, Collection<String> arg1) {
            }
        });

        System.out.println(ei.getEntities().size());
        for (String e : ei.getEntities()) {
            System.out.println("Register agent " + e);
            agentLocations.put(e, new AgentLocation());

            try {
                ei.registerAgent(e);
            } catch (AgentException e1) {
                e1.printStackTrace();
            }

            ei.attachAgentListener(e, this);

            try {
                ei.associateEntity(e, e);
            } catch (RelationException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void handlePercept(String agent, Percept percept) {
    }

    @Override
    public List<Literal> getPercepts(String agName) {

        Collection<Literal> ps = super.getPercepts(agName);
        List<Literal> percepts = ps == null ? new ArrayList<>() : new ArrayList<>(ps);
        List<Literal> operatorPercepts = new ArrayList<>(percepts);

        clearPercepts(agName);

        // The operator is an agent that only resides on the local machine,
        // it does not participate as an entity in the competition
        if (agName.equals("operator")) {
            for (Map.Entry<String, List<Literal>> agPerceptEntry : recentPerceptions.entrySet()) {
                List<Literal> agPercepts = agPerceptEntry.getValue();
                percepts.addAll(agPercepts);
            }

            return percepts;

        }


        if (ei != null) {
            try {

                AgentLocation curAgentLocation = agentLocations.get(agName);

                Map<String, Collection<Percept>> perMap = ei.getAllPercepts(agName);
                Stream<Percept> perStream = perMap.get(agName).stream();
                Percept actionIDPercept = perStream.filter(per -> per.getName().equalsIgnoreCase("actionID")).findFirst().orElse(null);


                // Only process location updates when there is a new action ID available
                if (actionIDPercept != null) {
                    int curActionID = ((Numeral) actionIDPercept.getParameters().getFirst()).getValue().intValue();
                    if (curActionID > curAgentLocation.getLastActionId()) {

                        curAgentLocation.updateAgentLocation(perMap.get(agName).stream());
                        curAgentLocation.setLastActionId(curActionID);
                    }
                }

                // Process Agent Perceptions
                for (String entity : perMap.keySet()) {

                    Structure strcEnt = ASSyntax.createStructure("entity", ASSyntax.createAtom(entity));

                    try {
                        Literal perceptLit = perceptToLiteral(curAgentLocation).addAnnots(strcEnt);
                        percepts.add(perceptLit);

                        operatorPercepts.add(perceptToLiteral(new Atom(entity), curAgentLocation).addAnnots(strcEnt).addSource(new Atom(entity)));
                    } catch (JasonException e) {
                        e.printStackTrace();
                    }

                    for (Percept p : perMap.get(entity)) {
                        try {
                            percepts.add(perceptToLiteral(p).addAnnots(strcEnt));
                            if (!p.getName().equals("task"))
                                operatorPercepts.add(perceptToLiteral(new Atom(entity), p));
                        } catch (JasonException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (PerceiveException e) {
                logger.log(Level.WARNING, "Could not perceive.");
            }
        }

//        if(agName.equals("agentA1"))
        recentPerceptions.put(agName, operatorPercepts);
        return percepts;
    }

    @Override
    public boolean executeAction(String agName, Structure action) {

        if (ei == null) {
            logger.warning("There is no environment loaded! Ignoring action " + action);
            return false;
        }

        try {
            ei.performAction(agName, literalToAction(action));
            return true;
        } catch (ActException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Called before the end of MAS execution
     */
    @Override
    public void stop() {
        if (ei != null) {
            try {
                if (ei.isKillSupported()) ei.kill();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.stop();
    }

    private static Literal perceptToLiteral(Atom namespace, Percept per) throws JasonException {
        Literal l;
        if (namespace == null)
            l = ASSyntax.createLiteral(per.getName());
        else
            l = ASSyntax.createLiteral(namespace, per.getName());

        for (Parameter par : per.getParameters())
            l.addTerm(parameterToTerm(par));
        return l;
    }

    private static Literal perceptToLiteral(Percept per) throws JasonException {

        Atom namespace = new Atom("percept");
        return perceptToLiteral(namespace, per);
    }

    private static Term parameterToTerm(Parameter par) throws JasonException {
        if (par instanceof Numeral) {
            return ASSyntax.createNumber(((Numeral) par).getValue().doubleValue());
        } else if (par instanceof Identifier) {
            try {
                Identifier i = (Identifier) par;
                String a = i.getValue();
                if (!Character.isUpperCase(a.charAt(0)))
                    return ASSyntax.parseTerm(a);
            } catch (Exception ignored) {
            }
            return ASSyntax.createString(((Identifier) par).getValue());
        } else if (par instanceof ParameterList) {
            ListTerm list = new ListTermImpl();
            ListTerm tail = list;
            for (Parameter p : (ParameterList) par)
                tail = tail.append(parameterToTerm(p));
            return list;
        } else if (par instanceof Function) {
            Function f = (Function) par;
            Structure l = ASSyntax.createStructure(f.getName());
            for (Parameter p : f.getParameters())
                l.addTerm(parameterToTerm(p));
            return l;
        }
        throw new JasonException("The type of parameter " + par + " is unknown!");
    }

    private static Action literalToAction(Literal action) {
        Parameter[] pars = new Parameter[action.getArity()];
        for (int i = 0; i < action.getArity(); i++)
            pars[i] = termToParameter(action.getTerm(i));
        return new Action(action.getFunctor(), pars);
    }

    private static Parameter termToParameter(Term t) {
        if (t.isNumeric()) {
            try {
                double d = ((NumberTerm) t).solve();
                if ((d == Math.floor(d)) && !Double.isInfinite(d)) return new Numeral((int) d);
                return new Numeral(d);
            } catch (NoValueException e) {
                e.printStackTrace();
            }
            return new Numeral(null);
        } else if (t.isList()) {
            Collection<Parameter> terms = new ArrayList<>();
            for (Term listTerm : (ListTerm) t)
                terms.add(termToParameter(listTerm));
            return new ParameterList(terms);
        } else if (t.isString()) {
            return new Identifier(((StringTerm) t).getString());
        } else if (t.isLiteral()) {
            Literal l = (Literal) t;
            if (!l.hasTerm()) {
                return new Identifier(l.getFunctor());
            } else {
                Parameter[] terms = new Parameter[l.getArity()];
                for (int i = 0; i < l.getArity(); i++)
                    terms[i] = termToParameter(l.getTerm(i));
                return new Function(l.getFunctor(), terms);
            }
        }
        return new Identifier(t.toString());
    }
}
