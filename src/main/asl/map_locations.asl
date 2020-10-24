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

//
//isLocationOnMap(0, 0).
//isLocationOnMap(0, 1).
//isLocationOnMap(0, 2).
//isLocationOnMap(0, 3).
//isLocationOnMap(0, 4).
//isLocationOnMap(1, 0).
//isLocationOnMap(1, 1).
//isLocationOnMap(1, 2).
//isLocationOnMap(1, 3).
//isLocationOnMap(1, 4).
//isLocationOnMap(2, 0).
//isLocationOnMap(2, 1).
//isLocationOnMap(2, 2).
//isLocationOnMap(2, 3).
//isLocationOnMap(2, 4).
//isLocationOnMap(3, 0).
//isLocationOnMap(3, 1).
//isLocationOnMap(3, 2).
//isLocationOnMap(3, 3).
//isLocationOnMap(3, 4).
//isLocationOnMap(4, 0).
//isLocationOnMap(4, 1).
//isLocationOnMap(4, 2).
//isLocationOnMap(4, 3).
//isLocationOnMap(4, 4).

// isLocationOnMap(location(X, Y)) :- isLocationOnMap(X, Y).


// Capture symmetric
isAdjacent(LocOne, LocTwo) :-
    modelObject(ModelObj) &
    internal.is_adjacent(ModelObj, LocOne, LocTwo).