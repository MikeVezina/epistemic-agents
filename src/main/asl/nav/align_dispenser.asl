// Move to align with a dispenser
+!alignDispenser(BLOCK, REL_X, REL_Y)
    :   hasDispenser(X, Y, BLOCK) &
        needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y) &
        nav::navigationDirection(DIR, MOVE_X, MOVE_Y)
    <-  !performAction(move(DIR));
        !alignDispenser(BLOCK, REL_X, REL_Y).

// No alignment needed
+!alignDispenser(BLOCK, REL_X, REL_Y)
    :   hasDispenser(X, Y, BLOCK) &
        not(needsAlignment(MOVE_X, MOVE_Y, X, Y, REL_X, REL_Y)).

+!alignDispenser(BLOCK, _, _)
    :   not(hasDispenser(X, Y, BLOCK))
    <-  .print("No Dispenser to align for block: ",  BLOCK);
        .fail.