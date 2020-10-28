obj(obstacle).
obj(none).

mapLocation(0,0).
mapLocation(0,1).
mapLocation(1,0).
mapLocation(1,1).

// Each world has a separate location
kb::location(X, Y)[prop] :- mapLocation(X, Y).

kb::up(Obj)[prop] :- obj(Obj).
kb::down(Obj)[prop] :- obj(Obj).
kb::left(Obj)[prop] :- obj(Obj).
kb::right(Obj)[prop] :- obj(Obj).


kb::is_valid(location(0, 0)).

+moved
    <- .print("I Moved!").

+possible(location(X, Y))
    <- .print("I know the location is possibly: ", [X, Y]).