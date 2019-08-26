{ include("common.asl") }


+percept::thing(X, Y, entity, TEAM)
    :   hasThingPerception(X, Y, entity, TEAM) &
        percept::team(TEAM) &
        percept::location(L_X, L_Y)
    <-  .broadcast(tell, friendly(X, Y, location(L_X, L_Y))).

+friendly(X, Y, location(AGENT_X, AGENT_Y))[source(AGENT)]
    :   percept::name(ME) &
        percept::team(TEAM) & // Confirm team
        percept::thing(-X, -Y, entity, TEAM) & // Confirm perception
        percept::location(MY_X, MY_Y) & // Get my location
        TRANSLATE = location(MY_X - X - AGENT_X, MY_Y - Y - AGENT_Y) // Calculate the translation between the two agent origins
    <-  .print("Authenticated Friendly as ", AGENT, ". Translation: ", TRANSLATE);
        +team::agentLocation(AGENT, TRANSLATE);
        .broadcast(tell, locationTranslation(AGENT, TRANSLATE));
        .abolish(friendly(X, Y, LOC)[source(AGENT)]). // Remove the message sent by the agent


+locationTranslation(A2, location(A2_X, A2_Y))[source(A1)]
    :   agentLocation(A1, location(A1_X, A1_Y)) & // We only want to translate new locations if we have previously authenticated the source agent
        not(agentLocation(A2, _)) & // We have not authenticated A2 previously.
        TRANSLATE = location(A2_X + A1_X, A2_Y + A1_Y) // Calculate the translation between agents
    <-  +team::agentLocation(A2, TRANSLATE).

+friendly(X, Y, location(AGENT_X, AGENT_Y))[source(AGENT)]
    :   percept::name(NAME) &
        not(percept::thing(-X, -Y, entity, TEAM)) & // Confirm perception
        percept::team(TEAM)// Confirm team
    <-  .print("Not Friendly");
        .abolish(friendly(X, Y, LOC)[source(AGENT)]).



// Calculates the current location of an agent.
// Agent is the other agent to calculate coordinates for.
// The absolute position is the current location of AGENT (based on their percept::location)
// The relative parameter is the calculated return value.

+!getCurrentAgentLocation(AGENT, absolute(X, Y))
    :   team::agentLocation(AGENT, location(T_X, T_Y))// Get the translation value
    <-  .send(AGENT, askOne, percept::location(_, _), percept::location(A_X, A_Y));
        X = A_X - T_X;
        Y = A_Y - T_Y.


+!getCurrentAgentLocation(AGENT, relative(X, Y))
    :   percept::location(MY_X, MY_Y)
    <-  !getCurrentAgentLocation(AGENT, absolute(A_X, A_Y));
        X = A_X -  MY_X;
        Y = A_Y - MY_Y.

//        ?REPLY;
//        percept::location(A_X, A_Y)

