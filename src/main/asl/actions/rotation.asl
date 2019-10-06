/*
 * Rotation Component (rotation.asl)
 * This component is responsible for providing plans for achieving agent rotation in various scenarios.
 * Rotation is only used when blocks are attached, as it would have no effect on an agent without any attached blocks.
*/



// To unblock an attachment:
// Get all available rotations
//  - Has one available rotation: rotate.
//  - Has two rotations: rotate away from where we are trying to navigate to (keep the attachment behind us)

rotationDirection(cw).
rotationDirection(ccw).

/** Rules for rotation **/
getRotations(ROTS) :-
    eis.internal.get_rotations(ROTS).

// Gets a random (non-blocked) rotation direction
getRotation(ROT) :-
    not(.ground(ROT)) & // Checks if ROT is ground.
    canRotate(ROT).

// Check if we can rotate with ROT rotation
canRotate(ROT) :-
    rotationDirection(ROT) &
    getRotations(ROTS) &
    .member(ROT,ROTS).

hasExhaustedRotation(ROT)
    :-  exhaustedRotation(ROT) |
        numRotations(ROT, 4).

lastRotationSuccess(ROT)
    :-  getLastAction(rotate) &
        getLastActionResult(success) &
        getLastActionParams([ROT]).


// Resets the current exhausted rotations.
+!resetExhaustedRotations
    <-  .abolish(currentRotation(_));
        .abolish(numRotations(_, _));
        .abolish(exhaustedRotation(_)).


+!exhaustedRotation
    :   currentRotation(ROT) &
        not(hasExhaustedRotation(ROT))
    <-  !rotate(ROT).

// No current rotation
+!exhaustedRotation
    :   not(currentRotation(_)) &
        getRotation(ROT) &  // Gets an unblocked rotation
        not(hasExhaustedRotation(ROT)) // Ensure we have not exhausted it yet
    <-  +currentRotation(ROT);
        +numRotations(ROT, 0);
        .print("Setting ", ROT, " to 0");
        !exhaustedRotation.

// Current rotation that has been exhausted.
// Reset current rotation, and see if there is another available rotation.
+!exhaustedRotation
    :   currentRotation(ROT) &
        hasExhaustedRotation(ROT) // Exhausted current rotation.
    <-  .abolish(currentRotation(ROT));
        !exhaustedRotation.


// Rotation Action Plans and Result Handling
+!rotate(ROT)   :   .ground(ROT) & rotationDirection(ROT)  <-  !performAction(rotate(ROT)).


+!updateRotationCount(ROT)  : not(numRotations(ROT, _)) <- .print("No Rotation count to update for ", ROT).
+!updateRotationCount(ROT)  : numRotations(ROT, COUNT) <- -numRotations(ROT, COUNT); +numRotations(ROT, COUNT + 1); .print("Rotation Count: ", COUNT + 1).

/** Sets the current rotation as exhausted if we are currently exhausting a rotation **/
+!updateExhaustedRotation(ROT)  : not(numRotations(ROT, _)) <- .print("No rotation being exhausted.").
+!updateExhaustedRotation(ROT)  : numRotations(ROT, _) <- .print("Failed to rotate. Exhausted rotation: ", ROT); +exhaustedRotation(ROT).

+!handleActionResult(rotate, [ROT], success)
    <-  !updateRotationCount(ROT).

+!handleActionResult(rotate, [ROT], failed)
    <-  !checkExhaustedRotation;
        .print("One of the things attached to the agent cannot rotate to its target position OR the agent is currently attached to another agent.");
        .fail.

+!handleActionResult(rotate, [ROT], failed_parameter)
    <-  .print("Parameter was not a rotation direction: ", ROT);
        .fail.