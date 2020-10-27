// If only I could express "elimination of impossible scenarios/worlds" in Jason.

/*                         */
/*                         */
/* 1. Specifying the model */

// Jason Approach: (Probably can be cleaned up into single plan)
hand("Alice", "AA").
hand("Alice", "A8").
hand("Alice", "88").

hand("Bob", "AA").
hand("Bob", "A8").
hand("Bob", "88").

hand("Charlie", "AA").
hand("Charlie", "A8").
hand("Charlie", "88").

// Epistemic Approach: Model Generation
//...

/*                         */
/*                         */
/* 2. Model Consistency    */

// This should be a BRF, since it checks for BB consistency
// If we add a hand(Player, Card), check to make sure there are a max of 4 aces/4 eights
// i.e. can't have AA, AA, AA in the BB

// Jason Approach       - Programmer has to specify custom BRF
// Epistemic Approach   - BRF is done automatically with epistemic framework based on Worlds


// We have to manually express knowledge relation
// I.e. if Alice and Bob both have AA, then Charlie has 88

+hand(_, _)
    :   hand("Alice", AliceCard) &
        hand("Bob", BobCard) &
        hand("Charlie", CharlieCard)


















//// world(identifier, list of ground literals)
////world("block_0_1", [location(0, 1), block]).
////world("none_0_1", [location(0, 1), none]).
//
//world(location(0,1), block). // Creates one world with valuation {location_0_1, block}
//world(location(0,1), [block, none]). // Creates three world with valuations: {location_0_1, block}, {location_0_1, none}, {location_0_1, none, block}
//
//// Ex 3
//world([location(0,1), location(0,2)], [block, none]).
//// Creates six world with valuations:
//// {location_0_1, block}, {location_0_1, none}, {location_0_1, none, block}
//// {location_0_2, block}, {location_0_2, none}, {location_0_2, none, block}
//
//create(2).
//
//
//
//
//// Issue: perceptions will happen before plan runs and model is created
////!createWorlds.
////
////!createWorlds
////    <- !createLocationWorlds(0, 1).
////
////!createLocationWorlds(X, Y)
////    <-  +world("block_0_1", [location(0, 1), block]);
////        +world("none_0_1", [location(0, 1), none]);
////        .create_model.
//
//
//
//
//kb::item(block).
//kb::item(none).
//
//// Rules to specify mutually exclusive locations
//// 1. Perception Locations
//kb::location(0, 1, Item)[prop] :- kb::item(Item).
//kb::location(0, -1, Item)[prop] :- kb::item(Item).
//
//// kb::location(1, 0, Item)[prop] :- kb::item(Item).
//// kb::location(-1, 0, Item)[prop] :- kb::item(Item).
//
//// Unknown locations (Outside percepts)
//// kb::location(-1, 1, Item)[prop] :- kb::item(Item).
//// kb::location(-1, -1, Item)[prop] :- kb::item(Item).
//// kb::location(1, -1, Item)[prop] :- kb::item(Item).
//// kb::location(1, 1, Item)[prop] :- kb::item(Item).
//
//!testPlan(X, Y).
//
//+know(location(X, Y, Item)) <- .print("The item at (", X, ",", Y, ") must be ", Item).
//
//+!testPlan[world]
//   : kb::Vart
//   <- .print(": ", Vart).
//
//+!testPlan(X, Y)
//   <- .print(": ", X, " ", Y).
//
//
//
//// Todo: Not XOR values
//// Todo: Maybe introduce as revision rules?
