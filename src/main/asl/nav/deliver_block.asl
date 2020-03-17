isMasterReq(req(X, Y, _))
    :-  X == 0 & Y == 1.

getAttachedAbsolute(BLOCK, A_X, A_Y)
    :-  hasBlockAttached(R_X, R_Y, BLOCK) &
        calculateAbsolutePosition(relative(R_X, R_Y), absolute(A_X, A_Y)).

isBesideAbsolute(X, Y)
    :-  .ground(X) &
        .ground(Y) &
        calculateRelativePosition(relative(R_X, R_Y), absolute(X, Y)) &
        xyToDirection(R_X, R_Y, _).

/** Aligns the block to the requirement spec **/
+!alignBlock(req(X, Y, BLOCK))
    :   hasBlockAttached(X_ACTUAL, Y_ACTUAL, BLOCK) &
        .ground(X_ACTUAL) & .ground(Y_ACTUAL) &
        (X_ACTUAL \== X | Y_ACTUAL \== Y)
    <-  .print("Aligning Block");
        !rotateToDirection(X_ACTUAL, Y_ACTUAL, X, Y);
        !alignBlock(req(X, Y, BLOCK)).

/** Aligns the block to the requirement spec **/
+!alignBlock(req(X, Y, BLOCK))
    :   hasBlockAttached(X_ACTUAL, Y_ACTUAL, BLOCK) &
        X_ACTUAL == X & Y_ACTUAL == Y
    <- .print("Has Good Alignment.").

+!alignBlock(_)
    <-  .print("Align Block Missing?").

// The plan responsible for collaborating with the next slave
// In this case, we are finished connecting all of the requirements.
+!informSlaves(Task)
        // Using the requirement planner to get the next requirement:
    :   eis.internal.get_next_req(Task, _, done)
    <-  .print("All Requirements met.");
        !performAction(submit(Task)). // Submit the task

// Attempts synchronization and connection with one slave agent at a time
// PrevReq is the last connected requirement block that the slave will be connecting their block to.
// This preserves the proper connection order of the task requirements.
+!informSlaves(Task)
    :   eis.internal.get_next_req(Task, PrevReq, Req) & // Determine next requirement
        taskAssignment(Slave, Task, Req) // Given the next requirement, unify the Slave
    <-  .send(Slave, tell, meetingPointSet(Task)); // Inform the slave of the meeting point
        .abolish(slaveConnect(Task, _)[source(_)]); // Remove old slave connect attempts
        !prepareForConnect(Slave); // Prepare the master for slave synchronization
        !waitForConnect(Slave, Task, PrevReq, Req); // Synchronize with slave and perform connect
        !informSlaves(Task). // Inform the next slave or submit the task if complete.

+!waitForConnect(SLAVE, TASK, PREV_REQ, REQ)
    :   not(slaveConnect(TASK, REQ)[source(SLAVE)]) &
        (req(CON_X, CON_Y, _) = PREV_REQ)
    <-  .print("Waiting for slave.");
        .wait(50);
        !skipUntilSlaveConnect(SLAVE);
        !waitForConnect(SLAVE, TASK, PREV_REQ, REQ).

+!skipUntilSlaveConnect(SLAVE)
    :   not(slaveConnect(_,_)[source(SLAVE)])
    <-  !performAction(skip).

+!skipUntilSlaveConnect(_)
    <-  .print("No Need to skip").

+!waitForConnect(SLAVE, TASK, PREV_REQ, REQ)
    :   slaveConnect(TASK, REQ)[source(SLAVE)] &
        (req(CON_X, CON_Y, _) = PREV_REQ)
    <-  .print("Attempting to connect.");
        !connect(SLAVE, CON_X, CON_Y);
        !waitForDetach(SLAVE, REQ).

+!waitForDetach(SLAVE, REQ)
    :    not(slaveDetached[source(SLAVE)])
    <-  .print("Waiting for slave detach");
        .wait(50);
        !waitForDetach(SLAVE, REQ).

+!waitForDetach(SLAVE, REQ)
    :   slaveDetached[source(SLAVE)] &
        req(X, Y, BLOCK) = REQ
    <-  .print("Slave ", SLAVE, " detached");
        blockAttached(X, Y).


+!deliverBlock(TASK, REQ)
    :   isMasterReq(REQ) &
//        eis.internal.get_next_req(TASK)
        eis.internal.meeting_point(TASK, LOC) &
        location(X, Y) = LOC
    <-  .print("I Am Master! Meeting Point: ", LOC);
        !navigateToDestination(X, Y);
        .print(RES);
        !alignBlock(REQ);
        !informSlaves(TASK).

+!skipForever
    <-  !performAction(skip);
        !skipForever.




