package epistemic.agent.mock;

import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;

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
            super.initAg();
            super.load("");
        } catch (JasonException e) {
            // This will never happen due to the empty string.
            throw new RuntimeException(e);
        }
    }
}
