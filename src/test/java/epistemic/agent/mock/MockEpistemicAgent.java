package epistemic.agent.mock;

import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;
import jason.util.Config;

public class MockEpistemicAgent extends EpistemicAgent {

    public MockEpistemicAgent(EpistemicDistributionBuilder mockEpistemicDistributionBuilder) {
        super(mockEpistemicDistributionBuilder);
    }

    /**
     * Loads an empty agent and suppresses the exception
     */
    public void loadAgent()
    {
        try {
            // Disable the Web inspector during testing
            Config.get().setProperty(Config.START_WEB_MI, "false");

            super.initAg();
            super.load("");
        } catch (JasonException e) {
            // This will never happen due to the empty string.
            throw new RuntimeException(e);
        }
    }
}
