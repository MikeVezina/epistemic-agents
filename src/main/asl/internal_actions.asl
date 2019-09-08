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

navigationDirection(DIR, X, Y) :-
    .print("PLEASE GET RID OF THIS INTERNAL ACTION CALL. IT IS DEPRECATED.") &
    eis.internal.navigation_path(destination(X, Y), [DIR | _]).

/* Rule Mappings to Internal Functions */
navigationPath(X, Y, DIR_LIST) :-
    .print("Calling navigation_path with: ", X, ", ", Y) &
    eis.internal.navigation_path(destination(X, Y), DIR_LIST).

xyToDirection(X, Y, DIR) :-
    eis.internal.rel_to_direction(DIR, X, Y).

//randomDirection(DIR) :-
//    eis.internal.random_direction(DIR).