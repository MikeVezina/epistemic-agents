{ include("common.asl") }

hasFriendly(X, Y, TEAM, location(ABS_X, ABS_Y), _)
    :-  percept::thing(-X, -Y, entity, TEAM) &
        percept::location(ABS_X, ABS_Y).


@auth_add[atomic]
+hasFriendly(R_X, R_Y, TEAM, A2_LOC, MY_LOC)[source(A2)]
    :   .my_name(A1) &
        A1 \== A2 &
        not(percept::teamAgent(_, _, A2))
    <-  .print("Received Friendly: ", TRANSLATE);
        !auth::authenticateSingle(agent(A1, MY_LOC), agent(A2, A2_LOC), relative(R_X, R_Y)).

@auth_check[atomic]
+percept::thing(X, Y, entity, TEAM)
    :   hasThingPerception(X, Y, entity, TEAM) &
        percept::team(TEAM) &
        not(hasMarker(X, Y)) &
        percept::location(L_X, L_Y)
    <-  .print(X, ", ", Y, " Test");
        .broadcast(askOne, hasFriendly(X, Y, TEAM, POSITION, location(L_X, L_Y))).


+!authenticateSelf(marker(X, Y))
    :   randomDirection(DIR)
    <-  !performAction(move(DIR)).

getTeamAgentLocation(AGENT, relative(X, Y))
    :-  percept::teamAgent(X, Y, AGENT).

getTeamAgentLocation(AGENT, absolute(X, Y))
    :-  percept::teamAgent(R_X, R_Y, AGENT) &
        calculateAbsolutePosition(relative(R_X, R_Y), absolute(X, Y)).


