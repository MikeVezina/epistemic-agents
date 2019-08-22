{ include("common.asl") }

hasCurrentDir(DIR)
    :- currentDir(DIR).

+?hasCurrentDir(DIR)
    :   randomDirection(D)
    <-  +currentDir(D);
        ?hasCurrentDir(DIR).

+!explore
    <-  ?hasCurrentDir(DIR);
        !performAction(move(DIR)).



//+!explore : (not(currentDir(_)) | (currentDir(DIR) & not(canMove(DIR)))) & randomDirection(D) <-
//	-currentDir(_);
//	+currentDir(D);
//	.print("Generated New Direction: ", D);
//	!explore.
//
//+!explore : currentDir(DIR) & canMove(DIR) <-
//	.print("Moving in Direction: ", DIR);
//	!performAction(move(DIR));
//	.print("Done").