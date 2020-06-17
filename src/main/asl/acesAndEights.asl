// 1. Possible Hands
//raining.
// raining[unknown].
kb::cards("AA").
kb::cards("A8").
kb::cards("88").

//Input (Jason Language -- ground sentences):
//    [hand("Alice","AA") xor hand("Alice","A8") xor hand("Alice","88")]
//        and [hand("Bob","AA") xor hand("Bob","A8") xor hand("Bob","88")]
// Input -> Combine literals for Alice & Bob

// Output (Reasoner Language):
//    {hand_“Alice”_“AA”}, {hand(Alice,"A8")}, {hand(Alice,"88")}

// 2. Prop. rules to specify possible Agent hands
kb::hand("Alice", Hand)[prop] :- kb::cards(Hand).
kb::hand("Bob", Hand)[prop] :- kb::cards(Hand).
kb::hand("Charlie", Hand)[prop] :- kb::cards(Hand).

// OR 3. Invalid hands using Beliefs
kb::~is_valid(hand("Alice", "AA"), hand("Bob", "AA"), hand("Charlie", "AA")).
kb::~is_valid(hand("Alice", "AA"), hand("Bob", "AA"), hand("Charlie", "A8")).
kb::~is_valid(hand("Alice", "AA"), hand("Bob", "A8"), hand("Charlie", "AA")).
kb::~is_valid(hand("Alice", "A8"), hand("Bob", "AA"), hand("Charlie", "AA")).

kb::~is_valid(hand("Alice", "88"), hand("Bob", "88"), hand("Charlie", "88")).
kb::~is_valid(hand("Alice", "88"), hand("Bob", "88"), hand("Charlie", "A8")).
kb::~is_valid(hand("Alice", "88"), hand("Bob", "A8"), hand("Charlie", "88")).
kb::~is_valid(hand("Alice", "A8"), hand("Bob", "88"), hand("Charlie", "88")).

// 3. A rule to help Determine invalid hands.
kb::is_valid(hand("Alice", Alice), hand("Bob", Bob), hand("Charlie", Charlie))
    :- internal.is_valid_hand(Alice, Bob, Charlie).

// Set initial goal to !play
!load[epistemic].

@epistemic_alice[all_unifs]
+!load[epistemic]
    : not hand("Alice", _) & kb::cards(Cards)
    <-  +hand("Alice", Cards).

+!load[epistemic]
    : not hand("Bob", _) & kb::cards(Cards)
    <-  +hand("Bob", Cards).

+!load[epistemic]
    : not hand("Charlie", _) & kb::cards(Cards)
    <-  +hand("Charlie", Cards).


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
