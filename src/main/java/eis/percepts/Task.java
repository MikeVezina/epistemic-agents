package eis.percepts;

import eis.iilang.*;
import eis.percepts.requirements.Requirement;

import java.util.ArrayList;
import java.util.List;

public class Task extends ParsedPercept {
    public static final String PERCEPT_NAME = "task";
    private String name;
    private int deadline;
    private int reward;
    private List<Requirement> requirementList;

    private Task(String name, int deadline, int reward, List<Requirement> requirements) {
        this.name = name;
        this.deadline = deadline;
        this.reward = reward;
        this.requirementList = requirements;
    }

    public String getName() {
        return name;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getReward() {
        return reward;
    }

    public List<Requirement> getRequirementList() {
        return requirementList;
    }


    public static Task parseTask(Percept l) {
        String name = ((Identifier) l.getParameters().get(0)).getValue();
        int deadline = ((Numeral) l.getParameters().get(1)).getValue().intValue();
        int reward = ((Numeral) l.getParameters().get(2)).getValue().intValue();
        ParameterList requirementParamList = ((ParameterList) l.getParameters().get(3));

        List<Requirement> requirementList = parseRequirementList(requirementParamList);
        return new Task(name, deadline, reward, requirementList);
    }

    private static List<Requirement> parseRequirementList(ParameterList reqParamList) {
        ArrayList<Requirement> reqs = new ArrayList<>();
        reqParamList.forEach(req -> reqs.add(Requirement.parseRequirementParam((Function) req)));
        return reqs;
    }

    public static boolean canParse(Percept l) {
        return l != null && l.getName().equalsIgnoreCase(PERCEPT_NAME);
    }
}
