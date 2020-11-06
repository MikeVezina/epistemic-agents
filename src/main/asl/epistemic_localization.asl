/*********************/
/* Better Generation */
/*********************/
location(X, Y)[possibly]
  :-  locPercept(location(X, Y), _).

percept(Direction, Object)[necessary] :- location(X, Y) & locPercept(location(X, Y), Perceptions) & .member(percept(Direction, Object), Perceptions).
adjacent(MoveDir, Prev)[necessary] :- location(X, Y) & locAdjacent(location(X, Y), Adj) & .member(adjacent(MoveDir, Prev), Adj).
direction(Dir)[necessary] :- location(X, Y) & locDirToGoal(location(X,Y), Dirs) & .member(Dir, Dirs).


//percept(up, Object)[necessary] :- location(X, Y) & visual(location(X, Y), up, Object).
//percept(down, Object)[necessary] :- location(X, Y) & visual(location(X, Y), down, Object).
//percept(left, Object)[necessary] :- location(X, Y) & visual(location(X, Y), left, Object).
//percept(right, Object)[necessary] :- location(X, Y) & visual(location(X, Y), right, Object).


// Generate worlds with one location each (except location(2,2) and location(1,2) where obstacles reside)
//location(X, Y)[possibly]
//    :-  .member(X, [0, 1, 2, 3, 4]) &
//        .member(Y, [0, 1, 2, 3, 4]) &
//        not (X == 2 & Y == 2) &
//        not (X == 1 & Y == 2) .
//
//// Generate the perceptions depending on the location
//percept(left, block)[necessary] :- location(3, 2).
//percept(left, none)[necessary] :- not percept(left, block).
//
//percept(up, block)[necessary] :- location(1, 3) | location(2, 3).
//percept(up, none)[necessary] :- not percept(up, block).
//
//percept(down, block)[necessary] :- location(1, 1) | location(2, 1).
//percept(down, none)[necessary] :- not percept(down, block).
//

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

