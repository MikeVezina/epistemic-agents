getAgents(AGENTS)
    :-  .df_search("builder", NAMES) &
        .delete(operator, NAMES, AGENTS).


getAgent(AGENT)
    :-  not(.ground(AGENT)) &
        getAgents(ALL_AGENTS) &
        .member(AGENT, ALL_AGENTS).

getFreeAgents(FREE_AGENTS)
    :-  not(.ground(FREE_AGENTS)) &
        .setof(AGENT, getAgent(AGENT) & not(taskAssignment(AGENT, _, _)), FREE_AGENTS).


