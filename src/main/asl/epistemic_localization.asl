
/********************/
/* Model Generation */
/********************/

// Creates one world per unification
// I.e. one world for each location and its respective perceptions
// The annotation world(Location) uses Location as an identifier for the world (helps with complexity of generation)
world(location(X, Y), left(Left), right(Right), up(Up), down(Down))[world(location(X,Y))]
    :-  locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).

// Appends additional literal/propositions to existing worlds
// The annotation append(Location) only appends to worlds with Location as an ID
// One rule for each adjacent direction
world(adjacent(up, location(DirX, DirY)))[append(location(X,Y))]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(up, location(DirX, DirY)), AdjacentLocs).

world(adjacent(down, location(DirX, DirY)))[append(location(X,Y))]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(down, location(DirX, DirY)), AdjacentLocs).

world(adjacent(left, location(DirX, DirY)))[append(location(X,Y))]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
.member(adjacent(left, location(DirX, DirY)), AdjacentLocs).

world(adjacent(right, location(DirX, DirY)))[append(location(X,Y))]
:-  locAdjacent(location(X, Y), AdjacentLocs) &
    .member(adjacent(right, location(DirX, DirY)), AdjacentLocs).

/************************/
/* END Model Generation */
/************************/


// The following plan runs when the agent moves and perceptions are updated
+moved
    <-  .print("I Moved.");
        !updateAdjacent; // Update the adjacent locations based on movement from our previous locations to further eliminate worlds
        !updateGUIPossible. // Now we can update the GUI with locations that are possible/known


+!updateAdjacent
    :  not previousPossible(PrevList) // Get previous locations
    <- !updatePrevious. // Set new previous locations


+!updateAdjacent
    :  previousPossible(PrevList) & // Get previous locations
       lastMove(MoveDir)
    <-  .abolish(adjacent(_, _)); // Remove existing adjacent knowledge (it is no longer relevant for our new location)
        .print("Moved (", MoveDir, ") from: ", PrevList);
        for(.member(Prev, PrevList))
        {
            +adjacent(MoveDir, Prev);
        };
        !updatePrevious. // Now that we no longer need the current previous locations, we update the list of possibilities

+!updateGUIPossible
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
    <-  .print("Possible Locations: ", Possible); // Print to agent log
        internal.update_possible(Possible). // Update GUI positions

+!updatePrevious
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).


//
//+know(location(X, Y))
//    <- .print("I know the location is: ", [X, Y]).

//+possible(location(X, Y))
//    : not know(location(X, Y))
//    <- .print("I know the location is possibly: ", [X, Y]).
// We use separate beliefs for percept directions so that previous/stale adj. locations do not mess with worlds.
//+movedOld
//    :   right(Right) &
//        down(Down) &
//        up(Up) &
//        left(Left)
//    <- .print("I Moved.");
//        .abolish(percept(_, _)); // Remove existing knowledge
//        .abolish(adjacent(_, _)); // Remove existing adjacent knowledge before reasoning
//        +percept(up, Up);
//        +percept(down, Down);
//        +percept(right, Right);
//        +percept(left, Left);
//        !updateKnowledge.
// Abolish all adjacent(Dir,Locs)
// Get all adjacent(Dir, Locs) ... i.e. adjacent from previous possible locs
