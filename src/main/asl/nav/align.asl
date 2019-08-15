{ include("common.asl") }

//{ include("nav/nav_common.asl") }
//{ include("internal_actions.asl") }

/* Rules */
hasDispenserPerception(dispenser(X, Y, BLOCK)) :-
    hasThingPerception(X,Y,dispenser,BLOCK).


canDispenseBlock(BLOCK, DISPENSER) :-
    hasDispenserPerception(DISPENSER) &
    dispenser(X, Y, BLOCK) = DISPENSER &
    xyToDirection(DIR, X, Y). // Checks if agent is next to dispenser

// Need to add check for blocks attached to other agents
canAttachBlock(X, Y, BLOCK) :-
    isBesideLocation(X, Y).



// Move to align with a dispenser
+!obtainBlock(BLOCK)
    <-  ?hasBlockAttached(BLOCK).

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
    <-  ?hasBlockPerception(X, Y, BLOCK); // Find a block
        ?canAttachBlock(X, Y, BLOCK); // Move the agent so that it can attach the block
        // Attach block here
        ?isBlockAttached(BLOCK). //

+?hasBlockPerception(X, Y, BLOCK)
    <-  ?canDispenseBlock(BLOCK, DISPENSER);
        !requestBlockFromDispenser(DISPENSER).

+?canAttachBlock(X, Y, BLOCK)
    <-  ?hasBlockPerception(X, Y, BLOCK);
        ?isBesideLocation(X, Y);
        ?canAttachBlock(X, Y, BLOCK).

+?canDispenseBlock(BLOCK, DISPENSER)
    <-  .print("Cannot dispense block");
        ?hasDispenserPerception(dispenser(X, Y, BLOCK));
        ?isBesideLocation(X, Y);
        ?canDispenseBlock(BLOCK, DISPENSER).

// Test Goal Plan for when we can't find a dispenser.
+?hasDispenserPerception(dispenser(X, Y, BLOCK))
    <-  .print("No Dispenser. Searching.");
        !nav::searchForThing(dispenser, BLOCK);
        ?hasDispenserPerception(X, Y, BLOCK).








