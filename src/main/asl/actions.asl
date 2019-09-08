getLastActionResult(RES) :-
    percept::lastActionResult(RES).

getLastAction(ACTION) :-
    percept::lastAction(ACTION).

getLastActionParams(PARAMS) :-
    percept::lastActionParams(PARAMS).


didActionSucceed :-
    getLastActionResult(success) & not(getLastAction(attach)) & not(getLastAction(rotate)).

/* This is where we include action and plan failures */
+!performAction(ACTION) <-
    +lastAttemptedAction(ACTION); // Remember last action in case we need to re-attempt it
	.print("Sending action: ", ACTION);
	ACTION;
	.wait("+percept::step(_)"); // Wait for the next simulation step
	?didActionSucceed;
	-lastAttemptedAction(ACTION).

//+!doNothing
//    :   randomDirection(DIR) &
//        directionToXY(DIR, X, Y) &
//        not(lastClearLocation(X, Y))
//    <-  !performAction(clear(X,Y));
//        .abolish(lastClearLocation(_,_));
//        +lastClearLocation(X, Y).
//
//+!doNothing
//    :   randomDirection(DIR) &
//        directionToXY(DIR, X, Y) &
//        lastClearLocation(X, Y)
//    <-  !doNothing.

+?didActionSucceed
    :   getLastAction(detach) &
        getLastActionResult(FAILURE) &
        (FAILURE == failed | FAILURE == failed_target)
    <-  .print("Failed to attempt detach. Failure: ", FAILURE).

+!reattemptLastAction
    :   lastAttemptedAction(ACTION)
    <-  .print("Re-attempting last action");
        !performAction(ACTION).

+!reattemptLastAction
    :   not(lastAttemptedAction(ACTION))
    <-  .print("Error: No Action Attempted.");
        .fail.

+?didActionSucceed
    :   getLastActionResult(failed_random)
    <-  .print("The action failed randomly.");
        !reattemptLastAction.

// This action failure occurs when an entity is blocking the dispenser.
// For now we just rotate and try again (assuming it is our own attached block that is blocking the dispenser)
+?didActionSucceed
    :   getLastAction(request) &
        getLastActionResult(failed_blocked)
    <-  .print("Blocked!");
        !performAction(rotate(cw));
        !reattemptLastAction.

+?didActionSucceed
    :   getLastAction(attach) &
        getLastActionResult(success) &
        getLastActionParams([DIR])
    <-  blockAttached(DIR).

+?didActionSucceed
    :   getLastAction(rotate) &
        getLastActionResult(success) &
        getLastActionParams([DIR])
    <-  .print("Rotate success"); agentRotated(DIR).

+?didActionSucceed
    :   getLastAction(attach) &
        getLastActionResult(failed_blocked)
    <-  .print("Blocked!");
        !performAction(rotate(cw));
        !reattemptLastAction.

+?didActionSucceed
    :   getLastAction(move) &
        getLastActionResult(failed_forbidden) &
        getLastActionParams([DIR])
    <-  addForbiddenDirection(DIR).

+?didActionSucceed
    :   getLastAction(move) &
        (getLastActionResult(failed_path)).

+?didActionSucceed
    :   getLastAction(move) &
        (getLastActionResult(failed_status))
    <-  !reattemptLastAction.

-?didActionSucceed
    :   percept::lastActionResult(FAILURE)
    <-  .print("Error: The action failed with: ", FAILURE, ". This should not have occurred.").