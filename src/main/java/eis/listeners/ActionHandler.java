package eis.listeners;

import eis.agent.AgentContainer;

public interface ActionHandler {
    void handleNewAction(AgentContainer agentContainer);
}
