package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.EpistemicDistributionBuilder;
import epistemic.agent.EpistemicAgent;
import jason.JasonException;
import org.jetbrains.annotations.Nullable;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StubEpistemicAgent extends EpistemicAgent {

    public StubEpistemicAgent(StubEpistemicDistributionBuilder mockEpistemicDistributionBuilder) {
        super(mockEpistemicDistributionBuilder);
    }

    @Override
    protected void agentLoaded() {
        super.agentLoaded();

        // Wrap distribution with spy

    }

    public void verifyNotLoaded()
    {
        verify(this, times(0)).agentLoaded();
    }

    /**
     * @return A spy EpistemicDistribution object, created by the StubEpistemicDistributionBuilder
     */
    public EpistemicDistribution getEpistemicDistributionSpy()
    {
        return this.getEpistemicDistribution();
    }

    /**
     * calls load while wrapping the exception in a runtime exception
     */
    public void suppressedLoad() {

        try {
            this.load("");
        } catch (JasonException e) {
            throw new RuntimeException("Failed to load agent: ", e);
        }
    }
}
