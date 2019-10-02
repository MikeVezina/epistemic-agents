/** Internal Action Mappings**/
calculateDistance(DIST, X, Y) :-
    eis.internal.distance(DIST, X, Y).

//nextDir(DIR) :-
//	eis.internal.random_direction(DIR).

/* Rule Mappings to Internal Functions */
directionToXY(DIR, X, Y) :-
    eis.internal.direction_to_rel(DIR, X, Y).

isBesideLocation(X, Y) :-
    eis.internal.is_beside_agent(X, Y).

/* Rule Mappings to Internal Functions */
navigationPath(X, Y, DIR_LIST, RESULT) :-
    eis.internal.navigation_path(destination(X, Y), DIR_LIST, RESULT).

xyToDirection(X, Y, DIR) :-
    eis.internal.rel_to_direction(DIR, X, Y).
