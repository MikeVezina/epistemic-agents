{ include("tasks/tasks.asl") }
{ include("tasks/requirements.asl") }
{ include("operator/location.asl") }

/*
The operator is going to be responsible for being the central point of communication for our agents.
A few things that the operator should keep track of:
- Absolute position reference for every agent, and the ability to translate between two agents' point of reference.
- Maintaining the overall mental model of the map.
- Task Parsing and requirement assignments.
*/

!assignTasks.


// TODO: BUG -> Four agents that all have the same relative positions recognize eachother (even if two of them are far away).
// TODO: We need to use our surroundings (terrain or entities) to reinforce two agents that see each other.
// TODO: translation confidence based on environment surroundings


// Coordinate the absolute positions between two agents (AGENT and AGENT_OTHER aka AGENT_O)
+AGENT::thing(X, Y, entity, TEAM)
    :   AGENT::team(TEAM) & // Check to see if the visible entity is on our team
        AGENT_O::thing(X_O, Y_O, entity, TEAM) & // Check to see that another agent sees the current agent
        AGENT \== AGENT_O & // Ensure the two agents are not the same
        (X \== 0 | Y \== 0) & // Check to make sure the perception is not self
        X + X_O == 0 & // If two agents see each other, the relative X,Y location for each perception should add to zero
        Y + Y_O == 0 &
        not(locationTranslation(AGENT, AGENT_O, _)) // Translation does not already exist

    <-  ?AGENT::location(A_X, A_Y); // Get AGENT absolute location reference point
        ?AGENT_O::location(O_X, O_Y); // Get AGENT_O absolute location reference point
        (DIF_X = A_X + X - O_X); // Calculate X difference between points of reference
        (DIF_Y = A_Y + Y - O_Y); // Calculate Y difference between points of reference
        +locationTranslation(AGENT, AGENT_O, translation(DIF_X, DIF_Y)); // Add mental note for translation (
        +locationTranslation(AGENT_O, AGENT, translation(-DIF_X, -DIF_Y)); // Add translation for other agent
        .print("Translation: ", DIF_X, ", ", DIF_Y).


+obtained(TASK, BLOCK)[source(AGENT)]
    :   taskAssignment(TASK, AGENT, req(X, Y, BLOCK)) &
        taskAssignment(TASK, AGENT_O, req(_, _, B_O)) &
        AGENT \== AGENT_O
    <-  .print(AGENT, " obtained ", TASK, " block: ", BLOCK);
        .send(AGENT_O, askOne, hasBlockAttached(B_O), REPLY);
        .print(REPLY);
        .send(AGENT, achieve, nav::navigateToGoal).

+taskAssignment(TASK, AGENT, REQ)
    <-  .print("Agent ", AGENT, " has been assigned requirement: ", REQ);
        .send(AGENT, achieve, achieveRequirement(TASK, REQ)).



// TODO NOTE: it's possible to assign tasks to sub-teams of two agents.
// (That way we can have multiple tasks being completed at the same time.)
+!assignTasks
    <-  .print("Selecting a Task...");
        !selectTask(TASK);
        .print("Selected Task: ", TASK);
        ?selectTwoRequirements(REQ, REQQ);
        +taskAssignment(TASK, agentA1, REQ);
        +taskAssignment(TASK, agentA2, REQQ).


