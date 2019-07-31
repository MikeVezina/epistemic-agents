// Agent navigation in project massim2019
{ include("common.asl") }

{ begin namespace(nav) }

/* Initial beliefs and rules */
direction(n).
direction(s).
direction(e).
direction(w).




/******       ******/
/****** Rules ******/
/******       ******/


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


/* Finds a 'thing' percept that isn't ourself (X = 0,Y = 0) */
hasThingPerception(X, Y, TYPE, DETAILS) :-
	percept::thing(X, Y, TYPE, DETAILS) &
	(X \== 0 | Y \== 0).


/* Checks if the agent can move in the given direction. 
 * Uses the 'direction_to_rel' internal function to convert the direction {n,s,e,w} to a unit vector
 */
canMove(DIR) :- 
	directionToXY(DIR, X, Y) &
	not(percept::obstacle(X,Y)) &
	not(hasThingPerception(X,Y,entity,_)).



/******       ******/
/****** Plans ******/
/******       ******/


+!explore : (not(currentDir(_)) | (currentDir(DIR) & not(canMove(DIR)))) & randomDirection(D) <-
	-currentDir(_);
	+currentDir(D);
	.print("Generated New Direction: ", D);
	!explore.
	
+!explore : currentDir(DIR) & canMove(DIR) <-
	.print("Moving in Direction: ", DIR);
	!performAction(move(DIR));
	.print("Done").
	
+!navigateDestination(X, Y) : navigationDirection(DIR, X, Y) <-
	!performAction(move(DIR)).

+!navigateToGoal
    :   chosenGoal(ABSOLUTE) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        (X \== 0 | Y \== 0)
    <- .print("Going to goal: ", X, Y);
       !navigateDestination(X, Y);
       !navigateToGoal.

+!navigateToGoal
    :   chosenGoal(ABSOLUTE) &
        calculateRelativePosition(relative(X, Y), ABSOLUTE) &
        (X == 0 & Y == 0)
    <- .print("Arrived At goal: ", X, Y).

+!navigateToGoal
    :   not(chosenGoal(_)) &
        closestGoal(goal(X, Y)) &
        calculateAbsolutePosition(relative(X, Y), ABSOLUTE)
    <-  +chosenGoal(ABSOLUTE);
        .print("Chosen Goal: ", ABSOLUTE);
        !navigateToGoal.
	
	
+!searchForThing(TYPE, DETAILS) : thingType(TYPE) & hasThingPerception(X, Y, TYPE, DETAILS) <-
	.print("Found ", TYPE, " at (", X, ", ", Y, ")").
	
+!searchForThing(TYPE, DETAILS) : thingType(TYPE) & not(hasThingPerception(X, Y, TYPE, DETAILS)) <-
	.print("Searching for: ", TYPE, " (", DETAILS, ")");
	!explore;
	!searchForThing(TYPE, DETAILS).

+!searchForThing(TYPE) <- !searchForThing(TYPE, _).
	
{ end } /* End Navigation name space */