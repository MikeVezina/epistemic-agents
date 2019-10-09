package eis.percepts.containers;

import eis.percepts.Task;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskMap extends HashMap<String, Task> {

    private static TaskMap instance;
    private Set<String> expiredTasks;
    private Set<String> nonCompletedTasks;

    private TaskMap() {
        // Private constructor
        expiredTasks = new HashSet<>();
        nonCompletedTasks = new HashSet<>();
    }

    public static TaskMap getInstance() {
        if (instance == null)
            instance = new TaskMap();

        return instance;
    }

    public boolean isTaskExpired(String taskName) {
        return expiredTasks.contains(taskName);
    }

    public boolean isTaskExpired(Task task) {
        return this.isTaskExpired(task.getName());
    }

    /**
     * @return A List of non expired and non-completed tasks.
     */
    public List<Task> getRemainingTasks() {
        return this.values().stream().filter(e -> !e.isExpired() && !e.isCompleted()).collect(Collectors.toList());
    }

    /**
     * Appends tasks that don't already exist in the map. Also updates the expired task set.
     *
     * @param mapAllPercepts The current percept mappings
     * @param currentStep    The current simulation step.
     */
    public void updateFromPercepts(List<Task> mapAllPercepts, long currentStep) {
        nonCompletedTasks.clear();

        // mapAllPercepts contains a list of non-completed tasks (which also may include expired tasks)
        mapAllPercepts.forEach(task -> {
            String taskKey = task.getName();
            put(taskKey, task);

            if (task.getDeadline() < currentStep)
            {
                task.setExpired(true);
                expiredTasks.add(taskKey);
            }

            nonCompletedTasks.add(taskKey);
        });

        // Look through previously stored tasks and see if any have been completed
        values().stream()
                .filter(t -> !nonCompletedTasks.contains(t.getName()))
                .forEach(t -> t.setCompleted(true));

    }
}
