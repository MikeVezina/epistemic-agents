
//!hello.

!playGame.

+!playGame
    :   turn &
        action(Action)
    <-  .print("My Turn: ", Action);
        .wait(700);
        announce(Action);
        !playGame.

+!playGame <- !playGame.

//+!hello
//    : k_box(alice, aa)
//    <- .print("Alice knows AA");
//        announce(k_box(alice, aa)).
//
//+!hello
//    : ~knows(alice(aa))
//    <- .print("not Alice knows AA").
//
//+!hello
//    <- .print("Alice not sure AA").