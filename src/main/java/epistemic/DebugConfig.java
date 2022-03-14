package epistemic;

import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.environment.grid.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class DebugConfig {
    private static final boolean DEBUG = false;
    // Create unique perceptions (gives largest prop size)
    private static final boolean UNIQUE_PERCEPTS = false;
    // Max number of obst/none percept combinations
    private static final boolean MAX_PERCEPTS = false;
    private static final boolean showGUI = true;
    private static final boolean showSettings = false;

    public boolean useUniquePercepts() {
        return isDebugging() && UNIQUE_PERCEPTS;
    }

    public boolean useMaxPercepts() {
        return isDebugging() && MAX_PERCEPTS;
    }

    private static DebugConfig instance;
    private final boolean isDebugging;

    private DebugConfig(boolean debugging) {
        this.isDebugging = debugging;
    }

    public synchronized static DebugConfig getInstance() {
        if (instance == null)
            instance = new DebugConfig(DEBUG);

        return instance;
    }

    public boolean isDebugging() {
        return isDebugging;
    }

    /**
     * Insert fake values into the ManagedWorlds object for evaluation purposes only.
     * (Used for evaluation of knows/possible queries)
     */
    public void insertFakes(ManagedWorlds managedWorlds, Logger logger) {
        if (!isDebugging)
            return;

        Set<NormalizedWrappedLiteral> litSet = new HashSet<>();

        var variable = ASSyntax.createVar("Val");
        var lit = ASSyntax.createLiteral("fake", variable);

        for (int i = 0; i < 50; i++) {
            var u = new Unifier();
            u.bind(variable, new NumberTermImpl(i));
            var unified = (Literal) lit.capply(u);
            litSet.add(new NormalizedWrappedLiteral(unified));
        }

        NormalizedWrappedLiteral newLit = new NormalizedWrappedLiteral(lit);

        for (var w : managedWorlds) {
            w.put(newLit, litSet);

            // Refresh new ML
            managedWorlds.getManagedLiterals().worldAdded(w);
        }


        Set<NormalizedWrappedLiteral> falseLitSet = new HashSet<>();


        var falseVar = ASSyntax.createVar("False");
        var falseLit = ASSyntax.createLiteral("take", falseVar);

        for (int i = 0; i < 50; i++) {
            var u = new Unifier();
            u.bind(falseVar, new NumberTermImpl(i));
            var unified = (Literal) falseLit.capply(u);
            falseLitSet.add(new NormalizedWrappedLiteral(unified));
        }

        NormalizedWrappedLiteral newFalseLit = new NormalizedWrappedLiteral(falseLit);

        World falseWorld = new World();
        falseWorld.put(newFalseLit, falseLitSet);


        // Add false values for possible testing
        managedWorlds.getManagedLiterals().worldAdded(falseWorld);


        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");

    }

    public List<Location> getExtraAgents()
    {
        List<Location> agentLocs = new ArrayList<>();
        // Bob
        agentLocs.add(new Location(2, 1));
//
//        // Charlie
        agentLocs.add(new Location(1, 3));

        return agentLocs;
    }

    public boolean showGUI() {
        return showGUI;
    }

    public boolean showSettingsPanel() {
        return showSettings;
    }
}
