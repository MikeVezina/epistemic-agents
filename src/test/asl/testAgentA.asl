{ include("common.asl") }
{ include("internal_actions.asl") }

{ include("tasks/tasks.asl") }
{ include("tasks/requirements.asl") }

{ include("nav/navigation.asl", nav) }
{ include("internal_actions.asl") }

!assertThingTypeBeliefs.
!testParseRequirements.

+!assertThingTypeBeliefs
    <-  ?thingType(entity);
        ?thingType(block);
        ?thingType(dispenser);
        .print("Success: thing type beliefs asserted.").


+!testParseRequirements
    <-  !testParseRequirements([req(0, 1, b1)]);
        !testParseRequirements([req(0, 1, b1), req(0, 2, b2)]);
        .print("Successfully Asserted Parsed Requirements.").

+!testParseRequirements(REQS)
    <-  !parseRequirements(REQS);
        !assertParsedRequirements(REQS);
        .abolish(parsedRequirements(_,_,_,_)).

+!assertParsedRequirements([req(X, Y, BLOCK) | T])
    :   T \== [] & calculateDistance(DIST, X, Y)
    <-  ?parsedRequirement(X, Y, DIST, BLOCK);
        !assertParsedRequirements(T).


+!assertParsedRequirements([req(X, Y, BLOCK) | T])
    :   T == [] & calculateDistance(DIST, X, Y)
    <-  ?parsedRequirement(X, Y, DIST, BLOCK).



    // Copied from requirements.asl
-!testRequirements
    <- .print("True");.fail.

+!testRequirements
    :   not(parsedRequirement(_,_,_,_)) &
        (REQS = [req(0, 0, b2)])
    <-  !parseRequirements(REQS);
        !testRequirements.

+!testRequirements
    :   parsedRequirement(_,_,_,_) &
        selectRequirement(X, Y, DIST, BLOCK)
    <-  +percept::attached(X, Y);
        +percept::thing(X, Y, block, BLOCK);
        .print("X: ", X, ", Y: ", Y); !testRequirements.
+!testRequirements
    :   parsedRequirement(_,_,_,_) &
        not(remainingRequirement(_,_,_,_)) &
        not(selectRequirement(_,_,_,_))
    <-  .print("Completed Task Requirements.").

+!testRequirements
    :   parsedRequirement(_,_,_,_) &
        remainingRequirement(_,_,_,_) &
        not(selectRequirement(_,_,_,_))
    <-  .print("No Requirement Selected.").


