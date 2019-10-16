
+!increaseClearCount(X, Y, NEW_COUNT)
    :   calculateAbsolutePosition(relative(X, Y), absolute(A_X, A_Y)) &
        clearCount(A_X, A_Y, NUM) &
        (NEW_COUNT = NUM + 1)
    <-  -clearCount(A_X, A_Y, NUM);
        +clearCount(A_X, A_Y, NEW_COUNT).

+!increaseClearCount(X, Y, NEW_COUNT)
    :   calculateAbsolutePosition(relative(X, Y), absolute(A_X, A_Y)) &
        not(clearCount(A_X, A_Y, _))
    <-  +clearCount(A_X, A_Y, 1);
        NEW_COUNT = 1.

+percept::disabled(VAL)
    <-  .print("Agent is currently disabled(", VAL ,")").

+!clear(X, Y)
    <-  !performAction(clear(X, Y)).

+handleActionResult(clear, [X, Y], success)
    <-  !increaseClearCount(X, Y, NEW_COUNT).
