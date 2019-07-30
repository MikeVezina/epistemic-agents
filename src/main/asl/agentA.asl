{ include("common.asl") }
{ include("navigation.asl", nav) }

	
/* MVP:
 * Iteration 0: No obstacles, deadlines are irrelevant, only one agent on the map, all important locations are hard-coded (dispenser, goals)


 * NOTE: Navigation module can be stubbed out to hard-code positions for everything
 * NOTE: These steps require the agent to remember it's location. The simulator does not provide location perception.

 * Steps:

 * 1. If Task Exists -> Parse Task for requirements
 * 		        Else -> Navigation: Survey Surroundings -> Repeat Step 1.


 * 2. For Each Required Block:
 *      If we have belief of block dispenser location -> Navigation: GoTo Belief Location
 *                                              Else  -> Navigation: Search For Required Block Dispenser
 * 		(Request) Block
 * 		(Attach) Requirement to Agent

 * 3. If we have a belief of the goal location -> Navigation: GoTo Goal Location
 *                                        else -> Navigation: Search for Goal Location

 *
 * 
 */

/***** Rules ******/
selectTask(task(NAME, DEADLINE, REWARD, REQS)) :-
    percept::task(NAME, DEADLINE, REWARD, REQS).

assertListEmpty(L) :-
    .list(L) &
    L == [].

assertListHasElements(L) :-
    .list(L) &
    L \== [].

/* Filter Out Task Requirements to Find the First One */
selectRequirement(req(X, Y, B), [req(X, Y, B) | T]) :-
    assertListEmpty(T) &
    not(hasBlockAttached(X, Y, B)).

selectRequirement(req(X, Y, B), [req(X, Y, B) | T]) :-
    assertListHasElements(T) &
    not(hasBlockAttached(X, Y, B)) &
    selectRequirement(req(X_O, Y_O, B_O), T) &
    eis.internal.distance(DIST, X, Y) &
    eis.internal.distance(DIST_O, X_O, Y_O) &
    DIST <= DIST_O.

selectRequirement(req(X_O, Y_O, B_O), [req(X, Y, B) | T]) :-
    assertListHasElements(T) &
    not(hasBlockAttached(X, Y, B)) &
    selectRequirement(req(X_O, Y_O, B_O), T) &
    eis.internal.distance(DIST, X, Y) &
    eis.internal.distance(DIST_O, X_O, Y_O) &
    DIST > DIST_O.


/** Rule for checking to see if the agent needs to be moved, and by how much **/
needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y) :-
    (MOVE_X = X - REL_X) &
    (MOVE_Y = Y - REL_Y) &
    .print("Align: (", MOVE_X, ", ", MOVE_Y, ")") &
    ((MOVE_X \== 0) | (MOVE_Y \== 0)).


/** Rules to Check if Requirements have been met **/
checkRequirementMet([req(X, Y, BLOCK) | T]) :-
    assertListHasElements(T) &
    hasBlockAttached(X, Y, BLOCK) &
    checkRequirementMet(T).

checkRequirementMet([req(X, Y, BLOCK) | T]) :-
    assertListEmpty(T) &
    hasAttached(X, Y).



/***** Initial Goals ******/
!getPoints.





/***** Plan Definitions ******/
// TODO: Action failures

// Move to align with dispenser
+!alignDispenser(BLOCK, REL_X, REL_Y)
    :   hasDispenser(X, Y, BLOCK) &
        needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y) &
        nav::navigationDirection(DIR, MOVE_X, MOVE_Y)
    <-  !performAction(move(DIR));
        !alignDispenser(BLOCK, REL_X, REL_Y).

// No alignment needed
+!alignDispenser(BLOCK, REL_X, REL_Y)
    :   hasDispenser(X, Y, BLOCK) &
        not(needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y)).



+!attachBlock(X, Y)
    :   hasDispenser(X, Y, _) &
        nav::xyToDirection(DIR, X, Y)
    <-  !performAction(request(DIR));
        !performAction(attach(DIR)).


/****** Task Selection Plans ********/
+!selectTask(TASK)
    :   not(selectedTask(_)) & not(selectTask(_))
    <-  !selectTask(TASK).

+!selectTask(TASK)
    :   not(selectedTask(_)) & selectTask(TASK)
    <-  +selectedTask(TASK).

+!selectTask(TASK)
    :   selectedTask(TASK).


/*** Task Requirement Selection Plans***/
+!selectRequirements(task(_, _, _, REQS), REQ)
    :   selectRequirement(REQ, REQS).

-!selectRequirements(TASK, REQ)
    <-  .print("Failed to select task requirements.").



// Main Plan
+!getPoints
    <-  !selectTask(TASK);
        !selectRequirements(TASK, req(R_X, R_Y, BLOCK));
        .print("Current Task: ", TASK, ", Requirement: ", REQ);
        !nav::searchForThing(dispenser, BLOCK);
        !alignDispenser(BLOCK, R_X, R_Y);
        !attachBlock(R_X, R_Y);
        !nav::navigateToGoal.



