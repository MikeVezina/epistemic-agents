{ include("common.asl") }

//{ include("nav/nav_common.asl") }
//{ include("internal_actions.asl") }

/* Rules */
hasDispenserPerception(dispenser(X, Y, BLOCK)) :-
    hasThingPerception(X,Y,dispenser,BLOCK).


canDispenseBlock(BLOCK, DISPENSER) :-
    .print("canDispenseBlock: ", BLOCK, ", Dispenser: ", DISPENSER) &
    hasDispenserPerception(dispenser(X, Y, BLOCK)) &
    not(hasBlockPerception(X, Y, _)) &
    isBesideLocation(X, Y) & // Checks if agent is next to dispenser
    DISPENSER = dispenser(X, Y, BLOCK).

// Need to add check for blocks attached to other agents
canAttachBlock(DIR, BLOCK) :-
    .print("canAttachBlock: (", BLOCK, ")") &
    hasBlockPerception(X, Y, BLOCK) &
    xyToDirection(X, Y, DIR).

// Checks if we have an attached block that needs to be rotated
// X = the requirement X location
// Y = the requirement Y location
// BLOCK =  the attached block type
isAttachedToCorrectSide(X, Y, BLOCK) :-
    hasBlockAttached(X, Y, BLOCK).


/* Plans / Goals */

+?isAttachedToCorrectSide(X, Y, BLOCK)
    : hasBlockAttached(B_X, B_Y, BLOCK)
    <-  !rotate(cw);
        ?isAttachedToCorrectSide(X, Y, BLOCK).

// Obtain a block of type BLOCK
+!obtainBlock(Block)
    <-  ?hasBlockAttached(Block); // Tests if the agent has a block attached
        ?hasBlockAttached(X, Y, Block); // Used to unify the block X and Y
        ?xyToDirection(X, Y, Dir); // Ensure the attached block is a valid {NSEW} direction
        .print("Block Attached: ", X, Y, Block).

// TODO: These should go into actions.asl
+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   xyToDirection(X, Y, DIR)
    <-  .print("Requesting Dispenser: (", X, ", ", Y, ", ", BLOCK, ")");
        !request(DIR).

+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   not(xyToDirection(X, Y, _))
    <-  .print("Failed to get dispenser direction.");
        .fail.



/* Test Goal Addition Events */

// Test Goal addition for when we do NOT have the corresponding block attached
// 1. We need to dispense the block from a dispenser and attach it
// 2. We then re-test the goal to ensure the block was attached
+?hasBlockAttached(Block)
    <-  .print("Block (",Block,") not attached.");
        !dispenseAndAttachBlock(Block); // Get a block from the dispenser
        ?hasBlockAttached(Block). // Re-test goal to ensure block was attached properly.

// Test goal occurs when no block can be seen
// We find a dispenser and request the specified block
+!dispenseAndAttachBlock(BLOCK)
    <-  .print("Dispensing and Attaching Block (",BLOCK,").");
        !searchForThing(dispenser, BLOCK, relative(DISPENSER_X, DISPENSER_Y)); // Search for a dispenser
        ?canDispenseBlock(BLOCK, dispenser(DISPENSER_X, DISPENSER_Y, BLOCK)); // Can we dispense the specified block?
        .print("Dispenser: ", dispenser(DISPENSER_X, DISPENSER_Y, BLOCK));
        !requestBlockFromDispenser(dispenser(DISPENSER_X, DISPENSER_Y, BLOCK)); // Request a block from the dispenser
        ?hasBlockAttached(BLOCK). // Re-test the goal to ensure all conditions are met and to unify X and Y




// Block can not be dispensed. Attempt to find a dispenser and navigate to it.
+?canDispenseBlock(BLOCK, DISPENSER)
    :   not(hasDispenserPerception(dispenser(_, _,BLOCK)))
    <-  .print("Cannot dispense block. No Dispenser Found after searching.");
        !searchForThing(dispenser, BLOCK);
        ?canDispenseBlock(BLOCK, DISPENSER).

+?canDispenseBlock(BLOCK, dispenser(X, Y,_))
    :   hasDispenserPerception(dispenser(_, _,BLOCK)) &
        hasBlockPerception(X, Y,_)
    <-  .print("Block is blocking Dispenser");
        !moveBlock(X, Y);
        ?canDispenseBlock(BLOCK, dispenser(X, Y, BLOCK)).

+!moveBlock(X, Y)
    :   hasBlockAttached(X, Y, BLOCK) &
        getRotation(ROT)
    <-  !rotate(ROT);
        !detachMovedBlock(BLOCK).

+!detachMovedBlock(BLOCK)
    :   hasBlockAttached(X, Y, BLOCK) &
        xyToDirection(X, Y, DIR)
    <-  !detach(DIR).

+!moveBlock(X, Y)
    :   not(hasBlockAttached(X, Y, _)) &
        xyToDirection(X, Y, DIR)
    <-  !attach(DIR);
        !moveBlock(X, Y).



// Occurs when we can not attach block to ourselves (block too far, etc.)
// X, Y = Relative Location of Block
// BLOCK = Desired Block type
//+?canAttachBlock(DIR, BLOCK)
//    <-  !searchForThing(block, BLOCK, relative(X, Y));
//        .print("Found Block for Attachment: ", X, Y);
//        ?canAttachBlock(DIR, BLOCK). // Re-test goal to see if we can attach it.

//// Test Goal Plan for when we can't find a dispenser.
//+?hasDispenserPerception(dispenser(X, Y, BLOCK))
//    <-  !searchForThing(dispenser, BLOCK).


