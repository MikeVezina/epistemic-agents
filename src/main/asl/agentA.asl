{ include("common.asl") }
{ include("internal_actions.asl") }

{ include("tasks/tasks.asl") }
{ include("tasks/requirements.asl") }

{ include("nav/navigation.asl", nav) }

	
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

/***** Initial Goals ******/
!getPoints.

/***** Plan Definitions ******/
// TODO: Action failures, See: http://jason.sourceforge.net/faq/#_which_information_is_available_for_failure_handling_plans


+!attachBlock(X, Y)
    :   hasDispenser(X, Y, _) &
        nav::xyToDirection(DIR, X, Y)
    <-  !performAction(request(DIR));
        !performAction(attach(DIR)).




/*** Task Requirement Selection Plans***/
+!selectRequirements(task(_, _, _, REQS), REQ)
    :   not(checkRequirementMet(REQS)) &
        selectRequirement(REQ, REQS).

-!selectRequirements(TASK, REQ)
    <-  .print("Failed to select task requirements.");
        .fail.



/** Task Submission Plans **/
+!submitTask(task(NAME, _, _, _))
    <- !performAction(submit(NAME)).

/** Main Task Plan **/
+!getPoints
    <-  !selectTask(TASK);
        !!selectedTask(TASK);
        !selectRequirements(TASK, REQ);
        (req(R_X, R_Y, BLOCK) = REQ);
        .print("Current Task: ", TASK, ", Requirement: ", REQ);
        !nav::searchForThing(dispenser, BLOCK);
        !nav::alignDispenser(BLOCK, R_X, R_Y);
        !attachBlock(R_X, R_Y);
        !nav::navigateToGoal;
        !submitTask(TASK);
        !getPoints.



