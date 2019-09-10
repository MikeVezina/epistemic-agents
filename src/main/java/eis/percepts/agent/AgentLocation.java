package eis.percepts.agent;

import eis.iilang.*;
import utils.Direction;
import utils.Position;

import java.util.LinkedList;

public class AgentLocation extends Percept {

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

    private Position currentLocation;

    public AgentLocation() {
        super("location");
        currentLocation = new Position(0, 0);
    }


    public Position getCurrentLocation() {
        return currentLocation;
    }

    public void updateAgentLocation(Direction dir) {
        if (dir == null)
            return;

        this.currentLocation = currentLocation.add(dir.getPosition());
    }

    @Override
    public LinkedList<Parameter> getParameters() {
        LinkedList<Parameter> params = new LinkedList<>();

        params.add(new Numeral(currentLocation.getX()));
        params.add(new Numeral(currentLocation.getY()));

        return params;
    }


}
