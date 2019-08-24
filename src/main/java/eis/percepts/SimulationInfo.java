package eis.percepts;



import eis.iilang.Percept;
import utils.PerceptUtils;

import java.util.List;

public class SimulationInfo {
    private static final String STEP_PERCEPT_NAME = "step";
    private static final String DEADLINE_PERCEPT_NAME = "deadline";
    private static final String ACTION_ID_PERCEPT_NAME = "actionID";
    private static final String VISION_PERCEPT_NAME = "vision";

    private long step;
    private long deadline;
    private long actionID;
    private int vision;


    public SimulationInfo(long step, long deadline, long actionID, int vision)
    {
        this.step = step;
        this.deadline = deadline;
        this.actionID = actionID;
        this.vision = vision;
    }

    public long getStep() {
        return step;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getActionID() {
        return actionID;
    }

    public int getVision() {
        return vision;
    }

    public static boolean isSimPercept(Percept p)
    {
        return p.getName().equalsIgnoreCase(STEP_PERCEPT_NAME) || p.getName().equalsIgnoreCase(DEADLINE_PERCEPT_NAME)
                || p.getName().equalsIgnoreCase(ACTION_ID_PERCEPT_NAME) || p.getName().equalsIgnoreCase(VISION_PERCEPT_NAME);
    }

    public static SimulationInfo parseSimulationPercepts(List<Percept> percepts)
    {
        if(percepts.size() != 4)
            throw new RuntimeException("Invalid Percepts passed to Simulation Info");

        long step = -1;
        long deadline = -1;
        long actionID = -1;
        int vision = -1;

        for(Percept p : percepts)
        {
            if(p.getName().equalsIgnoreCase(STEP_PERCEPT_NAME))
                step = PerceptUtils.GetFirstNumberParameter(p).longValue();
            else if (p.getName().equalsIgnoreCase(DEADLINE_PERCEPT_NAME))
                deadline = PerceptUtils.GetFirstNumberParameter(p).longValue();
            else if (p.getName().equalsIgnoreCase(ACTION_ID_PERCEPT_NAME))
                actionID = PerceptUtils.GetFirstNumberParameter(p).longValue();
            else if (p.getName().equalsIgnoreCase(VISION_PERCEPT_NAME))
                vision = PerceptUtils.GetFirstNumberParameter(p).intValue();
            else
                throw new RuntimeException("Invalid Percept passed to Simulation Info: " + p);
        }

        return new SimulationInfo(step, deadline, actionID, vision);
    }
}
