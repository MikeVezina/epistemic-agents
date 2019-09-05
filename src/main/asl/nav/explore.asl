{ include("common.asl") }
{ include("actions.asl") }

hasCurrentDir(DIR)
    :- currentDir(DIR).

+?hasCurrentDir(DIR)
    :   randomDirection(D)
    <-  +currentDir(D);
        ?hasCurrentDir(DIR).

+!explore
    : getLastActionResult(success)
    <-  ?hasCurrentDir(DIR);
        !performAction(move(DIR));
        !explore.
+!explore
    :   getLastActionResult(failed_path) |
        getLastActionResult(failed_forbidden)
    <-  -currentDir(_);
        ?hasCurrentDir(DIR);
        !performAction(move(DIR));
        !explore.

+!explore
    : not(getLastActionResult(failed_path)) &
        not(getLastActionResult(success)) &
        not(getLastActionResult(failed_forbidden))
    <-  -currentDir(_);
        ?hasCurrentDir(DIR);
        !performAction(move(DIR));
        !explore.




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