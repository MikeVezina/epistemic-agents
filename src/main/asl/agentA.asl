{ include("common.asl") }
{ include("navigation.asl", nav) }


/* Initial beliefs and rules */
curDir(w).
curDirCount(5).

	
nextDir(DIR) :- 
	eis.random_direction(DIR).

/* Initial goals */

/* Plans */

/* Start -> Check Tasks while avoiding Obstacles. To-do: Maintain list of absolute positions */
/* Action failures */

!nav::searchForThing(entity).