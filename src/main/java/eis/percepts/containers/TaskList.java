package eis.percepts.containers;

import eis.percepts.Task;

import java.util.*;

public class TaskList extends ArrayList<Task> {
    private Map<String, Task> taskMap;

    public TaskList(List<Task> mapAllPercepts) {
        this.taskMap = new HashMap<>();
        this.addAll(mapAllPercepts);
    }

    @Override
    public boolean add(Task t)
    {
        taskMap.put(t.getName(), t);
        return super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends Task> c) {
        for(Task t : c)
            this.add(t);

        return c.size() > 0;
    }

    public Map<String, Task> getTaskMap()
    {
        return taskMap;
    }
}
