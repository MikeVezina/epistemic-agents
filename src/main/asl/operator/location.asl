+!translateAllLocations(AGENT, AGENT_O, translation(O_X, O_Y))
    :   translateLocation(AGENT, AGENT_X, translation(X_X, X_Y)) &
        AGENT_X \== AGENT_O &
        not(translateLocation(AGENT_O, AGENT_X, _)) // A translation does not exist between two agents.
    <-  +locationTranslation(AGENT_O, AGENT_X, translation(O_X + X_X, O_Y + X_Y));
        !translateAllLocations.

-!translateAllLocations(AGENT, AGENT_O, TRANSLATION)[error(no_applicable)]
    <- .print("All locations have been translated.").

+locationTranslation(AGENT, AGENT_O, TRANSLATION)
    <- !translateAllLocations(AGENT, AGENT_O, TRANSLATION).