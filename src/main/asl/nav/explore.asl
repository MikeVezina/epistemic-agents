+!explore : (not(currentDir(_)) | (currentDir(DIR) & not(canMove(DIR)))) & randomDirection(D) <-
	-currentDir(_);
	+currentDir(D);
	.print("Generated New Direction: ", D);
	!explore.

+!explore : currentDir(DIR) & canMove(DIR) <-
	.print("Moving in Direction: ", DIR);
	!performAction(move(DIR));
	.print("Done").