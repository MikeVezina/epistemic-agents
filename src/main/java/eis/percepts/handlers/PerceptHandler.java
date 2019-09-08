package eis.percepts.handlers;

import eis.iilang.Percept;

import java.util.ArrayList;
import java.util.List;

public abstract class PerceptHandler {

    private List<Percept> collectedPercepts;
    private long step;
    private String agentSource;

    protected PerceptHandler(String agentSource)
    {
        this.agentSource = agentSource;
        step = -1;
        collectedPercepts = new ArrayList<>();
    }

    protected abstract boolean shouldHandlePercept(Percept p);
    public abstract void perceptProcessingFinished();

    public void handlePercept(Percept p)
    {
        if(shouldHandlePercept(p))
            collectedPercepts.add((Percept) p.clone());
    }

    public void prepareStep(long step) {
        this.collectedPercepts.clear();
        this.step = step;
    }

    protected List<Percept> getCollectedPercepts() {
        return collectedPercepts;
    }

    public String getAgentSource() {
        return agentSource;
    }

    public long getStep() {
        return step;
    }
}
