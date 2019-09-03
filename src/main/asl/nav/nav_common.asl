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


shouldNavigateAgain :-
    not(percept::lastActionResult(failed_path)).

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
        ?shouldNavigateAgain;
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




/** Search for Thing Perception **/

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



/** Beside Location **/

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







//
//
//+?isBlockAtLocation(BLOCK, relative(DEST_X, DEST_Y))
//    :   calculateAbsolutePosition(relative(DEST_X, DEST_Y), absolute(A_X, A_Y)) // Get the relative position of the block destination
//    <- ?isBlockAtLocation(BLOCK, absolute(A_X, A_Y)).
//
//+?canNavigateBlock(CUR_X, CUR_Y, DIR)
//    <-  !performAction(rotate(cw)).
//
//+?isBlockAtLocation(BLOCK, absolute(DEST_X, DEST_Y))
//    :   calculateRelativePosition(relative(R_X, R_Y), absolute(DEST_X, DEST_Y)) & // Get the relative position of the block destination
//        hasBlockAttached(CUR_X, CUR_Y, BLOCK) & // Get the location of the attached block
//        relative(BD_X, BD_Y) = relative(R_X - CUR_X, R_Y - CUR_Y) & // Relative block displacement
//        navigationDirection(DIR, BD_X, BD_Y)
//    <-  .print("Is block at ", DEST_X, ", ", DEST_Y);
//        ?canNavigateBlock(CUR_X, CUR_Y, DIR);
//        !performAction(move(DIR));
//        ?isBlockAtLocation(BLOCK, absolute(DEST_X, DEST_Y)).
//
//
//+!meetAgent(AGENT, req(R_X, R_Y, BLOCK), slave)
//    <-  ?getTeamAgentLocation(AGENT, relative(CUR_X, CUR_Y));
//        .print("Master At: ", CUR_X, ", ", CUR_Y);
//        ?isBlockAtLocation(BLOCK, relative(CUR_X + R_X, CUR_Y + R_Y)).
////        .send(SLAVE_AGENT, achieve, connectBlock(absolute(TARGET_X, TARGET_Y), BLOCK));
//
//+!meetAgent([SLAVE_AGENT, req(OTHER_X, OTHER_Y, BLOCK)], req(X, Y, B), master)
//    <-  ?getTeamAgentLocation(AGENT, relative(CUR_X, CUR_Y));
//        .print("Slave At: ", CUR_X, ", ", CUR_Y);
//        ?nav::isAttachedToCorrectSide(R_X, R_Y, BLOCK);
//        !doNothing;
//        !meetAgent([SLAVE_AGENT, req(OTHER_X, OTHER_Y, BLOCK)], req(X, Y, B), master).
//
//
//+!connectBlock(req(X, Y, BLOCK))[source(MASTER_AGENT)]
//    :   hasBlockAttached(BLOCK)
//    <-  ?getTeamAgentLocation(MASTER_AGENT, relative(CUR_X, CUR_Y));
//        ?isBlockAtLocation(BLOCK, relative(CUR_X + X, CUR_Y + Y)).