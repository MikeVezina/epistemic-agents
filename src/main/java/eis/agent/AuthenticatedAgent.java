package eis.agent;

import map.Position;

public class AuthenticatedAgent {
    private AgentContainer authenticatedAgent;
    private Position translationValue;

    public AuthenticatedAgent(AgentContainer agentContainer, Position translationValue)
    {
        this.authenticatedAgent = agentContainer;
        this.translationValue = translationValue;
    }

    public AgentContainer getAgentContainer() {
        return authenticatedAgent;
    }

    public Position getTranslationValue() {
        return translationValue;
    }
}
