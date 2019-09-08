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
    <-  !performAction(rotate(cw));
        ?isAttachedToCorrectSide(X, Y, BLOCK).

// Obtain a block of type BLOCK
+!obtainBlock(BLOCK)
    <-  ?hasBlockAttached(BLOCK);
        ?hasBlockAttached(X, Y, BLOCK);
        ?xyToDirection(X, Y, DIR);
        blockAttached(DIR);
        .print("Block Attached: ", X, Y, BLOCK).


// TODO: These should go into actions.asl
+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   xyToDirection(X, Y, DIR)
    <-  .print("Requesting Dispenser: (", X, ", ", Y, ", ", BLOCK, ")");
        !performAction(request(DIR)).

+!requestBlockFromDispenser(dispenser(X, Y, BLOCK))
    :   not(xyToDirection(X, Y, _))
    <-  .print("Failed to get dispenser direction.");
        .fail.



/* Test Goal Addition Events */

// Test Goal addition for when we do not have the corresponding block attached
// IF we don't have the block attached, but we see the corresponding block, we should obtain it
// Attach the block to the closest side
+?hasBlockAttached(BLOCK)
    <-  .print("Block (",BLOCK,") not attached.");
        ?hasBlockPerception(BLOCK); // Find a block. X and Y are unified. BLOCK should be given.
        .print("Block (",BLOCK,") perception found.");
        ?canAttachBlock(DIR, BLOCK); // Move the agent so that it can attach the block.
        !performAction(attach(DIR)); // Attach block in current direction
        ?hasBlockAttached(BLOCK). // Re-test goal to ensure block was attached properly.

// Test goal occurs when no block can be seen
// We find a dispenser and request the specified block
+?hasBlockPerception(BLOCK)
    <-  .print("Block (",BLOCK,") not perceived.");
        !searchForThing(dispenser, BLOCK, relative(DISPENSER_X, DISPENSER_Y)); // Search for a dispenser
        ?canDispenseBlock(BLOCK, dispenser(DISPENSER_X, DISPENSER_Y, BLOCK)); // Can we dispense the specified block?
        .print("Dispenser: ", dispenser(DISPENSER_X, DISPENSER_Y, BLOCK));
        !requestBlockFromDispenser(dispenser(DISPENSER_X, DISPENSER_Y, BLOCK)); // Request a block from the dispenser
        ?hasBlockPerception(BLOCK). // Re-test the goal to ensure all conditions are met and to unify X and Y

// Occurs when we can not attach block to ourselves (block too far, etc.)
// X, Y = Relative Location of Block
// BLOCK = Desired Block type
+?canAttachBlock(DIR, BLOCK)
    <-  !searchForThing(block, BLOCK, relative(X, Y));
        .print("Found Block for Attachment: ", X, Y);
        ?canAttachBlock(DIR, BLOCK). // Re-test goal to see if we can attach it.


// Block can not be dispensed. Attempt to find a dispenser and navigate to it.
+?canDispenseBlock(BLOCK, DISPENSER)
    :   not(hasDispenserPerception(dispenser(_, _,BLOCK)))
    <-  .print("Cannot dispense block. No Dispenser Found after searching.");
        !searchForThing(dispenser, BLOCK);
        ?canDispenseBlock(BLOCK, DISPENSER).


+!moveBlock(X, Y)
    :   hasAttached(X, Y) &
        getRotation(ROT)
    <-  !performAction(rotate(ROT)).

+!moveBlock(X, Y)
    :   not(hasAttached(X, Y)) &
        xyToDirection(X, Y, DIR)
    <-  !performAction(attach(DIR));
        !moveBlock(X, Y).


+?canDispenseBlock(BLOCK, dispenser(X, Y,_))
    :   hasDispenserPerception(dispenser(_, _,BLOCK)) &
        hasBlockPerception(X, Y,_)
    <-  .print("Block is blocking Dispenser");
        !moveBlock(X, Y);
        ?canDispenseBlock(BLOCK, dispenser(X, Y, BLOCK)).


// Test Goal Plan for when we can't find a dispenser.
+?hasDispenserPerception(dispenser(X, Y, BLOCK))
    <-  !searchForThing(dispenser, BLOCK).


