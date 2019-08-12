//{ include("nav/nav_common.asl") }
//{ include("internal_actions.asl") }

hasDispenser(X, Y, BLOCK) :-
    hasThingPerception(X,Y,dispenser,BLOCK).


// Test Goal addition for when we do not have the corresponding block attached
// IF we don't have the block attached, but we see the corresponding block, we should obtain it
+?hasBlockAttached(B_X, B_Y, BLOCK)
    :   hasThingPerception(B_X,B_Y,block,BLOCK)
    <-  !nav::navigateToRelativePosition(relative(B_X, B_Y));
        .fail.


// IF we don't see the corresponding block, we should obtain it from a dispenser
+?hasBlockAttached(BLOCK)
    :   not(hasThingPerception(_,_,block,BLOCK))
    <-  ?hasDispenser(BLOCK).
        // Navigate to dispenser

// Test Goal Plan for when we can't find a dispenser.
+?hasDispenser(X, Y, BLOCK)
    <-  .print("No Dispenser. Searching.");
        !nav::searchForThing(dispenser, BLOCK);
        ?hasDispenser(X, Y, BLOCK).


// Move to align with a dispenser
+!obtainBlock(BLOCK, REL_X, REL_Y)
    <-  ?hasBlockAttached(REL_X, REL_Y, BLOCK);
        ?canRequestBlock(BLOCK).

// No alignment needed
+!alignDispenser(BLOCK, REL_X, REL_Y)
    :   hasDispenser(X, Y, BLOCK) &
        not(needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y)).

//+!alignDispenser(BLOCK, _, _)
//    :   not(hasDispenser(X, Y, BLOCK))
//    <-  .print("No Dispenser to align for block: ",  BLOCK);
//        .fail.