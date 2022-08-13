package epistemic.distribution.consequences;

import epistemic.World;
import jason.asSemantics.Unifier;
import jason.asSyntax.LogicalFormula;

public class WorldConsequences {
    private World world;
    private Unifier unifier;
    private LogicalFormula logForm;

    public WorldConsequences(World world, Unifier unifier, LogicalFormula logForm) {
        this.world = world;
        this.unifier = unifier;
        this.logForm = logForm;
    }

    public World getWorld() {
        return world;
    }

    public Unifier getUnifier() {
        return unifier;
    }

    public LogicalFormula getLogicalFormula() {
        return logForm;
    }


}
