{ include("common.asl") }
{ include("internal_actions.asl") }
{ include("auth/team.asl") }

{ include("tasks/tasks.asl") }
{ include("tasks/requirements.asl") }

{ include("nav/navigation.asl", nav) }
{ include("internal_actions.asl") }


	
/* MVP:
 * Iteration 0: No obstacles, deadlines are irrelevant, only one agent on the map, all important locations are hard-coded (dispenser, goals)


 * NOTE: Navigation module can be stubbed out to hard-code positions for everything
 * NOTE: These steps require the agent to remember it's location. The simulator does not provide location perception.

 * Steps:

 * 1. If Task Exists -> Parse Task for requirements
 * 		        Else -> Navigation: Survey Surroundings -> Repeat Step 1.


 * 2. For Each Required Block:
 *      If we have belief of block dispenser location -> Navigation: GoTo Belief Location
 *                                              Else  -> Navigation: Search For Required Block Dispenser
 * 		(Request) Block
 * 		(Attach) Requirement to Agent

 * 3. If we have a belief of the goal location -> Navigation: GoTo Goal Location
 *                                        else -> Navigation: Search for Goal Location

 *
 * 
 */
// Operator Agent Belief
operator(operator).




/***** Initial Goals ******/
// None right now. We wait for the simulation to start.

+!clear
    <-  !performAction(clear(0,-1));
        ?percept::thing(X,Y,marker,DET);
        .print("Marker Percept: ", X, ", ", Y, ", ", DET).

+percept::simStart
    <-  .print("Waiting on Requirement.").
        //!coordinate.

+percept::step(X)
    : percept::lastActionResult(RES) & percept::lastAction(ACT) & ACT \== no_action & percept::lastActionParams(PARAMS)
    <-  .print("Action: ", ACT, PARAMS, ". Result: ", RES).


/***** Plan Definitions ******/
// TODO: Action failures, See: http://jason.sourceforge.net/faq/#_which_information_is_available_for_failure_handling_plans


+!requestBlock(X, Y)
    :   hasDispenser(X, Y, _) &
        xyToDirection(DIR, X, Y)
    <-  !performAction(request(DIR));
        !performAction(attach(DIR)).


+!printReward
    :   percept::score(X)
    <-  .print("Current Score is: ", X).

/** Main Task Plan **/
+!getPoints
    <-  .print("Selecting a Task.");
        !selectTask(TASK);
        .print("Selected Task: ", TASK);
        !achieveTask.

+!achieveTask
    :   not(taskRequirementsMet)
    <-  !achieveNextRequirement.

+!achieveTask
    :   taskRequirementsMet
    <-  !nav::navigateToGoal;
        !submitTask;
        !printReward;
        .print("Finished");
        !getPoints.

+!achieveNextRequirement
    <-  !selectRequirements(REQ);
        .print("Selected Requirement: ", REQ);
        !achieveRequirement(REQ).

+!achieveRequirement(TASK, req(R_X, R_Y, BLOCK))[source(SRC)]
    <-  !nav::obtainBlock(BLOCK);
        .send(SRC, tell, obtained(TASK, BLOCK)).

//        ?nav::isAttachedToCorrectSide(R_X, R_Y, BLOCK).

