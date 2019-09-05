package eis.listeners;

import eis.percepts.AgentMap;
import utils.Position;

public interface AgentLocationListener {
    void agentLocationUpdated(String agent, Position newLocation);
}
