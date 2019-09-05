{ include("common.asl") }


+percept::thing(X, Y, entity, TEAM)
    :   hasThingPerception(X, Y, entity, TEAM) &
        percept::team(TEAM) &
        not(hasMarker(X, Y)) &
        percept::location(L_X, L_Y)
    <-  .print(X, ", ", Y, " Test");.send(operator, tell, friendly(X, Y, location(L_X, L_Y))).


+!authenticateSelf(marker(X, Y))
    :   randomDirection(DIR)
    <-  !performAction(move(DIR)).

getTeamAgentLocation(AGENT, relative(X, Y))
    :-  percept::teamAgent(X, Y, AGENT).

getTeamAgentLocation(AGENT, absolute(X, Y))
    :-  percept::teamAgent(R_X, R_Y, AGENT) &
        calculateAbsolutePosition(relative(R_X, R_Y), absolute(X, Y)).


