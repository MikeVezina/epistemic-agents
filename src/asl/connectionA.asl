{ include("navigation", nav) }


/* Initial beliefs and rules */
curDir(w).
curDirCount(5).
entityType(entity).

	

	
nextDir(DIR) :- 
	eis.random_direction(DIR).

/* Initial goals */

/* Plans */

/* Start -> Check Tasks while avoiding Obstacles. To-do: Maintain list of absolute positions */
/* Action failures */


+!explore : curDir(DIR) & canMove(DIR) <- !move.

+!explore 	: curDir(DIR) & not(canMove(DIR)) & nextDir(NEXT)
			<- 	-curDir(DIR);
				+curDir(NEXT);
				!move.
			
	
+step(X) : nextDir(D) <- 
	.print("Direeeeeeeeeeeeeeeeee: ", D);
	!nav::move.

