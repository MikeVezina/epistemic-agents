// Import the map location rules that give possible locations
{ include("map_locations.asl") }

/******

Change adjacent directions!

******/

// Unify variable 'Locations' with a set/list of all location rules that are true
// i.e. unify Locations to a list of [location(0,0), location(1,1), ...]
possible(Locations) :- .setof(location(X, Y), location(X,Y), Locations).

// Get the directions we should travel in given our possible locations.
possibleDirections(Locs, Directions) :- .setof(direction(Dir), direction(Locs, Dir), Directions).
possibleDirections(Directions) :- .setof(direction(Dir), currentPossible(Locs) & direction(Locs, Dir), Directions).

// Cross-reference previous possible adjacent locations with current possible locations
// I.e. if previous possible locations are (0,0) and (1,1) and we move, then this means
// our new possible locations must be in the set (0,1), (1,0), (1,2), (2,1).
getNewPossible(NewPossible) :-
    .setof(Cur,
    previousPossible(PrevList) &
    possible(CurPossible) &
    .member(Prev, PrevList) &
    .member(Cur, CurPossible) &
    lastMove(MoveDir) &
    MoveDir \== none & // We moved somewhere
    isAdjacent(Prev, MoveDir, Cur),
    NewPossible).

currentPossible(Possible) :- previousPossible(_) & getNewPossible(Possible).
currentPossible(Possible) :- not previousPossible(_) & possible(Possible).

knowLocation :- getNewPossible(NewPossible) & .length(NewPossible, 1).

// Plans:
// 'moved' == When the GUI receives agent input
+moved
    :   currentPossible(Possible) &
        commonDirections(Dirs)
    <-  internal.update_possible(Possible); // Update GUI with list of possible locations
        .print("Possible Locations (Cross): ", Possible);
        .print("Possible Locations (Cross): ", Dirs);
        !runAgent(Possible);
        !updatePrevious(Possible).

+!runAgent(Possible)
    <- !travelToGoal(Possible).

-!runAgent(Possible)[error(E)]
    <-  !updatePrevious(Possible);
        .print("Failed to run agent");
        .fail(E).


+!updatePrevious(Possible)
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).

+moved
    <- .print("Hello?").

+!travelToGoal(_)
    : not autoMove.

// When we DONT know our location, but all possible locations share the same direction
+!travelToGoal(_)
    :   autoMove &
        not knowLocation &
        commonDirections(Directions) & // Find common directions
        .length(Directions, Len) & Len > 0
    <-  .print("Mutual Direction of all possibilities (Location Not Known): ", Directions);
        internal.update_best_move(Directions).


// When we DO know our location -> follow any direction
+!travelToGoal(Poss)
    :   autoMove &
        knowLocation &
        commonDirections(Directions) & // Find any direction
        .member(Dir, Directions) // Choose any member
    <-  .print("Suggesting movement from a known position: ", Dir, " from ", Directions);
        internal.update_best_move(Directions).


+!travelToGoal(_)
    :   commonDirections(Directions) &
        .length(Directions, 0)
    <- .print("No where to go!");
        internal.update_best_move(none).

+!travelToGoal(_)
    :   commonDirections(Directions)
    <- .print("Non Optimal Position directions:", Directions);
        internal.update_best_move(inconclusive).