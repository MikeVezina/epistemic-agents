// Agent navigation in project massim2019
{ include("common.asl") }
{ include("internal_actions.asl") }

{ begin namespace(nav, global) }

// Include Navigation sub-modules
{ include("nav/nav_common.asl") }
{ include("nav/explore.asl") }
{ include("nav/align.asl") }




+!navigateDestination(X, Y)
    :   navigationDirection(DIR, X, Y)
    <-  !performAction(move(DIR)).


// Plans for navigating to an absolute position
+!navigateToLocation(absolute(ABS_X, ABS_Y))
    :   ABSOLUTE = absolute(ABS_X, ABS_Y) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        not(isCurrentLocation(X, Y))
    <-  !navigateDestination(X, Y);
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