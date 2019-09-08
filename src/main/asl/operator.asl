{ include("tasks/tasks.asl") }
{ include("tasks/requirements.asl") }
{ include("operator/location.asl") }
{ include("auth/auth.asl") }

/*
The operator is going to be responsible for being the central point of communication for our agents.
A few things that the operator should keep track of:
- Absolute position reference for every agent, and the ability to translate between two agents' point of reference.
- Maintaining the overall mental model of the map.
- Task Parsing and requirement assignments.
*/


// O(A1) = (2, 1)
// O(A2) = (1, 3)

// T(A1, A2) = -1, 2

// L(A1) = 2, 13
// L(A2) = 9, -1

// R(A1, A2) = (6, -12)
// = L(A2) + T(A1, A2) - L(A1)

// A(A1, A2) = (8, 1)
// = L(A2) + T(A1, A2)

//
//friendly(2, 1, location(0,0))[source(agentA1)].
//friendly(-2, -1, location(2,1))[source(agentA2)].
//friendly(-2, -1, location(9,9))[source(agentA3)].
//friendly(2, 1, location(7,8))[source(agentA4)].


// Translate between agent locations
translateAgentLocation(A2, LOC)[source(A1)] :-
    locationTranslation(A1, A2, translation(T_X, T_Y)) &
    A2::location(A2_X, A2_Y) &
    A1::location(A1_X, A1_Y) &
    LOC = absolute(A2_X - T_X, A2_Y - T_Y).

getFriendlyMatches(X, Y, AGENT, AGENT_LOCS)
    :-  .findall(agent(AG, LOC_A), friendly(-X, -Y, LOC_A)[source(AG)] & AG \== AGENT, AGENT_LOCS).

!assignTasks.

+friendly(X, Y, LOC)[source(A1)]
    :   getFriendlyMatches(A1, L) &
        assertListEmpty(L)
    <-  .print("List empty").

+friendly(X, Y, LOC)[source(A1)]
    :   getFriendlyMatches(X, Y, A1, [A2|T]) &
        assertListEmpty(T)
    <-  .print("Single");
        !authenticateSingle(agent(A1, LOC), A2, relative(X, Y));
        .abolish(friendly(X, Y, LOC)[source(A1)]).

+friendly(X, Y, location(AGENT_X, AGENT_Y))[source(A1)]
    :   getFriendlyMatches(X, Y, A1, AGENTS) &
        .length(AGENTS) > 1
    <-  .print("Multiple");
        .abolish(friendly(X, Y, location(AGENT_X, AGENT_Y))[source(A1)]).



// TODO: BUG -> Four agents that all have the same relative positions recognize eachother (even if two of them are far away).
// TODO: We need to use our surroundings (terrain or entities) to reinforce two agents that see each other.
// TODO: translation confidence based on environment surroundings

+!coordinateAgents([AGENT, REQ], [AGENT_O, REQ_2])
    <-  .send(AGENT, achieve, nav::meetAgent([AGENT_O, REQ_2], REQ, master));
        .send(AGENT_O, achieve, nav::meetAgent(AGENT, REQ_2, slave)).

+taskAssignment(TASK, AGENT, REQ, OTHER_AGENT)
    <-  .print("Agent ", AGENT, " has been assigned requirement: ", REQ);
        .send(AGENT, achieve, achieveRequirement(TASK, REQ, OTHER_AGENT)).


+obtained(TASK, BLOCK)[source(AGENT)]
    :   taskAssignment(TASK, AGENT, req(X, Y, BLOCK)) &
        taskAssignment(TASK, AGENT_O, req(X_O, Y_O, B_O)) &
        AGENT \== AGENT_O &
        obtained(TASK, B_O)[source(AGENT_O)] // Other agent also obtained block.
    <-  .print(AGENT, " obtained ", TASK, " block: ", BLOCK);
        .send(AGENT, askOne, hasBlockAttached(B), REPLY);
        .send(AGENT_O, askOne, hasBlockAttached(B_O), REPLY_O);
        !coordinateAgents([AGENT, req(X,Y,BLOCK)],[AGENT_O, req(X_O,Y_O,B_O)]).


//// TODO NOTE: it's possible to assign tasks to sub-teams of two agents.
//// (That way we can have multiple tasks being completed at the same time.)
+!assignTasks
    <-  .print("Selecting a Task...");
        !selectTask(TASK);
        .print("Selected Task: ", TASK);
        ?selectTwoTaskRequirements(TASK, REQ, REQ_2);
        +taskAssignment(TASK, agentA1, REQ, agentA2);
        +taskAssignment(TASK, agentA2, REQ_2, agentA1).

+friendly(X, Y)
<- .print("Found friendly: ", X, Y).


