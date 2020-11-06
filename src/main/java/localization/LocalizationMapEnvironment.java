package localization;

import jason.asSyntax.*;
import jason.environment.Environment;
import localization.models.MapEvent;
import localization.view.LocalizationMapView;
import localization.models.LocalizationMapModel;

import java.util.*;

public class LocalizationMapEnvironment extends Environment implements MapEventListener {


    // Hack to access from agent....
    public static LocalizationMapEnvironment instance;

    private final LocalizationMapView localizationMapView;
    private final LocalizationMapModel localizationMapModel;
    private final Queue<MapEvent> mapEventQueue;

    public LocalizationMapEnvironment() {
        instance = this;
        this.mapEventQueue = new LinkedList<>();
        localizationMapView = new LocalizationMapView();
        localizationMapModel = localizationMapView.getModel();

        // Generate the map information beliefs based on the loaded map
        localizationMapModel.generateASL();



        localizationMapModel.addMapListener(this);
        localizationMapView.setVisible(true);
    }

    @Override
    public void init(String[] args) {
        super.init(args);
    }

    @Override
    public synchronized Collection<Literal> getPercepts(String agName) {
        // No change in perceptions if the agent hasn't moved
        // Also, keep current percepts if the agent is not done reasoning
        super.clearPercepts(agName);

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
//        curPercepts.add(ASSyntax.createLiteral(Literal.LPos, "location", ASSyntax.createNumber(2), ASSyntax.createNumber(1)));
//        curPercepts.add(ASSyntax.createLiteral(Literal.LPos, "location", ASSyntax.createNumber(1), ASSyntax.createNumber(1)));
//        curPercepts.add(ASSyntax.createLiteral(Literal.LNeg, "location", ASSyntax.createNumber(3), ASSyntax.createNumber(3)));
        curPercepts.addAll(nextEvent.getPerceptions());
        curPercepts.add(ASSyntax.createLiteral("lastMove", nextEvent.getMoveDirection()));

        return curPercepts;
    }

    private List<Literal> getPersistentPercepts() {
        List<Literal> persistPercepts = new ArrayList<>();

        persistPercepts.add(ASSyntax.createLiteral("modelObject", new ObjectTermImpl(localizationMapModel)));

        if (localizationMapView.getSettingsPanel().shouldAutoMove())
            persistPercepts.add(ASSyntax.createLiteral("autoMove"));

        return persistPercepts;
    }


    @Override
    public synchronized void agentMoved(MapEvent event) {
        this.mapEventQueue.add(event);

        // Disable input until agent is ready.
        getModel().signalInput(false);

        // Inform that agents need new percepts (otherwise there is a delay!)
        if (this.getEnvironmentInfraTier() != null)
            this.getEnvironmentInfraTier().informAgsEnvironmentChanged();
    }

    public LocalizationMapModel getModel() {
        return localizationMapModel;
    }
}
