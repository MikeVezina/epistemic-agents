package eis.listeners;

import eis.EISAdapter;
import eis.exceptions.PerceiveException;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import eis.percepts.agent.AgentContainer;
import eis.percepts.containers.SharedPerceptContainer;
import massim.eismassim.EnvironmentInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SynchronizedPerceptListener extends Thread {

    private static SynchronizedPerceptListener synchronizedPerceptListener;
    private EnvironmentInterface environmentInterface;

    public SynchronizedPerceptListener(EnvironmentInterface environmentInterface) {
        this.environmentInterface = environmentInterface;
    }

    public static SynchronizedPerceptListener getInstance() {
        if (synchronizedPerceptListener == null)
            synchronizedPerceptListener = new SynchronizedPerceptListener(EISAdapter.getSingleton().getEnvironmentInterface());

        return synchronizedPerceptListener;
    }

    @Override
    public void run() {
        // Thread pool for percept parsers
        // ExecutorService perceptListenerExecutorService = Executors.newCachedThreadPool();

        while (environmentInterface.getState() != EnvironmentState.KILLED) {


            // Check for new perceptions & update the agents.
            environmentInterface.getEntities().forEach(e -> {
                try {
                    Map<String, Collection<Percept>> perceptMap = environmentInterface.getAllPercepts(e);
                    List<Percept> perceptList = List.copyOf(perceptMap.getOrDefault(e, new ArrayList<>()));
                    AgentContainer agentContainer = EISAdapter.getSingleton().getAgentContainer(e);
                    agentContainer.updatePerceptions(perceptList);
                } catch (PerceiveException ex) {
                    ex.printStackTrace();
                }
            });
        }

        // Agents can now perform any updates based on the perception updates
        EISAdapter.getSingleton().getAgentContainers().values().forEach(AgentContainer::notifyPerceptsUpdated);
    }
}
