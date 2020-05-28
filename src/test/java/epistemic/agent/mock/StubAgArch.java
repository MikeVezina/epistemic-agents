package epistemic.agent.mock;

import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.CentralisedAgArch;
import jason.runtime.Settings;
import jason.util.Config;

import static org.mockito.Mockito.spy;


public class StubAgArch extends CentralisedAgArch {

    private final TransitionSystem mockTs;
    private final EpistemicAgent epistemicAgent;

    public StubAgArch() {
        this(new StubEpistemicDistributionBuilder());
    }

    public StubAgArch(EpistemicDistributionBuilder distributionBuilder) {
        super();

        // Disable the Web inspector during testing
        Config.get().setProperty(Config.START_WEB_MI, "false");

        // Mock the reasoner SDK
        this.epistemicAgent = spy(new EpistemicAgent(spy(distributionBuilder)));
        this.mockTs = spy(new TransitionSystem(epistemicAgent, new Circumstance(), new Settings(), this));
        this.epistemicAgent.setTS(mockTs);
        this.setTS(mockTs);

        this.epistemicAgent.initAg();

    }

    @Override
    public void init() throws Exception {
        super.init();

    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * @return A StubEpistemicAgent wrapped with a spy.
     */
    public EpistemicAgent getAg() {
        return this.epistemicAgent;
    }
}
