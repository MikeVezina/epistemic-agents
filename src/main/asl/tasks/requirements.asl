{ include("common.asl") }
{ include("internal_actions.asl") }


remainingRequirement(X, Y, DIST, BLOCK) :-
     parsedRequirement(X, Y, DIST, BLOCK) &
     not(checkRequirementMet(X, Y, BLOCK)).

selectTwoTaskRequirements(task(NAME, _, _, [REQ_1 | [REQ_2 | _]]), REQ_1, REQ_2).

// Hard-coded for tasks with two requirements.
selectTwoRequirements(req(X, Y, BLOCK), req(X_2, Y_2, B_2)) :-
    parsedRequirement(X, Y, DIST, BLOCK) &
    parsedRequirement(X_2, Y_2, DIST_2, B_2) &
    DIST \== DIST_2.




/** This rule selects a requirement that has not been met,
  * and that has the minimal distance to the agent. This rule unifies X, Y, BLOCK with
  * the requirement such that there exists no other requirement with a smaller distance.
  */
selectRequirement(X, Y, DIST, BLOCK) :-
    remainingRequirement(X, Y, DIST, BLOCK) &
    not(remainingRequirement(_,_,DIST_O,_) &
    DIST_O < DIST).

/** Parse Each Requirement in the Task list and load them in as separate mental notes **/
+!parseTaskRequirements(task(NAME, _, _, REQS))
    <-  .abolish(parsedRequirement(_,_,_,_))
        !parseRequirements(REQS).

/** The following plans are used for parsing a list of requirements. **/
+!parseRequirements([req(X, Y, BLOCK) | T])
    :   assertListEmpty(T) &
        calculateDistance(DIST, X, Y)
    <-  +parsedRequirement(X, Y, DIST, BLOCK);
        .print("Requirements Parsed.").

+!parseRequirements([req(X, Y, BLOCK) | T])
    :   assertListHasElements(T) &
        calculateDistance(DIST, X, Y)
    <-  +parsedRequirement(X, Y, DIST, BLOCK);
        !parseRequirements(T).






/** Rules to Check if Requirements of a task have been met **/
checkRequirementMet(X, Y, BLOCK) :-
    hasBlockAttached(X, Y, BLOCK).


/*** Task Requirement Selection Plans***/
+!selectRequirements(REQ)
    :   not(parsedRequirement(_,_,_,_))
    <-  .print("Requirements have not been parsed.");
        .fail.

+!selectRequirements(req(X, Y, BLOCK))
    :   parsedRequirement(_,_,_,_) &
        not(remainingRequirement(_,_,_,_))
    <-  .print("Task Requirements are Met.").

+!selectRequirements(req(X, Y, BLOCK))
    :   parsedRequirement(_,_,_,_) &
        remainingRequirement(_,_,_,_)
    <-  ?selectRequirement(X, Y, _, BLOCK);
        .print("Selecting Requirement: (", X, ", ", Y, ")").



+!selectRequirements(req(X, Y, BLOCK))
    :   selectRequirement(X, Y, BLOCK)
    <-  .print("Requirements have been parsed: ", X, " - ", Y, " - ", BLOCK).

-!selectRequirements(TASK, REQ)[error(no_applicable), error_msg(MSG)]
    <-  .print("No Applicable plan. Error: ", MSG);
        .fail.