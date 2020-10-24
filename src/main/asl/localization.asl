// Import the map location rules that give possible locations
{ include("map_locations.asl") }

goal(0,1).

direction(left) :- location(X, _) & goal(GoalX,_) & X > GoalX.
direction(right) :- location(X, _) & goal(GoalX,_) & X < GoalX.
direction(up) :- location(_, Y) & goal(_,GoalY) & Y > GoalY.
direction(down) :- location(_, Y) & goal(_,GoalY) & Y < GoalY.
direction(none) :- location(X, Y) & goal(X, Y) & Y < GoalY.


// Unify variable 'Locations' with a set/list of all location rules that are true
// i.e. unify Locations to a list of [location(0,0), location(1,1), ...]
possible(Locations) :- .setof(location(X, Y), location(X,Y), Locations).

possibleAdj(LocOne, Adjacents) :- .setof(LocTwo, isAdjacent(LocOne,LocTwo), Adjacents).

// Get the directions we should travel in given our possible locations.
possibleDirections(Directions) :- .setof(direction(Dir), direction(Dir), Directions).


// Cross-reference previous possible adjacent locations with current possible locations
// I.e. if previous possible locations are (0,0) and (1,1) and we move, then this means
// our new possible locations must be in the set (0,1), (1,0), (1,2), (2,1).
getNewPossible(NewPossible) :-
    .setof(Cur,
    previousPossible(PrevList) &
    possible(CurPossible) &
    .member(Prev, PrevList) &
    .member(Cur, CurPossible) &
    isAdjacent(Prev, Cur),
    NewPossible).

// Plans:
// 'moved' == When the GUI receives agent input
+moved
    :   possible(Locations) & // Get possible locations using rule above
        possibleDirections(Directions)
    <-  .print("Possible Locations: ", Locations); // Print list of possible locations
        .print("Possible Directions: ", Directions);
        !updatePossible(Locations);
        !travelToGoal.

+!updatePossible(Possible)
    :  not previousPossible(_)
    <-  +previousPossible(Possible);
        updatePossible(Possible). // Update GUI with list of possible locations

+!updatePossible(Possible)
    :   previousPossible(_) &
        getNewPossible(NewPossible)
    <-  .abolish(previousPossible(_));
        .print("New: ", NewPossible);
        updatePossible(NewPossible); // Update GUI with list of possible locations
        +previousPossible(NewPossible).

+!updatePossible(Possible)
    :  previousPossible(PrevPossible)
    <-  +previousPossible(Possible).

+!travelToGoal
    :   possibleDirections(Directions) & .length(Directions, 1) &
        Directions = [direction(Dir)|_]
    <-  .print("Moving in only direction: ", Dir);
        .wait(500);
        move(Dir).

+!travelToGoal.