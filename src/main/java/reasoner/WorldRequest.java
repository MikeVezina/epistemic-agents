package reasoner;

import epi.ManagedWorlds;
import wrappers.LiteralKey;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;

public class WorldRequest implements PropertyChangeListener {
    private final ReasonerSDK reasoner;
    private final ManagedWorlds managedWorlds;

    public WorldRequest(ManagedWorlds managedWorlds) {
        this.managedWorlds = managedWorlds;
        this.managedWorlds.addPropertyListener(this);
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
            if (!(literalKey instanceof LiteralKey))
                continue;

            propositionStrings.add(((LiteralKey) literalKey).toSafePropName());
        }

        var result = reasoner.updateProps(propositionStrings);
        managedWorlds.addKnowledge(result);


        System.out.println("Prop " + propositionStrings.toString() + " update success: " + result);
    }


}

