package eis.listeners;

import eis.percepts.handlers.AgentPerceptManager;

public interface PerceptListener {
    void perceptsProcessed(AgentPerceptManager perceptManager);
}
