
// Use rules to get possible locations from percepts
location(0, 0) :- left(none) & right(none) & up(none) & down(none).
location(1, 0) :- left(none) & right(none) & up(none) & down(none).
location(2, 0) :- left(none) & right(none) & up(none) & down(none).
location(3, 0) :- left(none) & right(none) & up(none) & down(none).
location(4, 0) :- left(none) & right(none) & up(none) & down(none).

location(0, 1) :- left(none) & right(none) & up(none) & down(none).
location(1, 1) :- left(none) & right(none) & up(none) & down(obstacle). // block
location(2, 1) :- left(none) & right(none) & up(none) & down(obstacle). // block
location(3, 1) :- left(none) & right(none) & up(none) & down(none).
location(4, 1) :- left(none) & right(none) & up(none) & down(none).

location(0, 2) :- left(none) & right(obstacle) & up(none) & down(none).
//location(1, 2) :- left(none) & right(none) & up(none) & down(none). (Not possible because has obstacle)
//location(2, 2) :- left(none) & right(none) & up(none) & down(none). (Not possible because has obstacle)
location(3, 2) :- left(obstacle) & right(none) & up(none) & down(none).
location(4, 2) :- left(none) & right(none) & up(none) & down(none).

location(0, 3) :- left(none) & right(none) & up(none) & down(none).
location(1, 3) :- left(none) & right(none) & up(obstacle) & down(none).
location(2, 3) :- left(none) & right(none) & up(obstacle) & down(none).
location(3, 3) :- left(none) & right(none) & up(none) & down(none).
location(4, 3) :- left(none) & right(none) & up(none) & down(none).

location(0, 4) :- left(none) & right(none) & up(none) & down(none).
location(1, 4) :- left(none) & right(none) & up(none) & down(none).
location(2, 4) :- left(none) & right(none) & up(none) & down(none).
location(3, 4) :- left(none) & right(none) & up(none) & down(none).
location(4, 4) :- left(none) & right(none) & up(none) & down(none).

possible(Locations) :- .setof(location(X, Y), location(X,Y), Locations).

// Percepts
//left(none).
//right(none).
//up(none).
//down(block).

+moved
    :   possible(Locations)
    <-  .print("Possible Locations: ", Locations);
        updatePossible(Locations).



// Where the framework really helps is when we associate information with each location (such as the nearest block)
// Having multiple levels of unknown (partially observable) information contained within a world is where it helps!
//block(right) :- location(2, 1).
//block(left) :- location(2, 1).
//block(down) :- location(0, 1) | location(3, 1).

+!localize
    :   possible(Locations) &
        block(Direction)
    <-  .print(Locations, " nearest block: ", Direction).

