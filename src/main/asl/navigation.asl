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
	eis.direction_to_rel(DIR, X, Y).
	
xyToDirection(DIR, X, Y) :-
	eis.rel_to_direction(DIR, X, Y).
	
randomDirection(DIR) :- 
	eis.random_direction(DIR).

	
/* Finds a 'thing' percept that isn't ourself (X = 0,Y = 0) */
hasThingPerception(X, Y, TYPE, DETAILS) :-
	default::thing(X, Y, TYPE, DETAILS) &
	(X \== 0 | Y \== 0).


/* Checks if the agent can move in the given direction. 
 * Uses the 'direction_to_rel' internal function to convert the direction {n,s,e,w} to a unit vector
 */
canMove(DIR) :- 
	directionToXY(DIR, X, Y) &
	not(obstacle(X,Y)) &
	not(thing(X,Y,entity,_)).



/******       ******/
/****** Plans ******/
/******       ******/


+!explore : randomDirection(D) <-
	.print("Moving in Direction: ", D);
	!performAction(move(D)).
	
+!navigateDestination(X, Y) : xyToDirection(DIR, X, Y) <-
	!performAction(move(DIR)).
	
	
+!searchForThing(TYPE) : thingType(TYPE) & hasThingPerception(X, Y, TYPE, _) <-
	.print("I found a thing at: ", TYPE, X, Y).
	
+!searchForThing(TYPE) : thingType(TYPE) & not(hasThingPerception(X, Y, TYPE, _)) <-
	.print("Searching for thing: ", TYPE);
	!explore;
	!searchForThing(TYPE).
	
	
{ end } /* End Navigation name space */