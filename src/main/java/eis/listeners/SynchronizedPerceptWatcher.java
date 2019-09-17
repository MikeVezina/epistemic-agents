package eis.listeners;

import eis.EISAdapter;
import eis.exceptions.PerceiveException;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import eis.agent.AgentContainer;
import eis.percepts.containers.InvalidPerceptCollectionException;
import eis.percepts.containers.SharedPerceptContainer;
import massim.eismassim.EnvironmentInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Stopwatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * This class is responsible for polling agent percepts and updating the AgentContainer objects upon retrieval of new
 * percepts.
 */
public class SynchronizedPerceptWatcher extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger("PerceptWatcher");
    private static SynchronizedPerceptWatcher synchronizedPerceptWatcher;

    // Contain the agent containers
    private ConcurrentMap<String, AgentContainer> agentContainers;
    private EnvironmentInterface environmentInterface;

    public SynchronizedPerceptWatcher(EnvironmentInterface environmentInterface) {
        this.environmentInterface = environmentInterface;
        agentContainers = new ConcurrentHashMap<>();

        // Set the thread name
        setName("SynchronizedPerceptWatcherThread");
    }

    private synchronized void initializeAgentContainers() {
        environmentInterface.getAgents();
    }

    @Override
    public synchronized void start() {
        if(agentContainers.isEmpty())
            throw new RuntimeException("The agent containers have not been set yet.");

        super.start();
    }

    public static SynchronizedPerceptWatcher getInstance() {
        if (synchronizedPerceptWatcher == null)
            synchronizedPerceptWatcher = new SynchronizedPerceptWatcher(EISAdapter.getSingleton().getEnvironmentInterface());

        return synchronizedPerceptWatcher;
    }

    private void waitForEntityConnection(String entity) {
        while (!environmentInterface.isEntityConnected(entity)) {
            LOG.warn("Not connected. Waiting for a connection.");

            // Sleep before we try again.
            try {
                Thread.sleep(500);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * This method requests entity perceptions.
     * The request will block if no new percepts have arrived since the last call
     *
     * @param entity The name of the registered entity
     */
    private void updateAgentPercepts(String entity) throws InvalidPerceptCollectionException {
        try {

            LOG.info("Waiting for new Perceptions [" + entity + "]...");
            Map<String, Collection<Percept>> perceptMap = environmentInterface.getAllPercepts(entity);
            LOG.info("Received new Perceptions [" + entity + "]...");

            Stopwatch sw = Stopwatch.startTiming();
            List<Percept> perceptList = new ArrayList<>(perceptMap.getOrDefault(entity, new ArrayList<>()));
            AgentContainer agentContainer = getAgentContainer(entity);
            agentContainer.updatePerceptions(perceptList);
            long time = sw.stopMS();

            if (time > 10)
                LOG.warn("Update perceptions took " + time + " ms.");
        } catch (PerceiveException ex) {
            ex.printStackTrace();
        }
    }

    public AgentContainer getAgentContainer(String e) {
        return EISAdapter.getSingleton().getAgentContainer(e);
    }

    @Override
    public void run() {
        // Thread pool for percept parsers
        // ExecutorService perceptListenerExecutorService = Executors.newCachedThreadPool();

        while (environmentInterface.getState() != EnvironmentState.KILLED) {

            try {
                // Check for new perceptions & update the agents.
                environmentInterface.getEntities().forEach(e -> {
                    waitForEntityConnection(e);
                    updateAgentPercepts(e);
                });
            } catch (InvalidPerceptCollectionException e) {
                // We want to ignore if this exception is a result of the sim-start message. (we don't need to parse that).
                // This was a bad/lazy way of handling this but I didn't want to spend too much time on this.
                if (!e.isStartPercepts())
                    throw e;

                continue;
            }


            Stopwatch sw = Stopwatch.startTiming();
            // Agents should now update their respective maps
            environmentInterface.getEntities().stream().map(this::getAgentContainer).forEach(AgentContainer::updateMap);

            // Agents should now synchronize maps.
            environmentInterface.getEntities().stream().map(this::getAgentContainer).forEach(AgentContainer::synchronizeMap);


            // Agents can now perform any updates based on the perception updates
            agentContainers.values().forEach(AgentContainer::notifyActionHandlers);

            long deltaTime = sw.stopMS();

            if (deltaTime > 500 && agentContainers.size() > 0)
                LOG.warn("Step " + agentContainers.get(environmentInterface.getEntities().getFirst()).getSharedPerceptContainer().getStep() + " took " + deltaTime + " ms to process map updates and synchronization.");
        }
    }

    public synchronized SharedPerceptContainer getSharedPercepts() {
        return null;
    }
}
