package epistemic.agent;

import epistemic.agent.stub.FixtureEpistemicDistributionBuilder;
import epistemic.fixture.AgArchFixture;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.*;

public class ChainedEpistemicBBTest {

    // Fixture of EpistemicDistribution enumerations
    private static final FixtureEpistemicDistributionBuilder DISTRIBUTION_FIXTURE = FixtureEpistemicDistributionBuilder.ofEntries(
            new SimpleEntry<>("hand('Alice', Card)",
                    List.of("hand('Alice', 'AA')", "hand('Alice', '88')", "hand('Alice', 'A8')")),

            new SimpleEntry<>("hand('Bob', Card)",
                    List.of("hand('Bob', 'AA')", "hand('Bob', '88')", "hand('Bob', 'A8')"))
    );

    @Test
    public void contains() {

    }


    @ParameterizedTest
    @MethodSource(value = "candidateBeliefBaseLiterals")
    public void getCandidateBeliefsFromBB(AgArchFixture agArchFixture) {
        var arch = agArchFixture.getAgArch();

        for(var belief : agArchFixture.getBeliefs())
        {
            arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());
            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any());
        }

        verify(arch.getBeliefBaseSpy(), times(agArchFixture.getBeliefs().size())).getCandidateBeliefs(any(Literal.class), any());
    }

    @ParameterizedTest
    @MethodSource(value = "candidateBeliefGroundFormulas")
    public void getCandidateBeliefsFromReasoner(AgArchFixture agArchFixture) {
        var arch = agArchFixture.getAgArch();

        for(var formula : agArchFixture.getFormulas())
        {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getOriginalLiteral(), new Unifier());
            assertEquals(formula.getOriginalLiteral(), iter.next(), "formula should be returned since it is ground");

            // Verify that the internal belief base does not get called since
            // this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(), any());

        }

        verify(arch.getBeliefBaseSpy(), times(agArchFixture.getBeliefs().size())).getCandidateBeliefs(any(Literal.class), any());

    }

    private static Stream<Arguments> candidateBeliefBaseLiterals() {
        return Stream.of(
                Arguments.of(
                        createAgArchFixture(
                                List.of(
                                        "test"
                                ),
                                List.of(
                                        "test",
                                        "nontest"
                                )
                        )
                )
        );
    }

    private static Stream<Arguments> candidateBeliefGroundFormulas() {
        return Stream.of(
                Arguments.of(
                        createAgArchFixture(
                                List.of(
                                        "test"
                                ),
                                List.of(
                                        "test"
                                )
                        )
                )
        );
    }

    public static AgArchFixture createAgArchFixture() {
        return createAgArchFixture(List.of(), List.of());
    }

    public static AgArchFixture createAgArchFixture(List<String> queryBels) {
        return createAgArchFixture(List.of(), queryBels);
    }

    /**
     * Use default distribution with a list of initial beliefs and beliefs to query.
     * @param initialBels
     * @param beliefsToQuery
     * @return
     */
    public static AgArchFixture createAgArchFixture(List<String> initialBels, List<String> beliefsToQuery) {

        // Create formulas. All enumerations will be
        var formulas = createFormulaMap(DISTRIBUTION_FIXTURE.getValues());
        var initBelsLit = new ArrayList<Literal>();
        var belsToQuery = new ArrayList<Literal>();

        for(String initBel : initialBels)
            initBelsLit.add(createLiteral(initBel));

        for(String bel : beliefsToQuery)
            belsToQuery.add(createLiteral(bel));

        return new AgArchFixture(DISTRIBUTION_FIXTURE,initBelsLit, belsToQuery, formulas);
    }

}