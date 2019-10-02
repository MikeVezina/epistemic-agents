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
    getRotations([ROT | _]).

// Check if we can rotate with ROT rotation
canRotate(ROT) :-
    rotationDirection(ROT) &
    getRotations(ROTS) &
    .member(ROT,ROTS).


/*
 * unblockAttachments(DIR):
 * Unblocks attachments by rotating the attachments so that they don't block our movement.
*/
+!unblockAttachments(DIR)
    :   .ground(DIR).   // Ensure we are passed a bound direction
                        // Get all rotations that aren't blocked