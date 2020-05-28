package epistemic.agent.mock;

import epistemic.EpistemicDistribution;
import epistemic.agent.EpistemicAgent;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.Settings;

import java.util.Collection;
import java.util.List;

public class MockAgArch extends CentralisedAgArch {

    //private final ChainedEpistemicBB beliefBase;
    private final TransitionSystem mockTs;
    private final MockEpistemicAgent epistemicAgent;

    public MockAgArch() {
        super();
        this.epistemicAgent = new MockEpistemicAgent();
        this.mockTs = new TransitionSystem(epistemicAgent, new Circumstance(), new Settings(), this);
        this.epistemicAgent.setTS(mockTs);
        this.setTS(mockTs);

        this.epistemicAgent.loadAgent();
        this.epistemicAgent.getPL();
    }

    @Override
    public void init() throws Exception {
        super.init();

    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public AgArch getFirstAgArch() {
        return super.getFirstAgArch();
    }

    @Override
    public AgArch getNextAgArch() {
        return super.getNextAgArch();
    }

    @Override
    public List<String> getAgArchClassesChain() {
        return super.getAgArchClassesChain();
    }

    @Override
    public void insertAgArch(AgArch arch) {
        super.insertAgArch(arch);
    }

    @Override
    public void createCustomArchs(Collection<String> archs) throws Exception {
        super.createCustomArchs(archs);
    }

    @Override
    public void reasoningCycleStarting() {
        super.reasoningCycleStarting();
    }

    @Override
    public void reasoningCycleFinished() {
        super.reasoningCycleFinished();
    }

    @Override
    public TransitionSystem getTS() {
        return super.getTS();
    }

    @Override
    public void setTS(TransitionSystem ts) {
        super.setTS(ts);
    }

    @Override
    public void actionExecuted(ActionExec act) {
        super.actionExecuted(act);
    }

    @Override
    public void setCycleNumber(int cycle) {
        super.setCycleNumber(cycle);
    }

    @Override
    public void incCycleNumber() {
        super.incCycleNumber();
    }

    @Override
    public int getCycleNumber() {
        return super.getCycleNumber();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public MockEpistemicAgent getAg() {
        return this.epistemicAgent;
    }
}
