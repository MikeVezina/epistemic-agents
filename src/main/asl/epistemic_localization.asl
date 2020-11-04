// Doesn't work because of single prop val per world (needs to be changed?)
//world(direction(Dir))[append(location(X,Y))]
//:-  locDirToGoal(location(X, Y), Dirs) &
//    .member(Dir, Dirs).

/*********************/
/* Better Generation */
/*********************/
// For the future: Multiple 'unknown' rules are combined using a cross-product??

// Or maybe not cross-product..
// Create different sets of worlds and link them via rules

// Maybe generate all possible values
// Generates location(0, 0), location(0, 1), ..., location(4, 4).
// is 'all_possible' a better annotation?

// The definitions for what will be contained within the worlds
location(X, Y)[unknown].
percept(Direction, Object)[unknown].


// known = 1 location per world
location(X, Y)[known]
    :-  .member(X, [0, 1, 2, 3, 4]) &
        .member(Y, [0, 1, 2, 3, 4]).

// The above can actually be simplified to:
//locPercept(location(2,0),left(none),right(none),up(none),down(none)).
//percept(right, Object)[[ps]] :- location(X, Y) & locPercept(location(X, Y), _, right(Object), _, _).

// possible = conditional on the world valuation
percept(left, block)[possible] :- location(3, 2).
percept(left, none)[known] :- not percept(left, block).

percept(up, block)[known] :- location(1, 3) | location(2, 3).
percept(up, none)[known] :- not percept(up, block).

percept(down, block)[known] :- location(1, 1) | location(2, 1).
percept(down, none)[known] :- not percept(down, block).


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

world(direction(down))[append(location(X,Y))]
:-  locDirToGoal(location(X, Y), Dirs) &
    .member(down, Dirs).

world(direction(up))[append(location(X,Y))]
:-  locDirToGoal(location(X, Y), Dirs) &
    .member(up, Dirs).

world(direction(left))[append(location(X,Y))]
:-  locDirToGoal(location(X, Y), Dirs) &
    .member(left, Dirs).

world(direction(right))[append(location(X,Y))]
:-  locDirToGoal(location(X, Y), Dirs) &
    .member(right, Dirs).

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


// Update the reasoner with knowledge of our adjacent positions
+!updateAdjacent
    :  previousPossible(PrevList) & // Get previous locations
       lastMove(MoveDir)
    <-  .abolish(adjacent(_, _)); // Remove existing adjacent knowledge (it is no longer relevant for our new location)
        .print("Moved (", MoveDir, ") from: ", PrevList);
        for(.member(Prev, PrevList)) { +adjacent(MoveDir, Prev); };// Add adjacent knowledge
        !updatePrevious. // Now that we no longer need the current previous locations, we update the list of possibilities

+!updateGUIPossible
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) & // get all possibilities from reasoner
        .setof(Dir, possible(direction(Dir)), AllDir)
    <-  .print("Possible Locations: ", Possible); // Print to agent log
        .print("Possible Directions: ", AllDir); // Print to agent log
        internal.update_best_move(AllDir);
        internal.update_possible(Possible). // Update GUI positions

+!updatePrevious
    :   .setof(location(X, Y), possible(location(X, Y)), Possible) // get all possibilities from reasoner
    <-  .abolish(previousPossible(_)); // Reset previous possibilities
        +previousPossible(Possible).

