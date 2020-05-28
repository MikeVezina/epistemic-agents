package epistemic.agent.mock;

import epistemic.EpistemicDistributionBuilder;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.Settings;


public class MockAgArch extends CentralisedAgArch {

    //private final ChainedEpistemicBB beliefBase;
    private final TransitionSystem mockTs;
    private final MockEpistemicAgent epistemicAgent;

    public MockAgArch() {
        this(new MockEpistemicDistributionBuilder());
    }

    public MockAgArch(EpistemicDistributionBuilder distributionBuilder) {
        super();

        // Mock the reasoner SDK
        this.epistemicAgent = new MockEpistemicAgent(distributionBuilder);
        this.mockTs = new TransitionSystem(epistemicAgent, new Circumstance(), new Settings(), this);
        this.epistemicAgent.setTS(mockTs);
        this.setTS(mockTs);

        this.epistemicAgent.loadAgent();
    }

    @Override
    public void init() throws Exception {
        super.init();

    }

    @Override
    public void stop() {
        super.stop();
    }

    public MockEpistemicAgent getAg() {
        return this.epistemicAgent;
    }
}
