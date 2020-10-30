
// Creates one world per unification
world(location(X, Y), percept(left, Left), percept(right, Right), percept(up, Up), percept(down, Down))[world]
    :-  locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).


// Extend existing worlds that match the first term "location(X, Y)" with all unifications of the second term
// i.e. append terms to existing worlds that match location
world(location(X, Y), adjacent(up, location(DirX, DirY)))[append]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(up, location(DirX, DirY)), AdjacentLocs).

world(location(X, Y), adjacent(down, location(DirX, DirY)))[append]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(down, location(DirX, DirY)), AdjacentLocs).

world(location(X, Y), adjacent(left, location(DirX, DirY)))[append]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
.member(adjacent(left, location(DirX, DirY)), AdjacentLocs).

world(location(X, Y), adjacent(right, location(DirX, DirY)))[append]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(right, location(DirX, DirY)), AdjacentLocs).

// obj(obstacle).
// obj(none).

// mapLocation(X, Y) :- locPercept(location(X, Y),_, _, _, _).
//
//mapLocation(0,0).
//mapLocation(0,1).
//mapLocation(1,0).
//mapLocation(1,1).

// The following rules define the values contained in all worlds
// Each world has a separate location and perceptions
// These are more to do with model validation than anything else...
// These really shouldn't be used to generate the model because the algorithm is too complex
//kb::locationInfo(X, Y, Up, Down, Left, Right)[prop] :- locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).
//kb::up(Obj)[prop] :- obj(Obj).
//kb::down(Obj)[prop] :- obj(Obj).
//kb::left(Obj)[prop] :- obj(Obj).
//kb::right(Obj)[prop] :- obj(Obj).


// Filter out generated worlds that are not part of the model
// i.e. only the location perceptions that belong to the map should be kept!
//kb::is_valid(location(X, Y), up(Up), down(Down), left(Left), right(Right))[keepOnly]
//    :- locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).

opposite(up, down).
opposite(down, up).
opposite(left, right).
opposite(right, left).

// Capture adjacent cells
isAdjacent(LocOne, LocTwo) :-
    locAdjacent(LocOne, AdjList) &
    .member(adjacent(_, LocTwo), AdjList).

// Check if LocTwo is Dir from LocOne
// i.e. if (1,2) is right from (0,2)
isAdjacent(LocOne, Dir, LocTwo) :-
    locAdjacent(LocOne, AdjList) &
    .member(adjacent(Dir, LocTwo), AdjList).


+moved
    :   right(Right) &
        down(Down) &
        up(Up) &
        left(Left)
    <- .print("I Moved.");
        .abolish(percept(_, _)); // Remove existing knowledge
        .abolish(adjacent(_, _)); // Remove existing knowledge
        +percept(up, Up);
        +percept(down, Down);
        +percept(right, Right);
        +percept(left, Left);
        !updateKnowledge.
        // Abolish all adjacent(Dir,Locs)
        // Get all adjacent(Dir, Locs) ... i.e. adjacent from previous possible locs

+!updateKnowledge
    <-  !updateAdjacent;
        !updatePossible(Possible); // Unify Possible here
        !updatePrevious(Possible).

+!updateAdjacent
    :  not previousPossible(PrevList). // Get previous locations

+!updateAdjacent
    :  previousPossible(PrevList) & // Get previous locations
       .setof(location(X, Y), possible(location(X, Y)), Possible) &
       lastMove(MoveDir) & opposite(MoveDir, PrevDir) &
       .setof(adjacent(PrevDir, Prev), .member(Prev, PrevList) & .member(Cur, Possible) & isAdjacent(Prev, MoveDir, Cur), Adjs)
        <-  .print("Adj Update:");
            .print(PrevList);
            .print(Possible);
            .print(PrevDir);
            .print(Adjs);
            for(.member(Adj, Adjs))
            {
                .print(Adj);
                +Adj;
            }.

+!updatePossible(Possible)
    :   .setof(location(X, Y), possible(location(X, Y)), Possible)
    <-  .print("Poss: ", Possible);
        internal.update_possible(Possible).


+know(location(X, Y))
    <- .print("I know the location is: ", [X, Y]).

+possible(location(X, Y))
    : not know(location(X, Y))
    <- .print("I know the location is possibly: ", [X, Y]).

+!updatePrevious(Possible)
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).
