{ include("common.asl") }
{ include("internal_actions.asl") }

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
	not(hasThingPerception(X,Y,entity,_)).

/** Rule for checking to see if the agent needs to be moved, and by how much **/
needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y) :-
    (MOVE_X = X - REL_X) &
    (MOVE_Y = Y - REL_Y) &
    .print("Align: (", MOVE_X, ", ", MOVE_Y, ")") &
    ((MOVE_X \== 0) | (MOVE_Y \== 0)).

isBesideLocation(X, Y) :-
    xyToDirection(DIR, X, Y).


+?isBesideLocation(X, Y)
    :   isCurrentLocation(X, Y)
    <-  .print("Current location. Not Implemented. Choose a direction based off of obstacles.");
        .fail.

-?isBesideLocation(X, Y)
    :   isCurrentLocation(X, Y)
    <-  !performAction(move(w));
        ?isBesideLocation(X, Y).

+?isBesideLocation(X, Y)
    :   not(isCurrentLocation(X, Y))
    <-  .print("Is beside location: ", X, Y);
        navigationDirection(DIR, X, Y);
        !performAction(move(DIR));
        ?isBesideLocation(X, Y).