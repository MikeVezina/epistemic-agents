{ include("common.asl") }

//{ include("nav/nav_common.asl") }
//{ include("internal_actions.asl") }

/* Rules */
hasDispenserPerception(dispenser(X, Y, BLOCK)) :-
    hasThingPerception(X,Y,dispenser,BLOCK).


canDispenseBlock(BLOCK, DISPENSER) :-
    .print("canDispenseBlock: ", BLOCK, DISPENSER) &
    hasDispenserPerception(dispenser(X, Y, BLOCK)) &
    isBesideLocation(X, Y) & // Checks if agent is next to dispenser
    DISPENSER = dispenser(X, Y, BLOCK).

// Need to add check for blocks attached to other agents
canAttachBlock(X, Y, BLOCK) :-
    .print("canAttachBlock: (", X, ", ", Y, ", ", BLOCK) &
    isBesideLocation(X, Y).

// Checks if we have an attached block that needs to be rotated
// X = the requirement X location
// Y = the requirement Y location
// BLOCK =  the attached block type
isAttachedToCorrectSide(X, Y, BLOCK) :-
    hasBlockAttached(X, Y, BLOCK).

+?isAttachedToCorrectSide(X, Y, BLOCK)
    : hasBlockAttached(B_X, B_Y, BLOCK)
    <-  !performAction(rotate(cw));
        ?isAttachedToCorrectSide(X, Y, BLOCK).

// Obtain a block of type BLOCK
+!obtainBlock(BLOCK)
    <-  ?hasBlockAttached(BLOCK);
        .print("Block Attached: ", BLOCK).


// TODO: These should go into actions.asl
+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   xyToDirection(DIR, X, Y)
    <-  .print("Requesting Dispenser: (", X, ", ", Y, ", ", BLOCK, ")");
        !performAction(request(DIR)).

+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   not(xyToDirection(_, X, Y))
    <-  .print("Failed to get dispenser direction.");
        .fail.



/* Test Goal Addition Events */

// Test Goal addition for when we do not have the corresponding block attached
// IF we don't have the block attached, but we see the corresponding block, we should obtain it
// Attach the block to the closest side
+?hasBlockAttached(BLOCK)
    <-  .print("Test Goal: hasBlockAttached(",BLOCK,")");
        ?hasBlockPerception(X, Y, BLOCK); // Find a block. X and Y are unified. BLOCK should be given.
        ?canAttachBlock(X, Y, BLOCK); // Move the agent so that it can attach the block.
        ?xyToDirection(DIR, X, Y); // Get the direction of the block
        !performAction(attach(DIR)); // Attach block in current direction
        ?hasBlockAttached(BLOCK). // Re-test goal to ensure block was attached properly.

// Test goal occurs when no block can be seen
// We find a dispenser and request the specified block
+?hasBlockPerception(X, Y, BLOCK)
    <-  ?canDispenseBlock(BLOCK, DISPENSER); // Can we dispense the specified block?
        .print("Dispenser: ", DISPENSER);
        !requestBlockFromDispenser(DISPENSER); // Request a block from the dispenser
        ?hasBlockPerception(X, Y, BLOCK). // Re-test the goal to ensure all conditions are met and to unify X and Y

// Occurs when we can not attach block to ourselves (block too far, etc.)
// X, Y = Relative Location of Block
// BLOCK = Desired Block type
+?canAttachBlock(X, Y, BLOCK)
    <-  ?hasBlockPerception(X, Y, BLOCK); // Double check to see if we can see a block of type BLOCK
        .print("+?canAttachBlock: ", X, Y, BLOCK);
        ?isBesideLocation(X, Y); // Are we beside the block location so we may attach it?
        ?canAttachBlock(X, Y, BLOCK). // Re-test goal to see if we can attach it.


// Block can not be dispensed. Attempt to find a dispenser and navigate to it.
+?canDispenseBlock(BLOCK, DISPENSER)
    <-  .print("Cannot dispense block");
        ?hasDispenserPerception(dispenser(X, Y, BLOCK));
        .print("Dispenser: ", X, Y);
        ?isBesideLocation(X, Y);
        ?canDispenseBlock(BLOCK, DISPENSER).

// Test Goal Plan for when we can't find a dispenser.
+?hasDispenserPerception(dispenser(X, Y, BLOCK))
    <-  .print("No Dispenser. Searching.");
        !nav::searchForThing(dispenser, BLOCK);
        ?hasDispenserPerception(X, Y, BLOCK).

