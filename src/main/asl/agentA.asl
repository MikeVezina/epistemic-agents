{ include("common.asl") }
{ include("navigation.asl", nav) }


/* Initial beliefs and rules */
curDir(w).
curDirCount(5).

	
nextDir(DIR) :- 
	eis.internal.random_direction(DIR).

/* Initial goals */

/* Plans */

/* Start -> Check Tasks while avoiding Obstacles. To-do: Maintain list of absolute positions */
/* Action failures */

	
//+step(X) : currentDestination(DEST) <-
	//.print("Hello World!2");
	//eis.internal.navigation_path(DEST, DIR);
	//move(DIR).
	
	

!nav::searchForThing(dispenser).