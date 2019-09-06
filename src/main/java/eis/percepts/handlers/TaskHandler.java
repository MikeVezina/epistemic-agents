package eis.percepts.handlers;

import eis.iilang.Percept;
import eis.percepts.Task;

import java.util.List;

public class TaskHandler extends PerceptMapper<Task> {
    public TaskHandler() {
        super(null);
    }

    @Override
    protected Task mapPercept(Percept p) {
        return Task.parseTask(p);
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Task.canParse(p);
    }

    @Override
    public void perceptProcessingFinished() {
        getMappedPercepts();
    }

    public List<Task> getTaskList() {
        return getMappedPercepts();
    }

    public List<Percept> getTaskListPercepts() {
        return getCollectedPercepts();
    }
}
