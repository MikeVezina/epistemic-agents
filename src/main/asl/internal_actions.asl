/** Internal Action Mappings**/
calculateDistance(DIST, X, Y) :-
    eis.internal.distance(DIST, X, Y).

nextDir(DIR) :-
	eis.internal.random_direction(DIR).

/* Rule Mappings to Internal Functions */
directionToXY(DIR, X, Y) :-
    eis.internal.direction_to_rel(DIR, X, Y).

/* Rule Mappings to Internal Functions */
navigationDirection(DIR, X, Y) :-
    .print("Calling navigation_path with: ", X, ", ", Y) &
    eis.internal.navigation_path(destination(X, Y), DIR).

xyToDirection(DIR, X, Y) :-
    eis.internal.rel_to_direction(DIR, X, Y).

randomDirection(DIR) :-
    eis.internal.random_direction(DIR).