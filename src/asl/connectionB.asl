{ include("navigation") }


/* Initial beliefs and rules */
nextDir(e, s).
nextDir(s, w).
nextDir(w, n).
nextDir(n, e).
curDir(w).
curDirCount(5).

shouldRotate :- 
	curDirCount(C) &
	C == 0.
	


/* Initial goals */

/* Plans */

/* Start -> Check Tasks while avoiding Obstacles. To-do: Maintain list of absolute positions */
/* Action failures */


+!explore : shouldRotate & curDir(D) & nextDir(D, N)  <- 
	-curDir(D);
	-curDirCount(_);
	+curDir(N);
	+curDirCount(5);
	!move.
	
+!explore : curDir(D) & curDirCount(C) <-
	-curDirCount(C);
	+curDirCount(C-1);
	!move.

+!explore : curDir(DIR) & eis.direction_to_rel(DIR, X, Y) & not obtacle(X, Y) <- !move.

+!explore 	: curDir(DIR) & eis.direction_to_rel(DIR, X, Y) & obtacle(X, Y) & nextDir(DIR, NEXT) 
			<- 	-curDir(DIR);
				+curDir(NEXT)
				!move.
			
+!move : curDir(D) <-
	move(D).
	
+step(X) <- 
	!explore.

