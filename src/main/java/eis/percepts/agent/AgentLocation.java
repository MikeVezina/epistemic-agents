package eis.percepts.agent;

import eis.EISAdapter;
import eis.iilang.*;
import eis.listeners.AgentLocationListener;
import utils.Direction;
import utils.PerceptUtils;
import utils.Position;
import utils.Utils;

import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentLocation extends Percept {


    private ConcurrentLinkedQueue<AgentLocationListener> agentLocationListeners;
    private String agentName;

    public enum ActionResultPerception {
        RESULT("lastActionResult"),
        ACTION("lastAction"),
        PARAMS("lastActionParams");

        private String name;

        ActionResultPerception(String name) {
            this.name = name;
        }

        public String getPerceptName() {
            return this.name;
        }

        public static ActionResultPerception GetEnumFromPerceptName(String name) {
            for (ActionResultPerception value : ActionResultPerception.values()) {
                if (value.getPerceptName().equalsIgnoreCase(name))
                    return value;
            }
            return null;
        }
    }

    private int lastActionId;
    private Position currentLocation;

    public AgentLocation(String agentName) {
        super("location");
        lastActionId = -1;
        currentLocation = new Position(0, 0);
        this.agentName = agentName;
        agentLocationListeners = new ConcurrentLinkedQueue<>();
    }


    public int getLastActionId() {
        return lastActionId;
    }

    public void setLastActionId(int lastActionId) {
        this.lastActionId = lastActionId;
    }

    public Position getCurrentLocation()
    {
        return currentLocation;
    }


    private boolean lastActionSuccess(Percept lastActionResultPercept) {
        return PerceptUtils.MatchPerceptFirstIdentifier(lastActionResultPercept, "success");
    }

    private boolean lastActionMove(Percept lastActionPercept) {
        return PerceptUtils.MatchPerceptFirstIdentifier(lastActionPercept, "move");
    }

    private String getDirection(Percept lastActionParamsPercept) {
        Parameter firstParam = PerceptUtils.GetFirstParameter(lastActionParamsPercept);

        if(!(firstParam instanceof ParameterList))
            return "";

        ParameterList paramList = (ParameterList) firstParam;

        if(paramList.size() == 0 || !(paramList.get(0) instanceof Identifier))
            return "";

        return ((Identifier) paramList.get(0)).getValue();

    }

    public void updateAgentLocation(Direction dir) {

        this.currentLocation = currentLocation.add(dir.getPosition());


        System.out.println("Position is: " + currentLocation);
        for(AgentLocationListener listener : agentLocationListeners)
        {
            listener.agentLocationUpdated(this.agentName, this.currentLocation);
        }

    }

    public void addListener(AgentLocationListener listener)
    {
        if(listener != null)
            agentLocationListeners.add(listener);
    }

    @Override
    public LinkedList<Parameter> getParameters() {
        LinkedList<Parameter> params = new LinkedList<>();

        params.add(new Numeral(currentLocation.getX()));
        params.add(new Numeral(currentLocation.getY()));

        return params;
    }


}
