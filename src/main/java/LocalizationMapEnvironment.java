import jason.asSyntax.*;
import jason.environment.Environment;
import localization.models.MapEvent;
import localization.view.LocalizationMapView;
import localization.models.LocalizationMapModel;
import localization.MapEventListener;

import java.util.*;

public class LocalizationMapEnvironment extends Environment implements MapEventListener {



    private final LocalizationMapView localizationMapView;
    private final LocalizationMapModel localizationMapModel;
    private final Queue<MapEvent> mapEventQueue;

    public LocalizationMapEnvironment() {
        this.mapEventQueue = new LinkedList<>();
        localizationMapView = new LocalizationMapView();
        localizationMapModel = localizationMapView.getModel();
        localizationMapModel.addMapListener(this);

        localizationMapView.setVisible(true);
    }


    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning

        var curPercepts = super.getPercepts(agName);

        if (curPercepts == null)
            curPercepts = new ArrayList<>();

        // add persistent percepts
        curPercepts.addAll(getPersistentPercepts());

        // Add always-present percepts
        if (mapEventQueue.isEmpty() && curPercepts.isEmpty())
            return curPercepts;

        // If no events need to be processed, return null (no change in percepts)
        if (mapEventQueue.isEmpty())
            return null;

        // Get next event to process
        MapEvent nextEvent = mapEventQueue.poll();

        curPercepts.add(ASSyntax.createAtom("moved"));
        curPercepts.addAll(nextEvent.getPerceptions());
        curPercepts.add(ASSyntax.createLiteral("lastMove", nextEvent.getMoveDirection()));

        return curPercepts;
    }

    private List<Literal> getPersistentPercepts() {
        List<Literal> persistPercepts = new ArrayList<>();

        persistPercepts.add(ASSyntax.createLiteral("modelObject", new ObjectTermImpl(localizationMapModel)));

        if (localizationMapView.getSettingsPanel().shouldAutoMove())
            persistPercepts.add(ASSyntax.createAtom("autoMove"));

        return persistPercepts;
    }


    @Override
    public synchronized void agentMoved(MapEvent event) {
        this.mapEventQueue.add(event);

        // Inform that agents need new percepts (otherwise there is a delay!)
        if (this.getEnvironmentInfraTier() != null)
            this.getEnvironmentInfraTier().informAgsEnvironmentChanged();
    }
}
