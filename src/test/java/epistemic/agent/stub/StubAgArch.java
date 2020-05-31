package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.reasoner.stub.StubReasonerSDK;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.Settings;
import jason.util.Config;

import static org.mockito.Mockito.*;

/**
 * Sets up a full agent architecture that has been loaded with
 * spy stub components for testing.
 */
public class StubAgArch extends CentralisedAgArch {

    private final TransitionSystem mockTs;
    private final StubEpistemicAgent epistemicAgent;
    private final StubEpistemicDistributionBuilder distributionBuilder;
    private final BeliefBase beliefBase;

    public StubAgArch(boolean loadAgent) {
        this(new StubEpistemicDistributionBuilder(), loadAgent);
    }

    public StubAgArch(StubEpistemicDistributionBuilder distributionBuilder, boolean loadAgent) {
        super();

        // Disable the Web inspector during testing
        Config.get().setProperty(Config.START_WEB_MI, "false");

        // Mock the reasoner SDK
        this.beliefBase = spy(new DefaultBeliefBase());
        this.distributionBuilder = spy(distributionBuilder);
        this.epistemicAgent = spy(new StubEpistemicAgent(distributionBuilder));
        this.epistemicAgent.setBB(beliefBase);

        // Set transition system
        this.mockTs = spy(new TransitionSystem(epistemicAgent, new Circumstance(), new Settings(), this));
        this.epistemicAgent.setTS(mockTs);
        this.setTS(mockTs);

        // Initialize (but do not load) the agent
        this.epistemicAgent.initAg();

        if(loadAgent)
            this.epistemicAgent.suppressedLoad();
        else
            this.epistemicAgent.verifyNotLoaded();

    }

    /**
     * @return A belief base spy.
     */
    public BeliefBase getBeliefBaseSpy()
    {
        return beliefBase;
    }

    /**
     * @return A StubEpistemicAgent wrapped with a spy.
     */
    public StubEpistemicAgent getAgSpy() {
        return this.epistemicAgent;
    }

    public StubEpistemicDistributionBuilder getDistributionBuilderSpy() {
        return this.distributionBuilder;
    }

    public StubReasonerSDK getReasonerSDKSpy()
    {
        return getDistributionBuilderSpy().getReasonerSDKSpy();
    }

    public StubEpistemicDistribution getEpistemicDistributionSpy() {
        getAgSpy().verifyLoaded();
        return getAgSpy().getEpistemicDistribution();
    }

    @Override
    public String toString() {
        return "StubArch [Beliefs: " + beliefBase.size() + "]";
    }
}
