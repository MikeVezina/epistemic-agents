package eis.percepts;

import aima.core.agent.Percept;
import jason.asSyntax.Literal;
import utils.Position;

import java.util.List;

public class AgentInfo {
    private String name;
    private Position location;
    private String team;
    private int energy;
    private int score;
    private boolean disabled;

    public AgentInfo(String name, Position location, String team, int score, int energy, boolean disabled) {
        this.name = name;
        this.location = location;
        this.team = team;
        this.score = score;
        this.energy = energy;
        this.disabled = disabled;
    }

    public String getName() {
        return name;
    }

    public Position getLocation() {
        return location;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int getScore() {
        return score;
    }

    public String getTeam() {
        return team;
    }
    public static AgentInfo ParseAgentInfo() {
        //ListLiteral name, Literal location, Literal score, Literal energy, Literal disabled
        return null;
    }


}
