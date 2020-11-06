package localization;

import jason.architecture.AgArch;

public class LocalizationAgArch extends AgArch {

    @Override
    public void reasoningCycleFinished() {
        super.reasoningCycleFinished();
        // Signal input when finished running
        if(this.getTS().getC().getNbRunningIntentions() == 0)
            LocalizationMapEnvironment.instance.getModel().signalInput(true);
    }
}
