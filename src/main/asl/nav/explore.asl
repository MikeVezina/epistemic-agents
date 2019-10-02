{ include("common.asl") }

hasCurrentDir(DIR)
    :- currentDir(DIR).

exploreDirection(DIR) :-
    eis.internal.explore_direction(DIR).

+?hasCurrentDir(DIR)
    :   randomDirection(D)
    <-  +currentDir(D);
        ?hasCurrentDir(DIR).

+?exploreDirection(DIR)
    <- .fail.


+!explore
    <-  ?exploreDirection(DIR);
        !move(DIR).


+!exploreForever
    <-  !explore;
        .print("Exploring Forever");
        !exploreForever.
//+!explore
//    :   getLastActionResult(failed_path) |
//        getLastActionResult(failed_forbidden)
//    <-  ?exploreDirection(DIR);
//        !performAction(move(DIR));
//        !explore.
//
//+!explore
//    : not(getLastActionResult(failed_path)) &
//        not(getLastActionResult(success)) &
//        not(getLastActionResult(failed_forbidden))
//    <-  ?exploreDirection(DIR);
//        !performAction(move(DIR));
//        !explore.




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