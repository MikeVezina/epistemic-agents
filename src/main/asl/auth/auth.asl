// Two agents that perceive each other must do the following to authenticate each other:
// 1. If we perceive only one agent with relative position X, Y (and they see us), then we can successfully authenticate that agent.
// 2. If more than one pair of agents perceive each other with shared relative positions,
//    then we must get each agent pair to generate a unique  marker
// 3. If a unique marker can not be generated for each pair, only authenticate one pair at a time

@auth[atomic]
+!attemptAuthentication(AGENT, STEP, X, Y, MY_POS)
    :   getFriendlyMatches(STEP, X, Y, AGENT, [agent(OTHER_AGENT, AGENT_LOC) | T]) &
        .length(T, 0)
    <-  .print("Attempting Authentication");
        .print(A1, " Authenticating: ", OTHER_AGENT);
        authenticateAgents(AGENT, MY_POS, OTHER_AGENT, AGENT_LOC, X, Y).

+!attemptAuthentication(agent(AGENT_NAME, AGENT_LOC, OTHER_AGENT, OTHER_AGENT_LOC, PER_X, PER_Y))
    <-  .print("Attempting to authenticate: ", AGENT_NAME, " and ", OTHER_AGENT, ".");
        authenticateAgents(AGENT_NAME, AGENT_LOC, OTHER_AGENT, OTHER_AGENT_LOC, PER_X, PER_Y).

+!attemptAuthentication([H|T])
    : T \== []
    <-  !attemptAuthentication(H);
        !attemptAuthentication(T).

+!attemptAuthentication([H|[]])
    <-  !attemptAuthentication(H).

+!attemptAuthentication([]).