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
kb::is_possible(kb::hand("Alice", Alice), kb::hand("Bob", Bob), kb::hand("Charlie", Charlie)) :- internal.is_valid_hand(Alice, Bob, Charlie).


// Set initial goal to !play
!play.

// Initially our eyes are not open, so execute the open_eyes plan
// and re-introduce the play goal.
+!play : not(eyes_open) <- !open_eyes; !play.



// This gets executed AFTER execution of !open_eyes
// This shows that we can access knowledge as if it were a belief.
+!play : know(hand("Alice", Card))
    <-  .print("====");
        .print("+!play Plan Says: ", "We know that alice has the cards: ", Card).

// Open our eyes (here we introduce ground beliefs that correspond to epistemic propositions, i.e. hand("Alice", "AA"))
// These will in-turn generate the knowledge events seen below this plan.
+!open_eyes
    <-  .print("Opening our eyes.");
        .print("");
        .print("=======");
        +~hand("Alice", "AA");
        .print("We see that Alice has AA");
        .wait(500);
        .print("");
        .print("=======");
        +hand("Bob", "AA");
        .print("We see that Bob has AA");
        +eyes_open.


+~know(hand("Charlie", Card))
    <-  .print("we DONT know if our card is ", Card, " (YET!!)").

+know(~hand("Charlie", Card))
    <-  .print("we know that our card is not ", Card, ". wow!").

+know(know(hand("Charlie", "88")))
    <-  .print("we know that we know our card is 88").
