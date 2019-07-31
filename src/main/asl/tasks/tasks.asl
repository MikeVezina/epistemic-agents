{ include("tasks/requirements.asl") }

/****** Task Selection Plans ********/
selectTask(task(NAME, DEADLINE, REWARD, REQS)) :-
    percept::task(NAME, DEADLINE, REWARD, REQS).

/****** Task Selection Plans ********/
+!selectTask(TASK)
    :   not(selectedTask(_)) & not(selectTask(_))
    <-  !selectTask(TASK).

+!selectTask(TASK)
    :   not(selectedTask(_)) & selectTask(TASK)
    <-  +selectedTask(TASK).

+!selectTask(TASK)
    :   selectedTask(task(_,_,_,REQS)) &
        not(checkRequirementMet(REQS))
    <-  .print("Requirements not Met!").

+!selectTask(TASK)
    :   selectedTask(task(_,_,_,REQS))
    <-  .print("Requirements are Met!").