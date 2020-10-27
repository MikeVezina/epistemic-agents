{ include("generated_map.asl") }


adjacentCell(Src, Direction, Dest).

// Provide rule to map term to percept
visual(down, Percept) :- down(Percept).
visual(up, Percept) :- up(Percept).
visual(right, Percept) :- right(Percept).
visual(left, Percept) :- left(Percept).


// Finds locations that match perceptions
location(X, Y) :-
    left(Left) & right(Right) & up(Up) & down(Down) &
    locPercept(location(X,Y), left(Left), right(Right), up(Up), down(Down)).


// Capture adjacent cells
isAdjacent(LocOne, LocTwo) :-
    locAdjacent(LocOne, AdjList) &
    .member(adjacent(_, LocTwo), AdjListdd).

// Check if LocTwo is Dir from LocOne
// i.e. if (1,2) is right from (0,2)
isAdjacent(LocOne, Dir, LocTwo) :-
    locAdjacent(LocOne, AdjList) &
    .member(adjacent(Dir, LocTwo), AdjList).

// Get a possible direction given
direction(Locations, Dir) :-
    .member(Loc, Locations) & // Get a location from the provided list and bind it to a direction
    locDirToGoal(Loc, DirList) &
    .member(Dir, DirList) &
    visual(Dir, none).// Direction not blocked

commonDirections(Locations, Result) :-
    .setof(DirList, .member(Loc, Locations) & locDirToGoal(Loc, DirList), Dirs) &
    internal.merge_sets(Dirs, Result). // Merges sub-sets into result.

commonDirections(Result) :-
    currentPossible(Locs) &
    commonDirections(Locs, Result).