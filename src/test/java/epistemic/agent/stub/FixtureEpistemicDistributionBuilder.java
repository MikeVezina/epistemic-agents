package epistemic.agent.stub;

import epistemic.EpistemicDistribution;
import epistemic.World;
import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.mockito.Mockito.spy;
import static utils.TestUtils.createLiteral;
import static utils.TestUtils.createWrappedLiteral;

/**
 * Allows us to create an epistemic distribution from a test fixture that maps
 * a String literal key to its respective enumeration values.
 */
public class FixtureEpistemicDistributionBuilder extends StubEpistemicDistributionBuilder {
    private final Map<WrappedLiteral, LinkedList<Literal>> propMap;

    public FixtureEpistemicDistributionBuilder(Map<WrappedLiteral, LinkedList<Literal>> propMap) {
        super();
        this.propMap = propMap;
    }

    @Override
    protected Map<WrappedLiteral, LinkedList<Literal>> generateLiteralEnumerations(List<Literal> propLiterals) {
        return propMap;
    }

    @Override
    public @NotNull EpistemicDistribution createDistribution(@NotNull EpistemicAgent agent) {
        return spy(super.createDistribution(agent));
    }

    @Override
    protected boolean isPossibleWorld(World nextWorld) {
        return true;
    }

    /**
     * @return a list of all possible literal enumeration values
     */
    public List<Literal> getValues() {
        List<Literal> aggregateList = new ArrayList<>();

        for(var list : propMap.values())
            aggregateList.addAll(list);

        return aggregateList;
    }

    private static Map<WrappedLiteral, LinkedList<Literal>> convertMap(Map<String, List<String>> stringMap) {
        Map<WrappedLiteral, LinkedList<Literal>> result = new HashMap<>();
        for (var entry : stringMap.entrySet())
            result.put(createWrappedLiteral(entry.getKey()), convertList(entry.getValue()));

        return result;
    }

    public static FixtureEpistemicDistributionBuilder fromStringMap(Map<String, List<String>> stringPropMap) {
        return new FixtureEpistemicDistributionBuilder(convertMap(stringPropMap));
    }

    @SafeVarargs
    public static FixtureEpistemicDistributionBuilder ofEntries(Map.Entry<String, List<String>>... stringEntries) {
        return fromStringMap(Map.ofEntries(stringEntries));
    }

    private static LinkedList<Literal> convertList(List<String> stringList) {
        LinkedList<Literal> result = new LinkedList<>();

        for (var entry : stringList) {
            result.add(createLiteral(entry));
        }

        return result;
    }
}
