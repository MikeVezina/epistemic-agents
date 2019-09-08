package eis.percepts.handlers;

import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.percepts.agent.AgentLocation;
import utils.Direction;
import utils.PerceptUtils;
import utils.Utils;

public class AgentLocationPerceptHandler extends PerceptHandler {
    private AgentLocation agentLocation;

    public AgentLocationPerceptHandler(String agentName, AgentLocation agentLocation)
    {
        super(agentName);
        this.agentLocation = agentLocation;
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        for(AgentLocation.ActionResultPerception actionPercept : AgentLocation.ActionResultPerception.values())
        {
            if(actionPercept.getPerceptName().equals(p.getName()))
                return true;
        }
        return false;
    }

    @Override
    public void perceptProcessingFinished() {
        if (getCollectedPercepts().size() != 3)
        {
            System.out.println("Failed to get three percepts.");
            return;
        }

        Percept lastActionResult = null;
        Percept lastAction = null;
        Percept lastActionParams = null;

        for (Percept percept : getCollectedPercepts())
        {
            if(percept.getName().equals(AgentLocation.ActionResultPerception.RESULT.getPerceptName()))
                lastActionResult = percept;
            if(percept.getName().equals(AgentLocation.ActionResultPerception.ACTION.getPerceptName()))
                lastAction = percept;
            if(percept.getName().equals(AgentLocation.ActionResultPerception.PARAMS.getPerceptName()))
                lastActionParams = percept;
        }

        if(lastActionResult == null || lastAction == null || lastActionParams == null)
        {
            System.out.println("Failed to parse the action percepts.");
            return;
        }

        if (isLastActionMove(lastAction) && isLastActionResultSuccess(lastActionResult))
        {
            Direction dir  = getDirection(lastActionParams);
            agentLocation.updateAgentLocation(dir);
            System.out.println(agentLocation.getCurrentLocation());
        }
        else
        {
        }
    }

    public AgentLocation getAgentLocation() {
        return agentLocation;
    }

    private boolean isLastActionResultSuccess(Percept lastActionResultPercept) {
        return PerceptUtils.MatchPerceptFirstIdentifier(lastActionResultPercept, "success");
    }

    private boolean isLastActionMove(Percept lastActionPercept) {
        return PerceptUtils.MatchPerceptFirstIdentifier(lastActionPercept, "move");
    }

    private Direction getDirection(Percept lastActionParamsPercept) {
        Parameter firstParam = PerceptUtils.GetFirstParameter(lastActionParamsPercept);

        if(!(firstParam instanceof ParameterList))
            return null;

        ParameterList paramList = (ParameterList) firstParam;

        if(paramList.size() == 0 || !(paramList.get(0) instanceof Identifier))
            return null;

        return Utils.DirectionToRelativeLocation(((Identifier) paramList.get(0)).getValue());

    }
}
