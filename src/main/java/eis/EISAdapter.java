package eis;

import eis.listeners.SynchronizedPerceptListener;
import eis.percepts.MapPercept;
import eis.percepts.agent.*;
import eis.percepts.things.Block;
import jason.JasonException;
import jason.NoValueException;
import jason.asSyntax.*;
import eis.exceptions.*;
import eis.iilang.*;
import jason.environment.Environment;
import massim.eismassim.EnvironmentInterface;
import utils.Direction;
import utils.PerceptUtils;
import utils.Position;
import utils.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private static EISAdapter singleton;
    private EnvironmentInterface ei;

    private ConcurrentMap<String, Map<String, Position>> authenticatedAgents;



    private Map<String, AgentContainer> agentContainers;

    private int lastUpdateStep = -1;

    public EISAdapter() {
        super(20);
        singleton = this;
    }

    public static EISAdapter getSingleton() {
        return singleton;
    }
    @Override
    public void init(String[] args) {

        ei = new EnvironmentInterface("conf/eismassimconfig.json");
        authenticatedAgents = new ConcurrentHashMap<>();
        agentContainers = new HashMap<>();

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

        for (String e : ei.getEntities()) {
            System.out.println("Register agent " + e);
            authenticatedAgents.put(e, new HashMap<>());
            agentContainers.put(e, new AgentContainer(e));

            try {
                ei.registerAgent(e);

            } catch (AgentException e1) {
                e1.printStackTrace();
            }

//            ei.attachAgentListener(e, SynchronizedPerceptListener.getInstance());
            ei.attachAgentListener(e, this);

            try {
                ei.associateEntity(e, e);
            } catch (RelationException e1) {
                e1.printStackTrace();
            }
        }

        SynchronizedPerceptListener.getInstance().start();
    }

    @Override
    public void handlePercept(String agent, Percept percept) {
//        System.out.println(percept);
//        if (false && percept.getName().equals("step")) {
//            checkSetStaticInfo(agent);
//            System.out.println(percept);
//
//         //   System.out.println(percept);
//
//            try {
//                long step = PerceptUtils.GetNumberParameter(percept, 0).intValue();
//                List<Percept> perceptList = List.copyOf(ei.getAllPercepts(agent).get(agent));
//                taskList.updateTaskList(step, perceptList);
//
//                AgentContainer container = agentContainers.get(agent);
//                container.updatePerceptions(step, perceptList);
//
//            } catch (PerceiveException e) {
//                e.printStackTrace();
//            }
//        }

    }

    public Map<String, AgentContainer> getAgentContainers() {
        return agentContainers;
    }

    public AgentContainer getAgentContainer(String agentName) {
        return agentContainers.getOrDefault(agentName, null);
    }

    @Override
    public List<Literal> getPercepts(String agName) {
        Structure strcEnt = ASSyntax.createStructure("entity", ASSyntax.createAtom(agName));

        Collection<Literal> ps = super.getPercepts(agName);
        List<Literal> percepts = ps == null ? new ArrayList<>() : new ArrayList<>(ps);

        if (ei == null) {
            throw new RuntimeException("Failed to get environment.");
        }


        // The perceptions that are copied to the operator BB
        List<Literal> operatorPercepts = new ArrayList<>(percepts);


        clearPercepts(agName);


        // The operator should only receive task perceptions.
        if (agName.equals("operator")) {

            return percepts;
        }

        AgentContainer agentContainer = agentContainers.get(agName);

        if(agentContainer == null)
            throw new RuntimeException("Failed to get agent container for: " + agName);

        List<Percept> agentPercepts = agentContainer.getCurrentPerceptions();

        try {
            Literal perceptLit = perceptToLiteral(agentContainer.getAgentLocation()).addAnnots(strcEnt);
            percepts.add(perceptLit);

            operatorPercepts.add(perceptToLiteral(new Atom(agName), agentContainer.getAgentLocation()).addAnnots(strcEnt).addSource(new Atom(agName)));
        } catch (JasonException e) {
            e.printStackTrace();
        }


        for (Percept p : agentPercepts) {
            try {

                p = mapEntityPerceptions(agentContainer, p);

                // Do not include perceptions that are filtered out
                if (p == null)
                    continue;

                percepts.add(perceptToLiteral(p).addAnnots(strcEnt));
                if (!p.getName().equals("task"))
                    operatorPercepts.add(perceptToLiteral(new Atom(agName), p));
            } catch (JasonException e) {
                e.printStackTrace();
            }
        }

        // Add team mate relative perceptions
        percepts.addAll(addAuthenticatedTeammates(agName, strcEnt));
        percepts.addAll(addTranslationValues(agName, strcEnt));

        return percepts;
    }

    private List<Literal> addAuthenticatedTeammates(String entity, Structure strcEnt) {

        Position myPosition = agentContainers.get(entity).getCurrentLocation();


        return getAuthenticatedAgents(entity).map(auth -> {
            String otherAgentName = auth.getKey();
            Position translationValue = auth.getValue();

            Position otherAgentLocation = agentContainers.get(otherAgentName).getCurrentLocation();
            Position relativePosition = otherAgentLocation.subtract(myPosition).add(translationValue);

            Percept p = new Percept("teamAgent", new Numeral(relativePosition.getX()), new Numeral(relativePosition.getY()), new Identifier(otherAgentName));
            try {
                return perceptToLiteral(p).addAnnots(strcEnt);
            } catch (JasonException e) {
                logger.info("Failed to convert percept to literal.");
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toUnmodifiableList());
    }

    private List<Literal> addTranslationValues(String entity, Structure strcEnt) {

        return getAuthenticatedAgents(entity).map(auth -> {
            String otherAgentName = auth.getKey();
            Position translationValue = auth.getValue();

            Percept p = new Percept("locationTranslation", new Identifier(otherAgentName), new Numeral(translationValue.getX()), new Numeral(translationValue.getY()));
            try {
                return perceptToLiteral(p).addAnnots(strcEnt);
            } catch (JasonException e) {
                logger.info("Failed to convert percept to literal.");
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
    }

    public AgentMap getAgentMap(String agentName) {
        return agentContainers.get(agentName).getAgentMap();
    }

    private Percept mapEntityPerceptions(AgentContainer agentContainer, Percept p) {
        if (!p.getName().equalsIgnoreCase("thing") || !((Identifier) p.getParameters().get(2)).getValue().equalsIgnoreCase("entity"))
            return p;

        String entity = agentContainer.getAgentName();

        String team = PerceptUtils.GetStringParameter(p, 3);

        // Other team perception
        if (!team.equals(agentContainer.getPerceptContainer().getSharedPerceptContainer().getTeamName()))
            return p;

        Position myPosition = agentContainers.get(entity).getCurrentLocation();
        Position relativePosition = new Position(PerceptUtils.GetNumberParameter(p, 0).intValue(), PerceptUtils.GetNumberParameter(p, 1).intValue());

        if (relativePosition.equals(new Position(0, 0)))
            // Modify self perception
            return new Percept("self", new Numeral(relativePosition.getX()), new Numeral(relativePosition.getY()), new Identifier(entity));

        long numberOfMatchingAgents = getAuthenticatedAgents(entity).filter(auth -> {
            String otherAgentName = auth.getKey();
            Position translationValue = auth.getValue();

            Position otherAgentLocation = agentContainers.get(otherAgentName).getCurrentLocation();
            return otherAgentLocation.subtract(myPosition).add(translationValue).equals(relativePosition);
        }).count();

        if (numberOfMatchingAgents == 1)
            return null;
        else if (numberOfMatchingAgents > 1) {
            logger.warning("There are multiple agents with the same translation coordinates for " + entity);
            logger.warning("Authenticated Agents: " + authenticatedAgents.get(entity));
        }

        return p;
    }

    private Stream<Map.Entry<String, Position>> getAuthenticatedAgents(String agent) {
        return authenticatedAgents.get(agent).entrySet().parallelStream();
    }

    private void setAuthenticatedAgent(String agent1, String agent2, Position pos) {
        if (agent1.equals(agent2)) {
            logger.info("Attempting to authenticate the same agent.");
            return;
        }

        Map<String, Position> agent1Auth = authenticatedAgents.get(agent1);
        Map<String, Position> agent2Auth = authenticatedAgents.get(agent2);

        if (agent1Auth.containsKey(agent2) && agent2Auth.containsKey(agent1)) {
            logger.info("Attempting to authenticate previously authenticated agents.");
            return;
        }


        AgentContainer agentContainer1 = agentContainers.get(agent1);
        AgentContainer agentContainer2 = agentContainers.get(agent2);

        if (pos != null) {
            agent1Auth.put(agent2, pos);
            agent2Auth.put(agent1, pos.negate());

            agentContainer1.getAgentAuthentication().authenticateAgent(agentContainer2, pos);
            agentContainer2.getAgentAuthentication().authenticateAgent(agentContainer1, pos.negate());

//            checkForTrivialAuthentication(agent1, agent2, pos);
//            checkForTrivialAuthentication(agent2, agent1, pos.negate());
        }

    }

    private void checkForTrivialAuthentication(String agent1, String agent2, Position translation) {
        Map<String, Position> agent1Auth = authenticatedAgents.get(agent1);
        Map<String, Position> agent2Auth = authenticatedAgents.get(agent2);


        // Find authentications with agent 2 that agent 1 has not received yet
        getAuthenticatedAgents(agent1).filter(auth ->
                !auth.getKey().equals(agent2) && !agent2Auth.containsKey(auth.getKey())
        ).collect(Collectors.toList()).forEach(auth -> {
            String agentName = auth.getKey();
            Position authPos = auth.getValue();

            // Translate any trivial agents
            Position newTranslation = authPos.subtract(translation);
            setAuthenticatedAgent(agent2, agentName, newTranslation);
        });


    }

    private Position getAuthenticatedAgents(String agent1, String agent2) {
        return authenticatedAgents.get(agent1).get(agent2);
    }

    int expectedUpdate = -1;

    @Override
    public boolean executeAction(String agName, Structure action) {

        if (ei == null) {
            logger.warning("There is no environment loaded! Ignoring action " + action);
            return false;
        }

        if (action.getFunctor().equalsIgnoreCase("authenticateAgents")) {
            Atom origin = (Atom) action.getTerm(0);
            Atom authAgent = (Atom) action.getTerm(1);
            Atom translation = (Atom) action.getTerm(2);

            Position pos = new Position(getNumberTermInt(translation.getTerm(0)), getNumberTermInt(translation.getTerm(1)));
            setAuthenticatedAgent(origin.getFunctor(), authAgent.getFunctor(), pos);

            return true;
        }

        if (action.getFunctor().equalsIgnoreCase("taskSubmitted")) {
            AgentContainer ent = agentContainers.get(agName);
            ent.taskSubmitted();

            return true;
        }

        if (action.getFunctor().equalsIgnoreCase("blockAttached")) {
            AgentContainer ent = agentContainers.get(agName);
            Literal dirLiteral = (Literal) action.getTerm(0);
            Direction dir = Utils.DirectionToRelativeLocation(dirLiteral.getFunctor());
            MapPercept mapPercept = agentContainers.get(agName).getAgentMap().getRelativePerception(dir);
            if(dir == null || mapPercept == null || !mapPercept.hasBlock())
                return false;

            Block block = mapPercept.getBlock();

            if(block == null)
                return false;

            ent.attachBlock(dir.getPosition());

            return true;
        }

        if (action.getFunctor().equalsIgnoreCase("blockDetached")) {
            AgentContainer ent = agentContainers.get(agName);
            Literal dirLiteral = (Literal) action.getTerm(0);
            Direction dir = Utils.DirectionToRelativeLocation(dirLiteral.getFunctor());
            MapPercept mapPercept = agentContainers.get(agName).getAgentMap().getRelativePerception(dir);

            if(dir == null || mapPercept == null || !mapPercept.hasBlock())
                return false;

            Block block = mapPercept.getBlock();

            if(block == null)
                return false;

            ent.detachBlock(dir.getPosition());

            return true;
        }

        if (action.getFunctor().equalsIgnoreCase("agentRotated")) {
            AgentContainer ent = agentContainers.get(agName);
            Atom rotationLiteral = (Atom) action.getTerm(0);
            Rotation rotate = Rotation.getRotation(rotationLiteral);
            ent.rotate(rotate);
            return true;
        }

        if (action.getFunctor().equals("addForbiddenDirection")) {
            Atom direction = (Atom) action.getTerm(0);
            Position dirPos = Utils.DirectionToRelativeLocation(direction.getFunctor()).getPosition();
            getAgentMap(agName).addForbidden(dirPos);
            return true;

        }

        try {
            if (action.getFunctor().equals("move")) {
                expectedUpdate = lastUpdateStep + 1;
            }

            System.err.println(action.getFunctor());

            ei.performAction(agName, literalToAction(action));
            return true;
        } catch (ActException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static int getNumberTermInt(Term t) {
        return (int) ((NumberTermImpl) t).solve();
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

    public static Literal perceptToLiteral(Atom namespace, Percept per) throws JasonException {
        Literal l;
        if (namespace == null)
            l = ASSyntax.createLiteral(per.getName());
        else
            l = ASSyntax.createLiteral(namespace, per.getName());

        for (Parameter par : per.getParameters())
            l.addTerm(parameterToTerm(par));
        return l;
    }

    public static Literal perceptToLiteral(Percept per) throws JasonException {

        Atom namespace = new Atom("percept");
        return perceptToLiteral(namespace, per);
    }

    public static Term parameterToTerm(Parameter par) throws JasonException {
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

    public static Action literalToAction(Literal action) {
        Parameter[] pars = new Parameter[action.getArity()];
        for (int i = 0; i < action.getArity(); i++)
            pars[i] = termToParameter(action.getTerm(i));
        return new Action(action.getFunctor(), pars);
    }

    public static Parameter termToParameter(Term t) {
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

    public EnvironmentInterface getEnvironmentInterface() {
        return ei;
    }
}
