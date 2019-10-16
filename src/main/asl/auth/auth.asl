
@friendly_auth[atomic]
+!processFriendlies(CUR_STEP)
    :   .findall(agent(AGENT, MY_POS, X, Y), hasFriendly(CUR_STEP - 1, X, Y, MY_POS)[source(AGENT)], AGENTS) &
        .length(AGENTS, SIZE) & SIZE > 0 &
        eis.internal.authenticate_agents(AGENTS) // Authenticate the agents
    <-  .abolish(hasFriendly(CUR_STEP - 1, _, _, _)[source(_)]). // Remove any hasFriendly notifications

// No Friendlies to process
@no_friendlies[atomic]
+!processFriendlies(_).
