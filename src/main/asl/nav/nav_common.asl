{ include("common.asl") }
{ include("internal_actions.asl") }
{ include("auth/team.asl") }

/* Initial beliefs and rules */
direction(n).
direction(s).
direction(e).
direction(w).


isCurrentLocation(X, Y) :-
    (X == 0 & Y == 0).

isCurrentLocation(relative(X, Y)) :-
    isCurrentLocation(X, Y).

isCurrentLocation(absolute(X, Y)) :-
    calculateRelativePosition(REL, absolute(X, Y)) &
    isCurrentLocation(REL).


/* Checks if the agent can move in the given direction.
 */
canMove(DIR) :-
	directionToXY(DIR, X, Y) &
	not(percept::obstacle(X,Y)) &
	not(hasThingPerception(X,Y,entity,_)) &
	not(hasThingPerception(X,Y,block,_)).


+!navigateDestination(X, Y)
    :   navigationDirection(DIR, X, Y)
    <-  !performAction(move(DIR)).


// Plans for navigating to an absolute position
+!navigateToLocation(absolute(ABS_X, ABS_Y))
    :   ABSOLUTE = absolute(ABS_X, ABS_Y) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        not(isCurrentLocation(X, Y))
    <-  !navigateDestination(X, Y);
        !navigateToLocation(ABSOLUTE).

+!navigateToLocation(absolute(ABS_X, ABS_Y))
    :   ABSOLUTE = absolute(ABS_X, ABS_Y) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        isCurrentLocation(X, Y)
    <-  .print("Absolute Location Reached: ", ABSOLUTE).


// Navigate to a relative position. Convert the location to an absolute position and
// call !navigateToLocation.
+!navigateToLocation(relative(X, Y))
    :   RELATIVE = relative(X, Y) &
        not(isCurrentLocation(X, Y)) &
        calculateAbsolutePosition(RELATIVE, ABSOLUTE)
    <-  !navigateToLocation(ABSOLUTE).

+!navigateToLocation(relative(X, Y))
    :   RELATIVE = relative(X, Y) &
        isCurrentLocation(X, Y) &
        calculateAbsolutePosition(RELATIVE, ABSOLUTE)
    <-  .print("Location Reached: ", ABSOLUTE).


+!searchForThing(TYPE, DETAILS)
    :   thingType(TYPE) &
        hasThingPerception(X, Y, TYPE, DETAILS)
    <-  .print("Found ", TYPE, " at (", X, ", ", Y, ")").

+!searchForThing(TYPE, DETAILS)
    :   thingType(TYPE) &
        not(hasThingPerception(X, Y, TYPE, DETAILS))
     <- .print("Searching for: ", TYPE, " (", DETAILS, ")");
	    !explore;
	    !searchForThing(TYPE, DETAILS).

+!searchForThing(TYPE) <- !searchForThing(TYPE, _).


+!goBesideLocation(absolute(A_X, A_Y))
    :   calculateRelativePosition(relative(X, Y), absolute(A_X, A_Y)) &
        navigationDirection(DIR, X, Y)
    <-  .print("Loc: ", X, Y);
        !performAction(move(DIR));
        ?calculateRelativePosition(relative(NEW_X, NEW_Y), absolute(A_X, A_Y)); // Calculate new rel position after moving
        ?isBesideLocation(NEW_X, NEW_Y).


+?isBesideLocation(X, Y)
    :   isCurrentLocation(X, Y)
    <-  .print("Current location. Not Implemented. Choose a direction based off of obstacles.");
        .fail.

-?isBesideLocation(X, Y)
    :   isCurrentLocation(X, Y)
    <-  .print("WARN: -?isBesideLocation should be implemented with a non-blocked direction)");
        !performAction(move(w));
        ?isBesideLocation(X, Y).

+?isBesideLocation(X, Y)
    :   not(isCurrentLocation(X, Y))
    <-  .print("Is not beside location: ", X, ", ", Y);
        ?calculateAbsolutePosition(relative(X, Y), ABS);
        !goBesideLocation(ABS).


+!bringBlockToLocation(BLOCK, absolute(DEST_X, DEST_Y))
    :   hasBlockAttached(CUR_X. CUR_Y, BLOCK)
    <-


+!meetAgent(AGENT, REQ, slave)
    <-  .send(AGENT, askOne, percept::location(AGENT_X, AGENT_Y));
        !getCurrentAgentLocation(AGENT, relative(CUR_X, CUR_Y));
        .print("Rel: ", CUR_X, ", ", CUR_Y).

+!connectBlock(absolute(M_X, M_Y), BLOCK)[source(MASTER_AGENT)]
    <-


+!meetAgent(SLAVE_AGENT, req(X, Y, B), master)
    <-  ?nav::isAttachedToCorrectSide(R_X, R_Y, BLOCK);
        // Get the absolute location for the slave to go.
        .send(SLAVE_AGENT, )
