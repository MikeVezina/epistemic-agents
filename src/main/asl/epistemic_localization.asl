obj(obstacle).
obj(none).

world(location(X, Y), up(Up), down(Down), left(Left), right(Right))[world] :- locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).

mapLocation(X, Y) :- locPercept(location(X, Y),_, _, _, _).
//
//mapLocation(0,0).
//mapLocation(0,1).
//mapLocation(1,0).
//mapLocation(1,1).

// The following rules define the values contained in all worlds
// Each world has a separate location and perceptions
// These are more to do with model validation than anything else...
// These really shouldn't be used to generate the model because the algorithm is too complex
kb::locationInfo(X, Y, Up, Down, Left, Right)[prop] :- locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).
//kb::up(Obj)[prop] :- obj(Obj).
//kb::down(Obj)[prop] :- obj(Obj).
//kb::left(Obj)[prop] :- obj(Obj).
//kb::right(Obj)[prop] :- obj(Obj).





// Filter out generated worlds that are not part of the model
// i.e. only the location perceptions that belong to the map should be kept!
//kb::is_valid(location(X, Y), up(Up), down(Down), left(Left), right(Right))[keepOnly]
//    :- locPercept(location(X, Y),left(Left),right(Right),up(Up),down(Down)).

+moved
    : .setof(location(X, Y), possible(location(X, Y)), PossibleLocs)
    <- .print("I Moved: ", PossibleLocs).

+possible(location(X, Y))
    <- .print("I know the location is possibly: ", [X, Y]).