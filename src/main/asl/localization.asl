// Use rules to get possible locations from percepts
// Encode each location with their percepts (i.e. this is synonymous with creating world valuations)
location(0, 0) :- left(none) & right(none) & up(none) & down(none).
location(1, 0) :- left(none) & right(none) & up(none) & down(none).
location(2, 0) :- left(none) & right(none) & up(none) & down(none).
location(3, 0) :- left(none) & right(none) & up(none) & down(none).
location(4, 0) :- left(none) & right(none) & up(none) & down(none).

location(0, 1) :- left(none) & right(none) & up(none) & down(none).
location(1, 1) :- left(none) & right(none) & up(none) & down(obstacle). // block below
location(2, 1) :- left(none) & right(none) & up(none) & down(obstacle). // block below
location(3, 1) :- left(none) & right(none) & up(none) & down(none).
location(4, 1) :- left(none) & right(none) & up(none) & down(none).

location(0, 2) :- left(none) & right(obstacle) & up(none) & down(none). // block to our right
// location(1, 2) -> Commented out because this is an obstacle position, so it is not possible for the agent to be here
// location(2, 2) -> Commented out because this is an obstacle position, so it is not possible for the agent to be here
location(3, 2) :- left(obstacle) & right(none) & up(none) & down(none). // block to our left
location(4, 2) :- left(none) & right(none) & up(none) & down(none).

location(0, 3) :- left(none) & right(none) & up(none) & down(none).
location(1, 3) :- left(none) & right(none) & up(obstacle) & down(none). // block on top of us
location(2, 3) :- left(none) & right(none) & up(obstacle) & down(none). // block on top of us
location(3, 3) :- left(none) & right(none) & up(none) & down(none).
location(4, 3) :- left(none) & right(none) & up(none) & down(none).

location(0, 4) :- left(none) & right(none) & up(none) & down(none).
location(1, 4) :- left(none) & right(none) & up(none) & down(none).
location(2, 4) :- left(none) & right(none) & up(none) & down(none).
location(3, 4) :- left(none) & right(none) & up(none) & down(none).
location(4, 4) :- left(none) & right(none) & up(none) & down(none).


// Unify variable 'Locations' with a set/list of all location rules that are true
// i.e. unify Locations to a list of [location(0,0), location(1,1), ...]
possible(Locations) :- .setof(location(X, Y), location(X,Y), Locations).


// Plans:
// 'moved' == When the GUI receives agent input
+moved
    :   possible(Locations) // Get possible locations using rule above
    <-  .print("Possible Locations: ", Locations); // Print list of possible locations
        updatePossible(Locations). // Update GUI with list of possible locations