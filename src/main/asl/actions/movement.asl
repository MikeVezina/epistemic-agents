/* Movement Component
 *
 * movement.asl should define all plans and test goals for achieving one unit of movement.
 * This file should handle obstacle avoidance, as well as action failures, so that the agent reliably gets
 * to it's destination.
 *
 * This component makes the assumption that the agent has either 0 or 1 attached blocks for the sake of
 * rotational and movement simplicity. This component also makes the assumption that path finding has
 * already generated a suitable path for the agent. This means that we can guarantee that the agent will not be blocked.
 * Any attachments however may be blocked in their current orientation. If the agent itself is blocked, we will trigger a
 * plan failure, otherwise if the attachment is blocked, we will try to find a suitable rotation so that
 * we may successfully navigate in the specified direction.
 */

direction(n).
direction(s).
direction(e).
direction(w).


// Check if the attached block is unblocked in the given direction
attachmentsUnblocked(DIR) :-
    not(eis.internal.are_attachments_blocked(DIR)).

// Checks if the agent is unblocked in the given direction
agentUnblocked(DIR) :-
    not(eis.internal.is_agent_blocked(DIR)).

// Checks to see if the agent can move in the given direction.
// This should also check to see if any attachments are blocked.
canMove(DIR)
    :-  agentUnblocked(DIR) &
        attachmentsUnblocked(DIR).

/** Attachments Unblocked Test Goal Events (An attachment is blocked) **/
// This test goal event occurs when the attachments are blocked.
// Checks if there is an available rotation
+?attachmentsUnblocked(DIR)
    :   eis.internal.get_rotations([ROT|_])
    <-  .print("Rotation");
        !performAction(rotate(ROT));
        ?attachmentsUnblocked(DIR).


/** Can Move Test Goal Events (The agent or attachment is blocked) **/
+?canMove(DIR)
    <-  ?agentUnblocked(DIR); // Test if the agent is unblocked
        ?attachmentsUnblocked(DIR); // Test if the attachments are unblocked.
        ?canMove(DIR). // Re-test goal


/**
    Checks if the agent can move in the given direction, and attempts basic rotations if our attachments
    are blocking our movement. This plan will fail if we are blocked and no rotations are available,
    or if the agent is blocked (this should not occur because path finding
     should give us a path that doesn't block the agent).
**/
+!move(DIR) : .ground(DIR) & direction(DIR)  <- ?canMove(DIR);  !performAction(move(DIR)).
+!move(DIR)                                  <- .print("Failed to provide a valid direction: ", DIR); .fail.
