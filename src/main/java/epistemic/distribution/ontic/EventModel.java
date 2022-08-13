package epistemic.distribution.ontic;

import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.NormalizedWrappedLiteral;
import epistemic.wrappers.WrappedLiteral;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.*;

public class EventModel {

    private final EpistemicAgent epistemicAgent;
    private final Rule applicabilityRule;
    private Term eventTerm;
    private Rule preRule;
    private Literal postLiteral;

    public EventModel(EpistemicAgent epistemicAgent, Rule applicabilityRule, Term eventTerm) {
        this.eventTerm = eventTerm;
        this.epistemicAgent = epistemicAgent;
        this.applicabilityRule = applicabilityRule;
    }

    public EpistemicAgent getEpistemicAgent() {
        return epistemicAgent;
    }

    public Term getEventTerm() {
        return eventTerm;
    }

    public void setEventTerm(Term eventTerm) {
        this.eventTerm = eventTerm;
    }

    public Rule getPreRule() {
        return preRule;
    }

    public void setPreRule(Rule preRule) {
        this.preRule = preRule;
    }

    public Literal getPostLiteral() {
        return postLiteral;
    }

    public void setPostLiteral(Literal postLiteral) {
        if (postLiteral == null)
            return;


        // Handle overwriting world props (first)
        if (postLiteral.getArity() == 3) {
            if (!Literal.LTrue.equals(postLiteral.getTerm(2)) && !Literal.LFalse.equals(postLiteral.getTerm(2))) {
                System.out.println("Post literal with overwrite was invalid literal: " + postLiteral);
                return;
            }
        }

        this.postLiteral = postLiteral;

    }

    public Rule getApplicabilityRule() {
        return applicabilityRule;
    }

    public boolean isApplicable() {
        var unifs = getApplicabilityRule().getBody().logicalConsequence(epistemicAgent, new Unifier());
        return unifs != null && unifs.hasNext();
    }

    public Set<World> getPreConditionWorlds() {
        // Get all worlds if no pre condition
        if (preRule == null)
            return epistemicAgent.getEpistemicDistribution().getManagedWorlds();

        if (preRule.getBody() instanceof LogExpr) {
            extract_form((LogExpr) preRule.getBody(), new Unifier());
        }

        var worldConsequences = epistemicAgent.getEpistemicDistribution().getManagedWorlds().logicalConsequences(preRule.getBody(), new Unifier());
        return worldConsequences.keySet();
    }

    private String extract_form(LogExpr expr, Unifier un) {



        List<String> all = new ArrayList<>();
        var iter = expr.logicalConsequence(epistemicAgent, un);

        while (!expr.isGround()) {
            if (iter == null || !iter.hasNext())
                return "NOT";
            var newExpr = (LogExpr) expr.capply(iter.next());

            if(newExpr.getOp() == LogExpr.LogicalOp.or)
            {
                //newExpr = new LogExpr(LogExpr.LogicalOp.not, new LogExpr(new LogExpr(LogExpr.LogicalOp.not, expr.getLHS()), LogExpr.LogicalOp.and, new LogExpr(LogExpr.LogicalOp.not, expr.getRHS())));
                System.out.println();
            }

            all.add(EpistemicFormulaFactory.fromLogExpr(epistemicAgent, newExpr, un));
        }


        return "";
    }


    public World applyPostConditionWorld(World world) {
        // Get all worlds if no pre condition
        if (postLiteral == null)
            return world;

        // Overwrite if third term is true
        boolean overwrite = postLiteral.getArity() == 3 && postLiteral.getTerm(2) == Literal.LTrue;


        List<WrappedLiteral> groundPostEffects = new ArrayList<>();

        // Check if rule is applicable to world
        if (!postLiteral.isRule()) {
            if (postLiteral.isGround())
                groundPostEffects.add(new WrappedLiteral(postLiteral));
        } else {
            var postRule = (Rule) postLiteral;
            var worldConsequences = epistemicAgent.getEpistemicDistribution().getManagedWorlds()
                    .logicalConsequences(world, postRule.getBody(), new Unifier())
                    .get(world);

            if (worldConsequences == null)
                return world;

            for (var cons : worldConsequences) {
                Unifier unif = cons.getUnifier();
                Literal newHead = postRule.headCApply(unif);

                if (newHead.isGround())
                    groundPostEffects.add(new WrappedLiteral(newHead));
            }
        }

        World newWorld = world.createCopy();
        // new world ID should be the same as old world
        newWorld.setWorldId(world.getWorldId());

        // Props need to be ordered to accommodate overwriting
        LinkedList<Literal> deltaProps = new LinkedList<>();


        // Handle overwriting world props (first)
        if (overwrite) {
            // Negate all props in world
            for (var prop : world.getValuation())
                deltaProps.add(new LiteralImpl(Literal.LNeg, prop.getCleanedLiteral()));
        }


        // Turn term into a list of prop changes
        for (WrappedLiteral l : groundPostEffects) {

            Literal cleaned = l.getCleanedLiteral();

            Term addTerm = cleaned.getTerm(1);

            if (addTerm.isList()) {
                for (Term t : (ListTerm) addTerm) {
                    deltaProps.add((Literal) t);
                }
            } else
                deltaProps.add((Literal) addTerm);

        }

        for (Literal l : deltaProps) {
            if (!epistemicAgent.getEpistemicDistribution().getManagedWorlds().getManagedLiterals().isManagedBelief(l))
                continue;

            if (l.negated())
                newWorld.removeFromValuation(new NormalizedWrappedLiteral(l));
            else
                newWorld.addToValuation(new NormalizedWrappedLiteral(l));
        }


        return newWorld;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventModel that = (EventModel) o;

        return eventTerm != null ? eventTerm.equals(that.eventTerm) : that.eventTerm == null;
    }

    @Override
    public int hashCode() {
        return eventTerm != null ? eventTerm.hashCode() : 0;
    }
}
