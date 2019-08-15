{ include("common.asl") }

//{ include("nav/nav_common.asl") }
//{ include("internal_actions.asl") }

/* Rules */
hasDispenserPerception(dispenser(X, Y, BLOCK)) :-
    hasThingPerception(X,Y,dispenser,BLOCK).


canDispenseBlock(BLOCK, DISPENSER) :-
    hasDispenserPerception(DISPENSER) &
    dispenser(X, Y, BLOCK) = DISPENSER &
    xyToDirection(X, Y, DIR). // Checks if agent is next to dispenser



// Test Goal Plan for when we can't find a dispenser.
+?hasDispenserPerception(X, Y, BLOCK)
    <-  .print("No Dispenser. Searching.");
        !nav::searchForThing(dispenser, BLOCK);
        ?hasDispenserPerception(X, Y, BLOCK).

+?canDispenseBlock(BLOCK, DISPENSER)
    <-  .print("Cannot dispense block");
        ?hasDispenserPerception(dispenser(X, Y, BLOCK));
        !nav::navigateToRelativePosition(X, Y);
        ?canDispenseBlock(BLOCK, DISPENSER).

+?hasBlockPerception(BLOCK)
    <-  ?canDispenseBlock(BLOCK, DISPENSER);
        !requestBlockFromDispenser(DISPENSER).

+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   xyToDirection(DIR, X, Y)
    <-  .print("Requesting Dispenser");
        !performAction(request(DIR)).

+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   not(xyToDirection(_, X, Y))
    <-  .print("Not next to a dispenser");
        .fail.

/* Test Goal Addition Events */

// Test Goal addition for when we do not have the corresponding block attached
// IF we don't have the block attached, but we see the corresponding block, we should obtain it
// Attach the block to the closest side
+?hasBlockAttached(BLOCK)
    <-  ?hasBlockPerception(BLOCK); // Find a block
        ?canAttachBlock(BLOCK); // Move the agent so that it can attach the block
        ?isBlockAttached(BLOCK); //




// IF we don't see the corresponding block, we should obtain it from a dispenser
+?hasBlockAttached(B_X, B_Y, BLOCK)
    :   not(hasThingPerception(_,_,block,BLOCK))
    <-  ?hasDispenserPerception(D_X, D_Y, BLOCK). // Get the X and Y position of a dispenser
        // Navigate to dispenser



// Move to align with a dispenser
+!obtainBlock(BLOCK)
    <-  ?hasBlockAttached(BLOCK).