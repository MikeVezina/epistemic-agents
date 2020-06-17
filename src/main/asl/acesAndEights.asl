kb::item(block).
kb::item(none).

// Rules to specify mutually exclusive locations
// 1. Perception Locations
kb::location(0, 1, Item)[prop] :- kb::item(Item).
kb::location(0, -1, Item)[prop] :- kb::item(Item).
kb::location(1, 0, Item)[prop] :- kb::item(Item).
kb::location(-1, 0, Item)[prop] :- kb::item(Item).

// Unknown locations (Outside percepts)
kb::location(-1, 1, Item)[prop] :- kb::item(Item).
kb::location(-1, -1, Item)[prop] :- kb::item(Item).
kb::location(1, -1, Item)[prop] :- kb::item(Item).
kb::location(1, 1, Item)[prop] :- kb::item(Item).

+~know(hand("Charlie", Card))
    <-  .print("we DONT know if our card is ", Card, " (YET!!)").

+know(~hand("Charlie", Card))
    <-  .print("we know that our card is not ", Card, ". wow!").

+know(know(hand("Charlie", "88")))
    <-  .print("we know that we know our card is 88").
