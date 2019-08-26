getLastActionResult(RES) :-
    percept::lastActionResult(RES).

getLastAction(ACTION) :-
    percept::lastAction(ACTION).

didActionSucceed :-
    getLastActionResult(success).


/* This is where we include action and plan failures */
+!performAction(ACTION) <-
    +lastAttemptedAction(ACTION); // Remember last action in case we need to re-attempt it
	.print("Sending action: ", ACTION);
	ACTION;
	.wait("+percept::step(_)"); // Wait for the next simulation step
	?didActionSucceed;
	-lastAttemptedAction(ACTION).

+!doNothing
    <-  performAction(clear(0,0)).

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
    :   getLastAction(move) &
        getLastActionResult(failed_path).

-?didActionSucceed
    :   percept::lastActionResult(FAILURE)
    <-  .print("Error: The action failed with: ", FAILURE, ". This should not have occurred.");
        .fail.