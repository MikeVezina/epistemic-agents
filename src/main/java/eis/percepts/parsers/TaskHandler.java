package eis.percepts.parsers;

import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.containers.TaskList;
import eis.percepts.parsers.PerceptMapper;

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

    public TaskList mapTaskList(List<Percept> rawPercepts)
    {
        // Create a task list object based on the mapped list
        return new TaskList(super.mapAllPercepts(rawPercepts));
    }
}
