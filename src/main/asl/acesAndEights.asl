
//+possible(alice("AA"), bob("AA"), charlie("AA")). => Not possible
//+possible(alice("AA"), bob("AA"), charlie("A8")). => Not possible
possible(alice("AA"), bob("AA"), charlie("88")).

//+possible(alice("AA"), bob("A8"), charlie("AA")). => Not possible
possible(alice("AA"), bob("A8"), charlie("A8")).
possible(alice("AA"), bob("A8"), charlie("88")).

possible(alice("AA"), bob("88"), charlie("AA")).
possible(alice("AA"), bob("88"), charlie("A8")).
possible(alice("AA"), bob("88"), charlie("88")).


//!hello.

//!playGame.

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