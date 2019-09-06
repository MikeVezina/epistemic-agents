package eis.listeners;

import utils.Position;

public interface AgentLocationListener {
    void agentLocationUpdated(String agent, Position newLocation);
}
