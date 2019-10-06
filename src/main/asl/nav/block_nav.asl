// Checks if the relative location of the block is at the correct spot
isBlockAtLocation(BLOCK, absolute(DEST_X, DEST_Y))
    :-  calculateRelativePosition(REL, absolute(DEST_X, DEST_Y)) &
        isBlockAtLocation(BLOCK, REL).

isBlockAtLocation(BLOCK, relative(DEST_X, DEST_Y))
    :-  hasBlockAttached(B_X, B_Y, BLOCK) &
        isCurrentLocation(DEST_X - B_X, DEST_Y - B_Y).

canNavigateBlock(B_X, B_Y, DIR) :-
    directionToXY(DIR, DIR_X, DIR_Y) &
    NEXT_X = B_X + DIR_X &
    NEXT_Y = B_Y + DIR_Y &
    .print("Navigate Block: ", NEXT_X, ", ", NEXT_Y) &
    not(hasThingPerception(NEXT_X, NEXT_Y, _, _)) &
    not(getTeamAgentLocation(_, NEXT_X, NEXT_Y)).




+?isBlockAtLocation(BLOCK, relative(DEST_X, DEST_Y))
    :   calculateAbsolutePosition(relative(DEST_X, DEST_Y), absolute(A_X, A_Y)) // Get the relative position of the block destination
    <- ?isBlockAtLocation(BLOCK, absolute(A_X, A_Y)).

+?canNavigateBlock(CUR_X, CUR_Y, DIR)
    <-  !performAction(rotate(cw)).

+?isBlockAtLocation(BLOCK, absolute(DEST_X, DEST_Y))
    :   calculateRelativePosition(relative(R_X, R_Y), absolute(DEST_X, DEST_Y)) & // Get the relative position of the block destination
        hasBlockAttached(CUR_X, CUR_Y, BLOCK) & // Get the location of the attached block
        relative(BD_X, BD_Y) = relative(R_X - CUR_X, R_Y - CUR_Y) & // Relative block displacement
        navigationDirection(DIR, BD_X, BD_Y)
    <-  .print("Is block at ", DEST_X, ", ", DEST_Y);
        ?canNavigateBlock(CUR_X, CUR_Y, DIR);
        !performAction(move(DIR));
        ?isBlockAtLocation(BLOCK, absolute(DEST_X, DEST_Y)).


+!meetAgent(AGENT, req(R_X, R_Y, BLOCK), slave)
    <-  ?getTeamAgentLocation(AGENT, relative(CUR_X, CUR_Y));
        .print("Master At: ", CUR_X, ", ", CUR_Y);
        ?isBlockAtLocation(BLOCK, relative(CUR_X + R_X, CUR_Y + R_Y)).
//        .send(SLAVE_AGENT, achieve, connectBlock(absolute(TARGET_X, TARGET_Y), BLOCK));

+!meetAgent([SLAVE_AGENT, req(OTHER_X, OTHER_Y, BLOCK)], req(X, Y, B), master)
    <-  ?getTeamAgentLocation(AGENT, relative(CUR_X, CUR_Y));
        .print("Slave At: ", CUR_X, ", ", CUR_Y);
        ?isAttachedToCorrectSide(R_X, R_Y, BLOCK);
        !doNothing;
        !meetAgent([SLAVE_AGENT, req(OTHER_X, OTHER_Y, BLOCK)], req(X, Y, B), master).


+!connectBlock(req(X, Y, BLOCK))[source(MASTER_AGENT)]
    :   hasBlockAttached(BLOCK)
    <-  ?getTeamAgentLocation(MASTER_AGENT, relative(CUR_X, CUR_Y));
        ?isBlockAtLocation(BLOCK, relative(CUR_X + X, CUR_Y + Y)).