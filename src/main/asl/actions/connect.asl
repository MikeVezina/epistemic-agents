/**
 * Attach Component (attach.asl)
 * This component is responsible for handling the attach command.
 * This also includes action failures.
 */

failureMessage(failed_parameter, "First parameter is not an agent of the same team OR x and y cannot be parsed to valid integers.").
failureMessage(failed_partner, "The partner's action is not connect OR failed randomly OR has wrong parameters.").
failureMessage(failed_target, "At least one of the specified blocks is not at the given position or not attached to the agent or already attached to the other agent.").
failureMessage(failed, "The given positions are too far apart OR one agent is already attached to the other (or through other blocks), or connecting both blocks would violate the size limit for connected structures.").

// Checks if a given block is attached to anything
isBlockAttached(X, Y) :- hasBlockAttached(X, Y, _).



// Returns a block that is currently free (not attached to any entity)
hasFreeBlock(X, Y, BLOCK_TYPE) :- hasBlockPerception(X, Y, BLOCK_TYPE) & not(isBlockAttached(X, Y)).

hasFreeBlockBeside(BLOCK_TYPE, DIR) :-
    hasFreeBlock(X, Y, BLOCK_TYPE) &
    xyToDirection(X, Y, DIR). // Determine if the X, Y coordinates correspond to a direction

// Attaches a block of type BLOCK_TYPE
+!attachBlock(BLOCK_TYPE)
    :   hasFreeBlockBeside(BLOCK_TYPE, DIR) // Get a block beside us that isn't attached
    <-  !attach(DIR).


/**
    Reasons for this failure to trigger:
    -   No block of type BLOCK_TYPE is beside us
    -   BLOCK_TYPE is beside us, but already attached to our self or a nearby agent
        (check attached percept)
**/
+!attachBlock(BLOCK_TYPE)
    :   not(hasFreeBlockBeside(BLOCK_TYPE, _))
    <-  .print("There was an issue attaching block type ", BLOCK_TYPE, ".");
        .fail(attachError(no_free_block)).


+!prepareForConnect(AGENT)
    <-  .send(AGENT, tell, connecting).

/** Handle Attach Actions **/
+!connect(AGENT, X, Y)
    :   connecting[source(AGENT)] &
        .my_name(NAME) &
        getAgentUsername(AGENT, USERNAME)
    <-  .print("Ready to connect with ", AGENT);
        !performAction(connect(USERNAME, X, Y));
        !connect(AGENT, X, Y).

+!connect(AGENT, X, Y)
    <-  .print("Connect succeeded or the other agent is not prepared (please call !prepareForConnect)").

//+!connect(AGENT, X, Y)
//    :   percept::step(STEP) &
//        not(attemptConnect(STEP)[source(AGENT)]) &
//        not(attemptConnect(STEP + 1)[source(AGENT)])
//    <-  .send(AGENT, tell, attemptConnect(STEP));
//        .print("Sent Connect Message: ", STEP);
//        !waitForConnectAttempt(AGENT, X, Y, STEP).

//+!waitForConnectAttempt(AGENT, X, Y, STEP)
//    :   attemptConnect(STEP)[source(AGENT)] &
//        getAgentUsername(AGENT, USERNAME)
//    <-  !performAction(connect(USERNAME, X, Y)).

// On successful attachment, we want to determine if there was more than one attachment
// that was attached. We also want to run the attach action, so that we can update our internal model
+!handleActionResult(connect, [USERNAME, X, Y], success)
    :   getAgentUsername(AGENT, USERNAME)
    <-  .print("Connect Success.");
        -connecting[source(AGENT)].

+!handleActionResult(connect, [USERNAME, X, Y], failed_random)
    :   getAgentUsername(AGENT, USERNAME)
    <-  .print("Connect Failed Randomly. Trying again.");
        !connect(AGENT, X, Y).

+!handleActionResult(connect, [USERNAME, X, Y], failed_partner)
    :   getAgentUsername(AGENT, USERNAME)
    <-  .print("Connect Failed due to partner. Trying again.");
        !connect(AGENT, X, Y).

// Attach Action failures
+!handleActionResult(connect, [USERNAME, X, Y], FAILURE)
    :   FAILURE \== success &
        failureMessage(FAILURE, MSG)
    <-  .print("Connect Failure: ", FAILURE, ". Message: ", MSG);
        .fail(failure(FAILURE)).
