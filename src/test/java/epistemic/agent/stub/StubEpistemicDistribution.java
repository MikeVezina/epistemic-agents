package epistemic.agent.stub;

import epistemic.distribution.EpistemicDistribution;
import epistemic.ManagedWorlds;
import epistemic.agent.EpistemicAgent;
import epistemic.reasoner.ReasonerSDK;
import org.jetbrains.annotations.NotNull;

public class StubEpistemicDistribution extends EpistemicDistribution {
    private boolean shouldUpdate = true;

    public StubEpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds) {
        super(agent, managedWorlds);
    }

    public StubEpistemicDistribution(@NotNull EpistemicAgent agent, @NotNull ManagedWorlds managedWorlds, @NotNull ReasonerSDK reasonerSDK) {
        super(agent, managedWorlds, reasonerSDK);
    }

    @Override
    protected boolean shouldUpdateReasoner() {
        return shouldUpdate;
    }

    public void setShouldUpdate(boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
    }
}
