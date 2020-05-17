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
kb::is_possible(kb::hand("Alice", Alice), kb::hand("Bob", Bob), kb::hand("Charlie", Charlie)) :- epistemic.is_valid_hand(Alice, Bob, Charlie).

!play.

// Maybe this shouldn't happen in a plan. Whatever happens here should
// be triggered as a part of BB addition/removal (behind the scenes).
+!open_eyes
    <-  .print("Opening our eyes.");
        +hand("Alice", "AA");
        .wait(500);
        +hand("Bob", "AA");
        +eyes_open.

+!play
    :   not(eyes_open)
    <-  !open_eyes;
        !play.

+!play
    : hand("Charlie", Card)
    <-  .print("Card is ", Card).

+!play <- .wait(200); !play.