package epistemic.agent.mock;

import epistemic.EpistemicDistribution;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;

import static org.mockito.Mockito.spy;

public class MockEpistemicAgent extends EpistemicAgent {
    private EpistemicDistribution epistemicDistribution;

    public MockEpistemicAgent()
    {
        super();
    }

    /**
     * Loads an empty agent
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

    @Override
    protected void agentLoaded() {
        // Spy on the distribution object
        this.epistemicDistribution = spy(super.getEpistemicDistribution());
        super.setEpistemicDistribution(epistemicDistribution);
        super.agentLoaded();

    }

    /**
     * Returns an epistemic distribution that has been spied on using
     * Mockito.spy().
     */
    @Override
    public EpistemicDistribution getEpistemicDistribution()
    {
        return this.epistemicDistribution;
    }

}
