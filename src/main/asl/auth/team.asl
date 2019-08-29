{ include("common.asl") }


+percept::thing(X, Y, entity, TEAM)
    :   hasThingPerception(X, Y, entity, TEAM) &
        percept::team(TEAM) &
        not(hasMarker(X, Y)) &
        percept::location(L_X, L_Y)
    <-  .send(operator, tell, friendly(X, Y, location(L_X, L_Y))).


+!authenticateSelf(marker(X, Y))
    <-  !performAction(move(e)).

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