+!moveAndRotateBlock(BLOCK, DEST_X, DEST_Y) // DEST_X and DEST_Y are absolute coordinates for where the block needs to go
    :   hasBlockAttached(R_X, R_Y, BLOCK) & // Get the relative position of the block
        calculateRelativePosition(relative(D_RX, D_RY), absolute(DEST_X, DEST_Y)) & // Calculate the relative position of the destination
        (D_RX == 0 & D_RY == 0) // If the agent is currently on the destination
    <-  !moveOrExplore(BLOCK, DEST_X, DEST_Y); // Move one space away from the cell
        !moveAndRotateBlock(BLOCK, DEST_X, DEST_Y).

+!moveAndRotateBlock(BLOCK, DEST_X, DEST_Y) // DEST_X and DEST_Y are absolute coordinates for where the block needs to go
    :   hasBlockAttached(R_X, R_Y, BLOCK) & // Get the relative position of the block
        calculateRelativePosition(relative(D_RX, D_RY), absolute(DEST_X, DEST_Y)) & // Calculate the relative position of the destination
        (D_RX \== R_X & D_RY \== R_Y) // If the block is currently NOT on the destination
    <-  !rotateToDirection(R_X, R_Y, D_RX, D_RY);
        !moveAndRotateBlock(BLOCK, DEST_X, DEST_Y). // Move one space away from the cell

+!moveAndRotateBlock(BLOCK, DEST_X, DEST_Y)
    :   hasBlockAttached(R_X, R_Y, BLOCK) & // Get the relative position of the block
            calculateRelativePosition(relative(D_RX, D_RY), absolute(DEST_X, DEST_Y)) & // Calculate the relative position of the destination
            (D_RX == R_X & D_RY == R_Y) // If the block is currently on the destination
    <-  .print("No need to move/rotate block: ", BLOCK, ", ", DEST_X, ", ", DEST_Y).

+!moveAndRotateBlock(BLOCK, DEST_X, DEST_Y)
    <-  .print("No Applicable plan for ", BLOCK, ", ", dest(DEST_X, DEST_Y), ". May be due to a blocked destination.").


+!moveOrExplore(BLOCK, DEST_X, DEST_Y)
    :   hasBlockAttached(R_X, R_Y, BLOCK) &
        xyToDirection(-R_X, -R_Y, DIR) &
        eis.internal.can_agent_move(DIR)
     <- .print("Moving... ", DIR);
        !move(DIR).

+!moveOrExplore(BLOCK, DEST_X, DEST_Y)
    :   hasBlockAttached(R_X, R_Y, BLOCK)
     <- .print("Exploring... ", DIR);
        !explore.

//+!navigateBlock(req(_, _, BLOCK), X, Y)
//    :   getAttachedAbsolute(BLOCK, A_X, A_Y) &
//        not(isAtLocation(X, Y)) &
//        isBesideAbsolute(A_X, A_Y)
//    <-


+!navigateBlock(req(_, _, BLOCK), X, Y)
    :   getAttachedAbsolute(BLOCK, A_X, A_Y) &
        A_X == X & A_Y == Y
    <-  .print("Block Delivered!").

+!navigateBlock(req(_, _, BLOCK), X, Y)
    :   getAttachedAbsolute(BLOCK, A_X, A_Y) &
        (A_X \== X | A_Y \== Y) &
        isAtLocation(X, Y) &
        D_X = X - A_X &
        D_Y = Y - A_Y &
        xyToDirection(D_X, D_Y, DIR)
    <-  .print("Delivering Block. I'm standing on it!");
        !moveAndRotateBlock(BLOCK, X, Y);
        !navigateBlock(req(_, _, BLOCK), X, Y).

+!navigateBlock(req(_, _, BLOCK), X, Y)
    :   getAttachedAbsolute(BLOCK, A_X, A_Y) &
        (A_X \== X | A_Y \== Y) &
        not(isCurrentLocation(X, Y))
    <-  .print("Delivering Block!");
        !stepToDestination(X, Y);
        !navigateBlock(req(_, _, BLOCK), X, Y).

+!slaveConnect(MASTER, TASK, REQ)
    :   req(_, _, BLOCK) = REQ &
        hasBlockAttached(X, Y, BLOCK)
    <-  .send(MASTER, tell, slaveConnect(TASK, REQ));
        !prepareForConnect(MASTER);
        !connect(MASTER, X, Y);
        !detach(X, Y);
        .send(MASTER, tell, slaveDetached);
        !exploreForever.


// This plan gets executed when the slave has been notified by the master to deliver its block.
+!deliverBlock(Task, Req)
    :   not(isMasterReq(Req)) & // Check that I am a slave
        meetingPointSet(Task)[source(Master)] & // I have received a notification to deliver the block to master
        eis.internal.get_slave_meeting_point(Master, Req, location(X, Y)) // Get the translated meeting point

        // Navigate our attached block to the meeting point, ensuring it gets placed exactly on X, Y.
    <-  !navigateBlock(Req, X, Y); // This brings the block to a location that allows us to connect with the master.
        !slaveConnect(Master, Task, Req). // Notify and connect with the master agent

+!deliverBlock(TASK, REQ)
    <-  .print("I am not master, and do not have a meeting point.");
        !explore;
        !deliverBlock(TASK, REQ).