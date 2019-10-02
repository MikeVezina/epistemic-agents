/**
 * Attach Component (attach.asl)
 * This component is responsible for handling the attach command.
 * This also includes action failures.
 */

// Checks if a given block is attached to anything
isBlockAttached(X, Y) :- hasBlockAttached(X, Y, _).



// Returns a block that is currently free (not attached to any entity)
hasFreeBlock(X, Y, BLOCK_TYPE) :- hasBlockPerception(X, Y, BLOCK_TYPE) & not(isBlockAttached(X, Y)).

hasFreeBlockBeside(BLOCK_TYPE, DIR) :-
    hasFreeBlock(X, Y, BLOCK_TYPE) &
    xyToDirection(X, Y, DIR). // Determine if the X, Y coordinates correspond to a direction

// Attaches a block of type BLOCK_TYPE
+!attachBlock(BLOCK_TYPE)
    :   hasFreeBlockBeside(BLOCK_TYPE, DIR) // Get a block beside us that isn't attached
    <-  !attach(DIR).


/**
    Reasons for this failure to trigger:
    -   No block of type BLOCK_TYPE is beside us
    -   BLOCK_TYPE is beside us, but already attached to our self or a nearby agent
        (check attached percept)
**/
@attach_failure[breakpoint]
-!attachBlock(BLOCK_TYPE) [error(ERR)]
    <-  .print("There was an issue attaching block type ", BLOCK_TYPE, ". Error: ", ERR);
        .fail.

/** Handle Attach Actions **/
+!attach(X, Y) :  xyToDirection(X, Y, DIR) <- !attach(DIR).
+!attach(DIR)  <- !performAction(attach(DIR)).

// On successful attachment, we want to determine if there was more than one attachment
// that was attached. We also want to run the attach action, so that we can update our internal model
+!handleActionResult(attach, [DIR], success)
    :   .findall([X, Y], attached(X, Y), ATTACHED_BLOCKS)
    <-  .print(ATTACHED_BLOCKS).

// Attach Action failures
+!handleActionResult(attach, [DIR], FAILURE)
    : FAILURE \== success
    <-  .print("This is where the attach failure is handled").
