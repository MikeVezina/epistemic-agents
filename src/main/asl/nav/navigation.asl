// Agent navigation in project massim2019
{ include("common.asl") }
{ include("internal_actions.asl") }

{ begin namespace(nav) }

// Include Navigation sub-modules
{ include("nav/nav_common.asl") }
{ include("nav/explore.asl") }
{ include("nav/align_dispenser.asl") }


+!navigateDestination(X, Y) : navigationDirection(DIR, X, Y) <-
	!performAction(move(DIR)).

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
    <-  -chosenGoal(_);
        .print("Arrived At goal: ", X, Y).

+!navigateToGoal
    :   not(chosenGoal(_)) &
        closestGoal(goal(X, Y)) &
        calculateAbsolutePosition(relative(X, Y), ABSOLUTE) &
        not(isCurrentLocation(X, Y))
    <-  +chosenGoal(ABSOLUTE);
        .print("Chosen Goal: ", ABSOLUTE);
        !navigateToGoal.

	
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
	
{ end } /* End Navigation name space */