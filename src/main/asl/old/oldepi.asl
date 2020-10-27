









/*** ------------------------ ***/
/*** ------------------------ ***/
/*** ------------------------ ***/
/*** ------------------------ ***/



// Common Knowledge (this is what we can use to define the indistinguishability)
// These are the possible predicates for each world
+commonKnowledge(xor(charlie("AA"), charlie("88"), charlie("A8"))).
+commonKnowledge(xor(bob("AA"), bob("88"), bob("A8"))).
+commonKnowledge(xor(alice("AA"), alice("88"), alice("A8"))).

// Removing any impossible worlds
/*
invalid_worlds = [
   ['AA', 'AA', 'AA'],
   ['A8', 'AA', 'AA'],
   ['AA', 'A8', 'AA'],
   ['AA', 'AA', 'A8'],

   ['88', '88', '88'],
   ['A8', '88', '88'],
   ['88', 'A8', '88'],
   ['88', '88', 'A8'],
];
*/


// Question: How to introduce indistinguishability for other agents? This is required to make the public announcements useful.
// Even though it is the single-agent case, we still need to model the indistinguishable worlds for the other agents.

+commonKnowledge(not(and(alice("AA"), bob("AA"), charlie("AA")))).
+commonKnowledge(not(and(alice("AA"), bob("AA"), charlie("AA")))).

// Charlie's Initial beliefs / perceptions:
+alice("AA").
+bob("88").
















// Our framework would call this rule to determine the indistinguishability relation.
// It seems this could actually have a default implementation:
//    the indistinguishable worlds are the ones that share all terms except for ONE fluent
//    i.e. in the following example it would be any world that shares the same x and y terms (the fluent is the agent).
//    this also works for aces and eights (the one fluent is the current agent's cards).

/* ======

 * Feedback: Confusing.
 * 1. Should apply to worlds rather than to 'possible'.
 * 2. Any belief expressed in AS is accessible in any reachable world, those worlds are indistinguishable to the agent.
 * 3. Indistinguishability is something reasoner should worry about, not developer.

 * ======
 */
indistinguishable(possible(agent(X1, Y1, Ag1)), possible(agent(X2, Y2, Ag2)))
    :- X1 == X2 & Y1 == Y2. // Worlds are indistinguishable if they have the same X / Y terms. Ag1 and Ag2 are not relevant.

// Perceive an agent at (0, 1). This could of course be generalized to any X, Y
+perceive(0, 1)
    :   agent(0, 1, Agent) // Do we know who the agent is? This is what the reasoner will introduce. Initially it is false.
    <-  .print("Identified agent ", Agent, " at 0, 1").


+perceive(0, 1)
    :   not(agent(0, 1, _)) // We do NOT know who the agent is
    <-  +possible(agent(0, 1, "Alice")); // The possible agents are alice / bob / carl
        +possible(agent(0, 1, "Bob")); // These are the possible worlds.
        +possible(agent(0, 1, "Carl")).




// =================== //
/** Behind the scenes **/
// =================== //
At every reasoning cycle:
    1. Get all possible(Belief) beliefs. For each Belief b:
        - If b is an untracked (new) belief, create a model with all beliefs that share the same name as b (i.e. agent(_, _, _))

    2. If there is a Belief b that is currently being tracked by our reasoner, but no longer in the belief base:
        -  remove worlds and relations associated with b

    3. If there are any pending messages from other agents:
        - Process these messages, update model accordingly

    4. Check set of current possible worlds.
        - Remove any possible(Belief) beliefs if the model no longer has these possibilities.

    5. Did we resolve any fluents? I.e. for all possible worlds, can we say the fluent is the same across all possible worlds?
        - If one fluent is known/resolved, we can unify that fluent in the possible(Belief) belief. (i.e. for agent(0, 1, Ag), Ag is the fluent. If it gets resolved, we can unify the result to Ag).
        - If ALL fluents are resolved (since it is possible we have more than one fluent), then we can remove all possible(Belief) beliefs, and introduce the unified Belief to the belief base.
            - I.e. we remove all possible(agent(0, 1, _)) beliefs, and add the only possible agent(0, 1, "Alice") belief (in the case where alice is the perceived agent)

    6. Once agent(0, 1, Ag) is no longer a 'possibility', but rather the 'truth', the agent can act on this new knowledge. (the relevant plan gets executed)


