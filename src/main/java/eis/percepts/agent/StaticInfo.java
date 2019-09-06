package eis.percepts.agent;

import eis.iilang.Percept;
import eis.percepts.handlers.AgentInfoPerceptHandler;

import java.util.List;

public class StaticInfo {

    private static StaticInfo singleStaticInfo;
    private AgentInfoPerceptHandler agentInfoPerceptHandler;
    private boolean hasBeenSet;

    private StaticInfo() {
        hasBeenSet = false;
        agentInfoPerceptHandler = new AgentInfoPerceptHandler();
    }

    public boolean hasBeenSet() {
        return hasBeenSet;
    }

    public synchronized void setInfo(List<Percept> initialPercepts) {
        if(hasBeenSet)
            return;

        agentInfoPerceptHandler.prepareStep(0);
        initialPercepts.forEach(agentInfoPerceptHandler::handlePercept);
        agentInfoPerceptHandler.perceptProcessingFinished();
        hasBeenSet = true;
    }


    public static StaticInfo getInstance() {
        if (singleStaticInfo == null)
            singleStaticInfo = new StaticInfo();
        return singleStaticInfo;
    }

    public String getTeam() {
        return agentInfoPerceptHandler.getTeam();
    }

    public int getVision() {
        return agentInfoPerceptHandler.getVision();
    }

}
