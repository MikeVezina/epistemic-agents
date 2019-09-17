// Two agents that perceive each other must do the following to authenticate each other:
// 1. If we perceive only one agent with relative position X, Y (and they see us), then we can successfully authenticate that agent.
// 2. If more than one pair of agents perceive each other with shared relative positions,
//    then we must get each agent pair to generate a unique  marker
// 3. If a unique marker can not be generated for each pair, only authenticate one pair at a time


calculateTranslationValue(agent(A1, location(A1_X, A1_Y)), agent(A2, location(A2_X, A2_Y)), relative(R_X, R_Y), TRANSLATE)
    :-  TRANSLATE = location(A1_X + R_X - A2_X, A1_Y + R_Y - A2_Y).

@auth_single[atomic]
+!auth::authenticateSingle(agent(A1, LOC_A1), agent(A2, LOC_A2), REL)
    :   calculateTranslationValue(agent(A1, LOC_A1), agent(A2, LOC_A2), REL, TRANSLATION)
    <-  .print("Attempting to authenticate: ", A1, " and ", A2, ". Translation: ", TRANSLATION);
        authenticateAgents(A1, A2, TRANSLATION).