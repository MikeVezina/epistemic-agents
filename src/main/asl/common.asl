// MASSiM Simulation Beliefs and utilities
thingType(entity).
thingType(block).
thingType(dispenser).

/* This is where we include action and plan failures */
+!performAction(ACTION) <-
	.print("Sending action: ", ACTION);
	ACTION;
	.wait("+percept::step(X)").
	