/*
The operator is going to be responsible for being the central point of communication for our agents.
A few things that the operator should keep track of:
- Absolute position reference for every agent, and the ability to translate between two agents' point of reference.
- Maintaining the overall mental model of the map.
- Task Parsing and requirement assignments.
*/

+register[source(AG)]
    : not(registeredAgent(AG))
    <-  .print("Registered Agent: ", AG);
        +registeredAgent(AG)
        .send(AG, askOne, percept::location(X, Y)).

// Coordinate the absolute positions between two agents
+AGENT::thing(X, Y, entity, TEAM)
    :   AGENT::team(TEAM) &
        AGENT_O::thing(X_O, Y_O, entity, TEAM) &
        AGENT \== AGENT_O &
        (X \== 0 | Y \== 0) &
        X + X_O == 0 &
        Y + Y_O == 0
    <-  .print("I see you! ", AGENT, AGENT_O).



+AGENT::location(X, Y)
<- .print("Agent ", AGENT, " is at location: ", X, ", ", Y).

