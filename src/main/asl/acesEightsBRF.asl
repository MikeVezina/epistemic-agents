hand("Alice", Alice)
    :-  hand("Bob", "AA") &
        hand("Charlie", "AA") &
        Alice = "88".

hand("Alice", Alice)
    :-  hand("Bob", "88") &
        hand("Charlie", "88") &
        Alice = "AA".

hand("Bob", "AA").
hand("Charlie", "AA").


+hand("Alice", C)
    <- .print("We know our cards! ", C).



// Approach 2: Inferring via goals
//+hand(Player, Cards)
//    :   Player \== "Alice" &
//        otherPlayer(Player, Other) &
//        hand(Other, Cards) &
//        aa(Cards)
//    <-  +hand("Alice", "88").
//
//+hand(Player, Cards)
//    :   Player \== "Alice" &
//        otherPlayer(Player, Other) &
//        hand(Other, Cards) &
//        ee(Cards)
//    <-  +hand("Alice", "AA").
//
//+hand("Alice", C)
//    <- .print("We know our cards! ", C).