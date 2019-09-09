{ include("tasks/requirements.asl") }

/****** Task Rules ********/
/**
  * This rule uses the internal action to select a task. This allows us to perform complicated logic when selecting a task.
  * Parameter: task structure that unifies the selected task NAME, DEADLINE, REWARD, REQS
 */
selectTask(task(NAME, DEADLINE, REWARD, REQS)) :-
    eis.internal.select_task(percept::task(NAME, DEADLINE, REWARD, REQS)).


// Get the current selected task (mental note)
getCurrentTask(TASK) :-
    selectedTask(TASK).

// Check to see if we have a current task and if there are any remaining requirements
taskRequirementsMet :-
    getCurrentTask(_) &
    not(remainingRequirement(_,_,_,_)).


/****** Task Plans ********/

/*** Task Submission Plan ***/
+!submitTask
    :   getCurrentTask(task(NAME, _, _, _))
    <-  !performAction(submit(NAME));
        .abolish(selectedTask(_)).

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
    :   not(selectTask(_))
    <-  .print("No Tasks to select. Waiting for a task!");
        !waitForTask(TASK).

// If there is an available task, we assign it to ourselves.
+!selectTask(TASK)
    :   selectTask(TASK).

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
