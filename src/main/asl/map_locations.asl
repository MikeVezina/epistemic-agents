/** Perceptions corresponding to each cell **/
// Map Location Mappings
locPercept(location(0,0),left(none),right(none),up(none),down(none)).
locPercept(location(0,1),left(none),right(none),up(none),down(none)).
locPercept(location(0,2),left(none),right(obstacle),up(none),down(none)).
locPercept(location(0,3),left(none),right(none),up(none),down(none)).
locPercept(location(0,4),left(none),right(none),up(none),down(none)).
locPercept(location(1,0),left(none),right(none),up(none),down(none)).
locPercept(location(1,1),left(none),right(none),up(none),down(obstacle)).
locPercept(location(1,3),left(none),right(none),up(obstacle),down(none)).
locPercept(location(1,4),left(none),right(none),up(none),down(none)).
locPercept(location(2,0),left(none),right(none),up(none),down(none)).
locPercept(location(2,1),left(none),right(none),up(none),down(obstacle)).
locPercept(location(2,3),left(none),right(none),up(obstacle),down(none)).
locPercept(location(2,4),left(none),right(none),up(none),down(none)).
locPercept(location(3,0),left(none),right(none),up(none),down(none)).
locPercept(location(3,1),left(none),right(none),up(none),down(none)).
locPercept(location(3,2),left(obstacle),right(none),up(none),down(none)).
locPercept(location(3,3),left(none),right(none),up(none),down(none)).
locPercept(location(3,4),left(none),right(none),up(none),down(none)).
locPercept(location(4,0),left(none),right(none),up(none),down(none)).
locPercept(location(4,1),left(none),right(none),up(none),down(none)).
locPercept(location(4,2),left(none),right(none),up(none),down(none)).
locPercept(location(4,3),left(none),right(none),up(none),down(none)).
locPercept(location(4,4),left(none),right(none),up(none),down(none)).

// Adjacent Location Mappings
locAdjacent(location(0,0),[adjacent(down,location(0,1)),adjacent(right,location(1,0))]).
locAdjacent(location(0,1),[adjacent(up,location(0,0)),adjacent(down,location(0,2)),adjacent(right,location(1,1))]).
locAdjacent(location(0,2),[adjacent(up,location(0,1)),adjacent(down,location(0,3))]).
locAdjacent(location(0,3),[adjacent(up,location(0,2)),adjacent(down,location(0,4)),adjacent(right,location(1,3))]).
locAdjacent(location(0,4),[adjacent(up,location(0,3)),adjacent(right,location(1,4))]).
locAdjacent(location(1,0),[adjacent(right,location(2,0)),adjacent(down,location(1,1)),adjacent(left,location(0,0))]).
locAdjacent(location(1,1),[adjacent(right,location(2,1)),adjacent(up,location(1,0)),adjacent(left,location(0,1))]).
locAdjacent(location(1,3),[adjacent(right,location(2,3)),adjacent(down,location(1,4)),adjacent(left,location(0,3))]).
locAdjacent(location(1,4),[adjacent(right,location(2,4)),adjacent(up,location(1,3)),adjacent(left,location(0,4))]).
locAdjacent(location(2,0),[adjacent(down,location(2,1)),adjacent(left,location(1,0)),adjacent(right,location(3,0))]).
locAdjacent(location(2,1),[adjacent(up,location(2,0)),adjacent(left,location(1,1)),adjacent(right,location(3,1))]).
locAdjacent(location(2,3),[adjacent(down,location(2,4)),adjacent(left,location(1,3)),adjacent(right,location(3,3))]).
locAdjacent(location(2,4),[adjacent(up,location(2,3)),adjacent(left,location(1,4)),adjacent(right,location(3,4))]).
locAdjacent(location(3,0),[adjacent(right,location(4,0)),adjacent(down,location(3,1)),adjacent(left,location(2,0))]).
locAdjacent(location(3,1),[adjacent(right,location(4,1)),adjacent(down,location(3,2)),adjacent(up,location(3,0)),adjacent(left,location(2,1))]).
locAdjacent(location(3,2),[adjacent(right,location(4,2)),adjacent(down,location(3,3)),adjacent(up,location(3,1))]).
locAdjacent(location(3,3),[adjacent(right,location(4,3)),adjacent(up,location(3,2)),adjacent(down,location(3,4)),adjacent(left,location(2,3))]).
locAdjacent(location(3,4),[adjacent(right,location(4,4)),adjacent(up,location(3,3)),adjacent(left,location(2,4))]).
locAdjacent(location(4,0),[adjacent(down,location(4,1)),adjacent(left,location(3,0))]).
locAdjacent(location(4,1),[adjacent(up,location(4,0)),adjacent(down,location(4,2)),adjacent(left,location(3,1))]).
locAdjacent(location(4,2),[adjacent(down,location(4,3)),adjacent(up,location(4,1)),adjacent(left,location(3,2))]).
locAdjacent(location(4,3),[adjacent(down,location(4,4)),adjacent(up,location(4,2)),adjacent(left,location(3,3))]).
locAdjacent(location(4,4),[adjacent(up,location(4,3)),adjacent(left,location(3,4))]).

// Location Direction Mappings
locDirToGoal(location(0,0),[down]).
locDirToGoal(location(0,1),[down]).
locDirToGoal(location(0,2),[none]).
locDirToGoal(location(0,3),[up]).
locDirToGoal(location(0,4),[up]).
locDirToGoal(location(1,0),[left,down]).
locDirToGoal(location(1,1),[left,down]).
locDirToGoal(location(1,3),[left,up]).
locDirToGoal(location(1,4),[left,up]).
locDirToGoal(location(2,0),[left,down]).
locDirToGoal(location(2,1),[left,down]).
locDirToGoal(location(2,3),[left,up]).
locDirToGoal(location(2,4),[left,up]).
locDirToGoal(location(3,0),[left,down]).
locDirToGoal(location(3,1),[left,down]).
locDirToGoal(location(3,2),[left,down]).
locDirToGoal(location(3,3),[left,up]).
locDirToGoal(location(3,4),[left,up]).
locDirToGoal(location(4,0),[left,down]).
locDirToGoal(location(4,1),[left,down]).
locDirToGoal(location(4,2),[left]).
locDirToGoal(location(4,3),[left,up]).
locDirToGoal(location(4,4),[left,up]).


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

direction(Locations, Dir) :-
    .member(Loc, Locations) & // Get a location from the provided list and bind it to a direction
    locDirToGoal(Loc, DirList) &
    .member(Dir, DirList) &
    visual(Dir, none).// Direction not blocked
