package eis.percepts.parsers;

import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.containers.TaskSet;

import java.util.List;

public class TaskHandler extends PerceptMapper<Task> {
    @Override
    protected Task mapPercept(Percept p) {
        return Task.parseTask(p);
    }

    @Override
    protected boolean shouldHandlePercept(Percept p) {
        return Task.canParse(p);
    }

    public TaskSet mapTaskList(List<Percept> rawPercepts)
    {
        // Create a task list object based on the mapped list
        return new TaskSet(super.mapAllPercepts(rawPercepts));
    }
}
