// 1. All possible cards
kb::hand("AA").
kb::hand("A8").
kb::hand("88").

// 2. Rules to specify Alice / Bob / Charlie all have cards
kb::hand("Alice", Hand)[prop] :- kb::hand(Hand).
kb::hand("Bob", Hand)[prop] :- kb::hand(Hand).
kb::hand("Charlie", Hand)[prop] :- kb::hand(Hand).


// 3. A rule to help Determine invalid hands. Defer to an internal action to reduce amount of explicit code.
// The 'is_valid_hand' internal action counts the number of Aces/Eights and returns true if it is a valid distribution of cards
kb::is_possible(kb::hand("Alice", Alice), kb::hand("Bob", Bob), kb::hand("Charlie", Charlie)) :- epi.is_valid_hand(Alice, Bob, Charlie).





/











// Determine possible world propositions using rule.
// Our framework looks for this 'distribution' rule and expands all possibilities.
// This is what helps us determine the possible worlds.
// Right now this is necessary to generate combinations of all predicates. (I was getting stuck otherwise).
// Next step is to automate this.
//kb::distribution(Alice, Bob, Charlie) :-
//    kb::hand("Alice", Alice) &
//    kb::hand("Bob", Bob) &
//    kb::hand("Charlie", Charlie).



// =================






// Worlds that are not possible
// What about a rule that determines whether a world is not possible?
//~possible("AA", "AA", "AA").
//~possible("A8", "AA", "AA").
//~possible("AA", "A8", "AA").
//~possible("AA", "AA", "A8").
//
//~possible("88", "88", "88").
//~possible("A8", "88", "88").
//~possible("88", "A8", "88").
//~possible("88", "88", "A8").
//
//
//
//// Use Jason-provided 'all_unifs" to unify all possible values to each variable (aka Alice, Bob, Charlie)
//@set_up_possible[all_unifs]
//+!setUp
//    :   alice_card(Alice) & bob_card(Bob) & charlie_card(Charlie) & // Unify values to Alice/Bob/Charlie
//        not(possible(Alice, Bob, Charlie)) &                        // Check if we've already added this world
//        not(~possible(Alice, Bob, Charlie))                         // Check that the world is not impossible (aka ~possible)
//
//    <-  +possible(Alice, Bob, Charlie);                             // Add the possible world
//        .print("Adding World: ", world(Alice, Bob, Charlie));       // Print out the world
//        !setUp.                                                     // Run setUp again until we generate all worlds recursively

+!setUp
    <- .print("Finished set-up!"). // Done :)


























//!hello.

//!playGame.

+!playGame
    :   turn &
        action(Action)
    <-  .print("My Turn: ", Action);
        .wait(700);
        announce(Action);
        !playGame.

+!playGame <- !playGame.

//+!hello
//    : k_box(alice, aa)
//    <- .print("Alice knows AA");
//        announce(k_box(alice, aa)).
//
//+!hello
//    : ~knows(alice(aa))
//    <- .print("not Alice knows AA").
//
//+!hello
//    <- .print("Alice not sure AA").