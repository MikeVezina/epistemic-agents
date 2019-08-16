{ include("nav/nav_common.asl") } // Also includes common.asl via transitivity

isAtGoal :-
    hasGoalPerception(X, Y) &
    isCurrentLocation(X, Y).



closestGoal(goal(X, Y)) :-
    percept::goal(X, Y) &
    not(percept::goal(X_2, Y_2) &
    X \== X_2 & Y\== Y_2 &
    calculateDistance(DIST, X, Y) &
    calculateDistance(DIST_2, X_2, Y_2) &
    DIST > DIST_2 &
    .print("Distance: ", DIST, " - ", DIST_2)).

+!navigateToGoal
    :   chosenGoal(ABSOLUTE) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        not(isCurrentLocation(X, Y))
    <- .print("Going to goal: ", X, Y);
       !navigateDestination(X, Y);
       !navigateToGoal.

+!navigateToGoal
    :   chosenGoal(ABSOLUTE) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        isCurrentLocation(X, Y)
    <-  -chosenGoal(ABSOLUTE);
        .print("Arrived At goal: ", X, Y).

+!navigateToGoal
    :   not(chosenGoal(_)) &
        closestGoal(goal(X, Y)) &
        calculateAbsolutePosition(relative(X, Y), ABSOLUTE) &
        not(isCurrentLocation(X, Y))
    <-  +chosenGoal(ABSOLUTE);
        .print("Chosen Goal: ", ABSOLUTE);
        !navigateToGoal.