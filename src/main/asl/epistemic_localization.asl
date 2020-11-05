/*********************/
/* Better Generation */
/*********************/
// Maybe generate all possible values
// Generates location(0, 0), location(0, 1), ..., location(4, 4).
// is 'all_possible' a better annotation?

// The definitions for what will be contained within the worlds
// Makes it easier for us to know what is part of the worlds before hand
//location(X, Y)[unknown].
//percept(Direction, Object)[unknown].


// if not dependent on another world: create one world per unification
// if dependent on another world: cross-product

// known = 1 unification per world
location(X, Y)[necessary]
    :-  .member(X, [0, 1, 2, 3, 4]) &
        .member(Y, [0, 1, 2, 3, 4]).

// The above can actually be simplified to:
//locPercept(location(2,0),left(none),right(none),up(none),down(none)).
//percept(right, Object)[[ps]] :- location(X, Y) & locPercept(location(X, Y), _, right(Object), _, _).

// possible = conditional on the world valuation
percept(left, block)[necessary] :- location(3, 2).
percept(left, none)[necessary] :- not percept(left, block).

percept(up, block)[necessary] :- location(1, 3) | location(2, 3).
percept(up, none)[necessary] :- not percept(up, block).

percept(down, block)[necessary] :- location(1, 1) | location(2, 1).
percept(down, none)[necessary] :- not percept(down, block).


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

