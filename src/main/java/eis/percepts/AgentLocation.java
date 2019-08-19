package eis.percepts;

import eis.EISAdapter;
import eis.iilang.*;
import utils.Position;
import utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentLocation extends Percept {


    private enum ActionResultPerception {
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

    public AgentLocation() {
        super("location");
        lastActionId = -1;
        currentLocation = new Position();
    }


    public int getLastActionId() {
        return lastActionId;
    }

    public void setLastActionId(int lastActionId) {
        this.lastActionId = lastActionId;
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

    public void updateAgentLocation(Stream<Percept> perStream) {
        List<Percept> lastActionResults = perStream.filter(per -> per.getName().contains("lastAction")).collect(Collectors.toList());

        if (lastActionResults.size() != 3)
            return;

        Percept lastActionResultPercept = lastActionResults.stream().filter(per -> per.getName().equalsIgnoreCase(ActionResultPerception.RESULT.getPerceptName())).findFirst().orElse(null);
        Percept lastActionPercept = lastActionResults.stream().filter(per -> per.getName().equalsIgnoreCase(ActionResultPerception.ACTION.getPerceptName())).findFirst().orElse(null);
        Percept lastActionParamsPercept = lastActionResults.stream().filter(per -> per.getName().equalsIgnoreCase(ActionResultPerception.PARAMS.getPerceptName())).findFirst().orElse(null);


        if (!lastActionSuccess(lastActionResultPercept) || !lastActionMove(lastActionPercept))
            return;

        String dir = getDirection(lastActionParamsPercept);
        this.currentLocation = currentLocation.add(Utils.DirectionToRelativeLocation(dir));


        System.out.println("Position is: " + currentLocation);

    }

    @Override
    public LinkedList<Parameter> getParameters() {
        LinkedList<Parameter> params = new LinkedList<>();

        params.add(new Numeral(currentLocation.getX()));
        params.add(new Numeral(currentLocation.getY()));

        return params;
    }


}
