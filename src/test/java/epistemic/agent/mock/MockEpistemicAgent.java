package epistemic.agent.mock;

import epistemic.EpistemicDistribution;
import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;
import org.jetbrains.annotations.NotNull;

import static org.mockito.Mockito.spy;

public class MockEpistemicAgent extends EpistemicAgent {
    private EpistemicDistribution epistemicDistribution;

    /**
     * Loads an empty agent and supresses the exception
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
    protected @NotNull EpistemicDistributionBuilder createDistributionBuilder() {
        var builder = super.createDistributionBuilder();
        this.epistemicDistribution = spy(builder.createDistribution());

        // Return a distribution builder that returns a spy of the original distribution
        return new EpistemicDistributionBuilder(builder.getEpistemicAgent()){
            @Override
            public @NotNull EpistemicDistribution createDistribution() {
                return epistemicDistribution;
            }
        };
    }

    /**
     * Returns an epistemic distribution that has been spied on using
     * Mockito.spy().
     */
    public EpistemicDistribution getEpistemicDistribution()
    {
        return this.epistemicDistribution;
    }

}
