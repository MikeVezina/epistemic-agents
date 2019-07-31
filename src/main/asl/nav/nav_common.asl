/* Initial beliefs and rules */
direction(n).
direction(s).
direction(e).
direction(w).


isCurrentLocation(X, Y) :-
    (X == 0 & Y == 0).


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