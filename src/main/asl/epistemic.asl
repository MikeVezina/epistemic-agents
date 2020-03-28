/*** ================================================= ***/
/*** Step 1: Introduce all worlds (with "closed" eyes) ***/
/*** ================================================= ***/
// NOTE 1: How do we simplify this while also making it general to any domain?
// NOTE 2: May be able to use domain ontology to generate possible worlds
//+not(possible(_, _, _))
//    <-
//+possible(alice("AA"), bob("AA"), charlie("AA")). => Not possible
//+possible(alice("AA"), bob("AA"), charlie("A8")). => Not possible
+possible(alice("AA"), bob("AA"), charlie("88")).

//+possible(alice("AA"), bob("A8"), charlie("AA")). => Not possible
+possible(alice("AA"), bob("A8"), charlie("A8")).
+possible(alice("AA"), bob("A8"), charlie("88")).

+possible(alice("AA"), bob("88"), charlie("AA")).
+possible(alice("AA"), bob("88"), charlie("A8")).
+possible(alice("AA"), bob("88"), charlie("88")).

        // ... This is only 1/3 of all worlds ...


/*** ======================================== ***/
/*** Step 2: Perceive the world ("open eyes") ***/
/*** ======================================== ***/
// The following are introduced as perceptions
// These determine the accessibility / indistinguishability relation.
//+alice("AA").
//+bob("AA").


/*** ======================== ***/
/*** Step 3:  What do I know? ***/
/*** ======================== ***/

// This plan will print out charlies cards if we know them
// The reasoner will be invoked to determine if we know charlie's cards
//+!playGame
//    :   charlie(Cards) // NOT in BB so it implies Knows(charlie(Cards))
//    <-  .print("Charlie's cards are ", Cards).
//
//
//// Or we don't know
//+!playGame
//    :   not(charlie(_))
//    <-  .print("Charlie doesn't know.").


// Step 4: (Future Work) Handle multi-agent modalities (with announcements). This requires adding what alice and bob know/don't know.
