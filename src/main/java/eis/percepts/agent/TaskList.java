package eis.percepts.agent;

import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.handlers.TaskHandler;

import java.util.*;
import java.util.stream.Collectors;

public final class TaskList {
    private static TaskList singleTaskList;
    private List<Task> taskList;
    private long lastUpdateStep;
    private TaskHandler taskParser;
    private List<Percept> taskListPercepts;

    private TaskList() {
        lastUpdateStep = -1;
        taskList = Collections.synchronizedList(new ArrayList<>());
        taskListPercepts = Collections.synchronizedList(new ArrayList<>());
        taskParser = new TaskHandler();
    }

    private boolean shouldUpdate(long currentStep) {
        return currentStep > lastUpdateStep;
    }

    public synchronized void updateTaskList(long currentStep, List<Percept> percepts) {
        if (!shouldUpdate(currentStep))
            return;

        taskParser.prepareStep(currentStep);
        percepts.forEach(taskParser::handlePercept);
        taskParser.perceptProcessingFinished();

        this.taskListPercepts = taskParser.getTaskListPercepts();
        this.taskList = taskParser.getTaskList();
    }

    public synchronized List<Task> getTaskList() {
        return this.taskList;
    }

    public synchronized Map<String, Task> getTaskMap() {
        return taskList.stream().collect(Collectors.toMap(Task::getName, task -> task));
    }

    public synchronized List<Percept> getTaskListPercepts() {
        return this.taskListPercepts;
    }

    public long getLastUpdateStep() {
        return lastUpdateStep;
    }

    public static TaskList getInstance() {
        if (singleTaskList == null)
            singleTaskList = new TaskList();
        return singleTaskList;
    }
}
