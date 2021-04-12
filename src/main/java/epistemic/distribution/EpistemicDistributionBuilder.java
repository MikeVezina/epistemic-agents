package epistemic.distribution;

import epistemic.ManagedWorlds;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.bb.BeliefBase;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public abstract class EpistemicDistributionBuilder<T> {

    private EpistemicAgent epistemicAgent;
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Should be called once the agent has been loaded.
     * This method needs to process the belief base as a whole
     * after the initial agent has been loaded and processed.
     * This is required in order for logicalConsequences to work
     * correctly.
     *
     * @param agent Necessary for accessing BB rules and agent
     *              logical consequences for evaluation and expansion.
     */
    @NotNull
    public EpistemicDistribution createDistribution(@NotNull EpistemicAgent agent) {
        this.epistemicAgent = agent;

        var managedWorlds = processDistribution();

        // TODO: REmove this! For query time evaluation only
        insertFakes(managedWorlds);


        if(managedWorlds.size() > 10000)
            logger.info("More than 10k worlds. Not printing.");
//        else
//            logger.info(managedWorlds.toString());

        return new EpistemicDistribution(this.epistemicAgent, managedWorlds);
    }

    private void insertFakes(ManagedWorlds managedWorlds) {
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

        for (var w : managedWorlds)
        {
            w.put(newLit, litSet);

            // Refresh new ML
            managedWorlds.getManagedLiterals().worldAdded(w);
        }



        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.warning("Fakes are being injected still!!!!!!!!!!!!!!!!!!!!!!!!");

    }

    /**
     * Whether or not the distribution can use the passed-in literal (i.e. belief or rule).
     * The returned value will be the key of the literal when the map of accepted literals is passed to {@link EpistemicDistributionBuilder#generateWorlds(Map)}.
     * @see EpistemicDistributionBuilder#generateWorlds(Map)
     * @param literal An initial belief base literal.
     * @return The key for the literal, or null if the literal is not used for creating the epistemic distribution.
     */
    protected abstract T acceptsLiteral(Literal literal);

    /**
     * Process the distribution of worlds, create, and set the ManagedWorlds object.
     */
    protected ManagedWorlds processDistribution() {
        // Gets and processes all literals in the kb belief base that are marked with 'prop'
        var filteredLiterals = processLiterals(epistemicAgent.getBB());

        // Create the distribution of worlds
        return generateWorlds(filteredLiterals);
    }

    protected EpistemicAgent getEpistemicAgent()
    {
        return epistemicAgent;
    }

    /**
     * Iterates through the belief base, filters them according to the list of functions, and returns the filtered literals/beliefs.
     * If any of the filters return false for a given belief, it will not be returned. Filters are called in the order
     * that they are passed in.
     *
     * @return A list of filtered literals
     */
    protected Map<T, List<Literal>> processLiterals(BeliefBase literals) {
        // We need to iterate all beliefs.
        // We can't use beliefBase.getCandidateBeliefs(...) [aka the pattern matching function] because
        // the pattern matching function doesn't allow us to pattern match by just namespace and annotation
        // (it requires a functor and arity)
        Map<T, List<Literal>> filteredLiterals = new HashMap<>();

        // Iterate through the belief base and call the consumers
        for (Literal belief : literals) {
            if (belief == null)
                continue;

            T resultKey = acceptsLiteral(belief);

            if(resultKey == null)
                continue;

            if(!filteredLiterals.containsKey(resultKey))
                filteredLiterals.put(resultKey, new ArrayList<>());

            filteredLiterals.get(resultKey).add(belief);
        }

        return filteredLiterals;
    }


    /**
     * Generate worlds given a mapping of all propositions. This essentially generates all permutations of each of the possible enumeration values.
     *
     * @param allPropositionsMap This is a mapping of all literals (which are used to create the propositions used in each of the worlds)
     * @return A List of Valid worlds
     */
    protected abstract ManagedWorlds generateWorlds(Map<T, List<Literal>> allPropositionsMap);
}
