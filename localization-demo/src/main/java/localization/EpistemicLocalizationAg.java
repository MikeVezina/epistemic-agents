package localization;

import epistemic.agent.EpistemicAgent;
import jason.asSemantics.Agent;

public class EpistemicLocalizationAg extends EpistemicAgent {


    @Override
    public void initAg() {
        // Sets up BB instance.
        super.initAg();

        // Add map data
        for (var bel : LocalizationMapEnvironment.instance.getModel().dumpMapBeliefsToBB())
            this.addInitialBel(bel);
    }
}
