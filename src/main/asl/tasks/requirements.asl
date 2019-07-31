{ include("common.asl") }
{ include("internal_actions.asl") }

/* Filter Out Task Requirements to Find the First One */
selectRequirement(req(X, Y, B), [req(X, Y, B) | T]) :-
    assertListEmpty(T) &
    not(hasBlockAttached(X, Y, B)).

selectRequirement(req(X, Y, B), [req(X, Y, B) | T]) :-
    assertListHasElements(T) &
    not(hasBlockAttached(X, Y, B)) &
    selectRequirement(req(X_O, Y_O, B_O), T) &
    calculateDistance(DIST, X, Y) &
    calculateDistance(DIST_O, X_O, Y_O) &
    DIST <= DIST_O.

selectRequirement(req(X_O, Y_O, B_O), [req(X, Y, B) | T]) :-
    assertListHasElements(T) &
    not(hasBlockAttached(X, Y, B)) &
    selectRequirement(req(X_O, Y_O, B_O), T) &
    calculateDistance(DIST, X, Y) &
    calculateDistance(DIST_O, X_O, Y_O) &
    DIST > DIST_O.


/** Rules to Check if Requirements of a task have been met **/
checkRequirementMet([req(X, Y, BLOCK) | T]) :-
    assertListHasElements(T) &
    hasBlockAttached(X, Y, BLOCK) &
    checkRequirementMet(T).

checkRequirementMet([req(X, Y, BLOCK) | T]) :-
    assertListEmpty(T) &
    hasAttached(X, Y).