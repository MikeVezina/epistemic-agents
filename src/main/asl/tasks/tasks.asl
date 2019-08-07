{ include("tasks/requirements.asl") }

/****** Task Selection Plans ********/

/**
  * This rule uses the internal action to select a task. This allows us to perform complicated logic when selecting a task.
  * Parameter: task structure that unifies the selected task NAME, DEADLINE, REWARD, REQS
 */
selectTask(task(NAME, DEADLINE, REWARD, REQS)) :-
    eis.internal.select_task(percept::task(NAME, DEADLINE, REWARD, REQS)).

/**
  * This rule uses the internal action to select a
 */
getCurrentTask(TASK) :-
    selectedTask(TASK).

taskRequirementsMet :-
    getCurrentTask(_) &
    not(remainingRequirement(_,_,_,_)).


/** Task Submission Plans **/
+!submitTask
    :   getCurrentTask(task(NAME, _, _, _))
    <-  !performAction(submit(NAME));
        .abolish(selectedTask(_)).

/****** Task Selection Plans ********/

/* Plans to wait for arrival of a new task */
+!waitForTask(TASK)
    :   selectTask(TASK).

+!waitForTask(TASK)
    :   not(selectTask(_))
    <-  .wait("+percept::task(_,_,_,_)");
        !waitForTask(TASK).

// Once we select a task, we parse the requirements.
+selectedTask(TASK)
    <-  .print("Task Selected. Parsing Requirements.");
        !parseTaskRequirements(TASK).


/* Plans to select a task */
// No Current Task and no tasks available
+!selectTask(TASK)
    :   not(getCurrentTask(_)) & not(selectTask(_))
    <-  .print("No Tasks to select. Waiting for a task!");
        !waitForTask(TASK);
        +selectedTask(TASK).

// If there is an available task, we assign it to ourselves.
+!selectTask(TASK)
    :   not(getCurrentTask(_)) &
        selectTask(TASK)
    <-  +selectedTask(TASK).

// If we have a current task that is incomplete, we do nothing.
+!selectTask(TASK)
    :   getCurrentTask(TASK) &
        (task(NAME, _, _, REQS) = TASK) &
        not(checkRequirementMet(REQS))
    <-  .print("Task ", NAME, " still in progress.").

+!selectTask(TASK)
    :   selectedTask(task(_,_,_,REQS)) &
        checkRequirementMet(REQS)
    <-  .print("Requirements are Met!").
