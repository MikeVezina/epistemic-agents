// Agent navigation in project massim2019
{ begin namespace(nav) }

/* Initial beliefs and rules */
direction(n).
direction(s).
direction(e).
direction(w).

canMove(DIR) :- 
	eis.direction_to_rel(DIR, X, Y) &
	not(obstacle(X,Y)) &
	not(thing(X,Y,TYPE,_)) &
	entityType(ENT_TYPE) &
	not(TYPE == ENT_TYPE).


randomDirection(DIR) :- 
	eis.random_direction(DIR).


+!move : nextDir(D) <-
	move(D).
	
{ end } /* End Navigation name space */