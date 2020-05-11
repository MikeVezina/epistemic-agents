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

// "dont know" isn't necessary for the single agent case.
//kb::dont_know(kb::hand("Alice", _)).

// Now, how do we "open our eyes"? (we are charlie)
!open_eyes.

// Maybe this shouldn't happen in a plan. Whatever happens here should
// be triggered as a part of BB addition/removal (behind the scenes).
+!open_eyes
    <-  +hand("Alice", "AA");
        .wait(2000);
        +hand("Bob", "AA");
        .wait(2000);
        -hand("Bob", "AA");
        .wait(2000);
        -hand("Alice", "AA").

