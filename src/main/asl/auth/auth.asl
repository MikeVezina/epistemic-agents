// Two agents that perceive each other must do the following to authenticate each other:
// 1. If we perceive only one agent with relative position X, Y (and they see us), then we can successfully authenticate that agent.
// 2. If more than one pair of agents perceive each other with shared relative positions,
//    then we must get each agent pair to generate a unique  marker
// 3. If a unique marker can not be generated for each pair, only authenticate one pair at a time

canAuthenticate(A_1, A_2)
    :-  not(authenticated(A_1, A_2, _)) &
        not(authenticated(A_2, A_1, _)).

generateUniqueMarker(X, Y)
    :-  X = 0 & Y = 1. // Placeholder for randomly generated marker

calculateTranslationValue(agent(A1, location(A1_X, A1_Y)), agent(A2, location(A2_X, A2_Y)), relative(R_X, R_Y), TRANSLATE)
    :-  TRANSLATE = location(A1_X + R_X - A2_X, A1_Y + R_Y - A2_Y).


getAgentsFromStruct([agent(A, _)|T], AGENTS)
    :-  .print("Empty ", T) & assertEmptyList(T) & .print("Empty ", A) & .concat([ ], [A], _).

getAgentsFromStruct([agent(A, _)|T], AGENTS)
    :-  assertListHasElements(T) & .print(A, ", ", T) & getAgentsFromStruct(T, RES) & .print("RES ", RES) & .concat(RES, [A], AGENTS).

//@auth[atomic]
+!authenticate(agent(A1, A1_LOC), agent(A2, A2_LOC), relative(X, Y))
    :   canAuthenticate(A1, A2)
    <-  .print("Authenticating ", A2);
        .send(A2, achieve, authenticateSelf(marker(X, Y)));
        .wait("+team::authSuccess[source(AGENT)]");
        .print(AGENT, " sees ", A2);
        +authenticated(A1, A2).


+!authenticate(agent(A_1, _), agent(A_2, _), _)
    :   not(canAuthenticate(A1, A2))
    <-  .print("Agents [", A1, ", ", A2, "] have already been authenticated.").

@auth_single[atomic]
+!auth::authenticateSingle(agent(A1, LOC_A1), agent(A2, LOC_A2), REL)
    :   canAuthenticate(A1, A2) &
        calculateTranslationValue(agent(A1, LOC_A1), agent(A2, LOC_A2), REL, TRANSLATION)
    <-  .print("Authenticated (No Clashes): ", A1, " and ", A2, ". Translation: ", TRANSLATION);
        +authenticated(A1, A2, TRANSLATION);
        authenticateAgents(A1, A2, TRANSLATION).

+!auth::authenticateSingle(agent(A1, _), agent(A2, _), _)
    :   not(canAuthenticate(A1, A2))
    <-  .print("Agents [", A1, ", ", A2, "] have already been authenticated.").

-!auth::authenticateSingle(_,_,_)
<-  .print("Test").

@auth[atomic]
+!authenticateAll(agent(A1, _), AGENTS, relative(X, Y))
    <-  .print("Authenticating ", A1, AGENTS);
        .send(A1, achieve, authenticateSelf(marker(X, Y))).

// We are going to authenticate an agent located at our relative position X, Y
//+!authenticate([A_1, relative(A1_X, A1_Y)], [A_2, relative(A2_X, A2_Y)])
//    :   not(hasBeenAuthenticated(A_1, A_2))
//    <-  ?

//+!authenticate([A_1, relative(A1_X, A1_Y)], [A_2, relative(A2_X, A2_Y)])
//    :   hasBeenAuthenticated(A_1, A_2)
//    <-  .print("Agents [", A_1, ", ", A_2, "] have previously been authenticated.").