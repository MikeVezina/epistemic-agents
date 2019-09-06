package eis.percepts.handlers;

import eis.iilang.Percept;

import java.util.ArrayList;
import java.util.List;

public abstract class PerceptMapper<R> extends PerceptHandler {
    private List<R> mappedPercepts;

    protected PerceptMapper(String agentSource) {
        super(agentSource);
        mappedPercepts = new ArrayList<>();
    }

    protected List<R> getMappedPercepts() {
        return mappedPercepts;
    }

    @Override
    public void prepareStep(long step)
    {
        super.prepareStep(step);
        this.mappedPercepts.clear();
    }

    public abstract R mapPercept(Percept p);

    public void handlePercept(Percept p) {
        if (shouldHandlePercept(p)) {
            Percept cloned = (Percept) p.clone();
            super.getCollectedPercepts().add(cloned);
            R mappedItem = mapPercept(cloned);
            if (mappedItem != null)
                mappedPercepts.add(mappedItem);
        }
    }
}

