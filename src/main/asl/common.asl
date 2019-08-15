// MASSiM Simulation Beliefs and utilities
{ include("actions.asl") }

// Initial Beliefs for things
thingType(entity).
thingType(block).
thingType(dispenser).

/***** Rules for Asserting List Properties ******/
assertListEmpty(L) :-
    .list(L) &
    L == [].

assertListHasElements(L) :-
    .list(L) &
    L \== [].

/* Finds a 'thing' percept. If the thing is an entity, do not perceive self (X = 0,Y = 0) */
hasThingPerception(X, Y, TYPE, DETAILS) :-
    thingType(TYPE) &
	percept::thing(X, Y, TYPE, DETAILS) &
	TYPE \== entity |
	(TYPE == entity & (X \== 0 | Y \== 0)).

/*** Rules for checking if block is attached ***/
hasAttached(X, Y, TYPE, DETAILS) :-
    percept::attached(X, Y) &
    hasThingPerception(X, Y, TYPE, DETAILS).

hasAttached(X, Y) :-
    percept::attached(X, Y).




closestGoal(goal(X, Y)) :-
    percept::goal(X, Y) &
    not(percept::goal(X_2, Y_2) &
    X \== X_2 & Y\== Y_2 &
    calculateDistance(DIST, X, Y) &
    calculateDistance(DIST_2, X_2, Y_2) &
    DIST > DIST_2) &
    .print("Distance: ", DIST, " - ", DIST_2).

calculateAbsolutePosition(relative(R_X, R_Y), absolute(A_X, A_Y)) :-
    percept::location(L_X, L_Y) &
    (A_X = L_X + R_X) &
    (A_Y = L_Y + R_Y).


calculateRelativePosition(relative(R_X, R_Y), absolute(A_X, A_Y)) :-
    percept::location(L_X, L_Y) &
    (R_X = A_X - L_X) &
    (R_Y = A_Y - L_Y).

calculateRelativePosition(_, _) :-
    not(percept::location(L_X, L_Y)) &
    .print("No Absolute Location Perception Exists.") &
    false.

hasBlockPerception(X, Y, BLOCK) :-
    hasThingPerception(X, Y, block, BLOCK).

hasBlockPerception(BLOCK) :-
    hasBlockPerception(_,_,BLOCK).

hasBlockAttached(X, Y, BLOCK) :-
    hasAttached(X, Y, block, BLOCK).

hasBlockAttached(BLOCK) :-
    hasAttached(_, _, block, BLOCK).

hasDispenser(X, Y, BLOCK) :-
    hasThingPerception(X, Y, dispenser, BLOCK).



