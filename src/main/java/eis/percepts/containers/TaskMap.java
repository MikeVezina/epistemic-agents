package eis.percepts.containers;

import eis.percepts.Task;

import java.util.*;

public class TaskMap extends HashMap<String, Task> {

    public TaskMap(List<Task> mapAllPercepts, long currentStep) {
        for(Task task : mapAllPercepts)
        {
            if(task.getDeadline() >= currentStep)
                this.put(task.getName(), task);
        }
    }
}
