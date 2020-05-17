package reasoner;

import epistemic.ManagedWorlds;
import wrappers.WrappedLiteral;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;

public class WorldRequest implements PropertyChangeListener {
    private final ReasonerSDK reasoner;
    private final ManagedWorlds managedWorlds;

    public WorldRequest(ManagedWorlds managedWorlds) {
        this.managedWorlds = managedWorlds;
        this.reasoner = new ReasonerSDK();


        // Create the managed worlds
        reasoner.createModel(managedWorlds);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(ManagedWorlds.PROPS_PROPERTY) || evt.getNewValue() == null || !(evt.getNewValue() instanceof HashSet))
            return;

        var propositionSet = (HashSet<?>) evt.getNewValue();
        var propositionStrings = new ArrayList<String>();


        for (var literalKey : propositionSet) {
            if (!(literalKey instanceof WrappedLiteral))
                continue;

            propositionStrings.add(((WrappedLiteral) literalKey).toSafePropName());
        }

        var result = reasoner.updateProps(propositionStrings);
        managedWorlds.addKnowledge(result);


        System.out.println("Prop " + propositionStrings.toString() + " update success: " + result);
    }


}

