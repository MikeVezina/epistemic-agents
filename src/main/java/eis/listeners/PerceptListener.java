package eis.listeners;

import eis.percepts.agent.AgentContainer;

public interface PerceptListener {
    void perceptsProcessed(AgentContainer agentContainer);
}
