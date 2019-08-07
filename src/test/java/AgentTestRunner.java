import jason.architecture.AgArch;
import jason.architecture.MindInspectorWeb;
import jason.asSemantics.*;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import jason.asSyntax.Trigger;
import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.infra.centralised.CentralisedAgArch;
import jason.infra.repl.ReplAgGUI;
import jason.runtime.Settings;
import jason.runtime.SourcePath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentTestRunner extends AgArch implements GoalListener {
    private Agent agent;
    private Logger LOG;
    private boolean isAgentRunning;

    public static void main(String[] a) throws Exception {
        AgentTestRunner ag = new AgentTestRunner("C:\\Users\\Michael Vezina\\eclipse-workspace\\massim2019\\src\\main\\asl\\", "C:\\Users\\Michael Vezina\\eclipse-workspace\\massim2019\\src\\test\\asl\\testAgentA.asl");
        ag.run();
        ag.stop();
    }

    public AgentTestRunner(String aslSourceRoot, String testSource) throws Exception {

        agent = new Agent();
        new TransitionSystem(agent, new Circumstance(), new Settings(), this);
        SourcePath sourcePath = new SourcePath();
        sourcePath.setRoot(aslSourceRoot);
        sourcePath.addPath(aslSourceRoot);

        ((Include) DirectiveProcessor.getDirective("include")).setSourcePath(sourcePath);
        agent.getTS().addGoalListener(this);
        isAgentRunning = true;

        agent.initAg(testSource); // demo.asl is the file containing the code of the agent
        LOG = agent.getLogger();
    }

    public String getAgName() {
        return "agentA";
    }

    public void stop()
    {
        isAgentRunning = false;
        wake(); // so that it leaves the run loop
        getTS().getAg().stopAg();
        super.stop(); // stops all archs
        MindInspectorWeb.stop();
        try {
            this.getRuntimeServices().stopMAS();
        } catch (Exception e) {
        }

    }

    public void run() {
        while (isRunning()) {
            // calls the Jason engine to perform one reasoning cycle
            getTS().reasoningCycle();
        }
    }

    // this method just add some perception for the agent
    public List<Literal> perceive() {
        return new ArrayList<>();
    }

    // this method gets the agent actions
    public void act(ActionExec action) {
        LOG.info("Agent " + getAgName() + " is doing: " + action.getActionTerm());
        // return confirming the action execution was OK
        action.setResult(true);
        actionExecuted(action);
    }

    public boolean canSleep() {
        return true;
    }

    public boolean isRunning() {
        return isAgentRunning;
    }

    public void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    }

    public void broadcast(jason.asSemantics.Message m) throws Exception {
    }

    public void checkMail() {
    }

    @Override
    public void goalStarted(Event goal) {

    }

    @Override
    public void goalFinished(Trigger goal, FinishStates result) {

    }

    @Override
    public void goalFailed(Trigger goal) {
        this.stop();
        LOG.severe("Failed to Assert Goal: " + goal.getLiteral());

    }

    @Override
    public void goalSuspended(Trigger goal, String reason) {

    }

    @Override
    public void goalResumed(Trigger goal) {

    }
}
