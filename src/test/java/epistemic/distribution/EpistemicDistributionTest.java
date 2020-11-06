package epistemic.distribution;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import epistemic.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import jason.RevisionFailedException;
import jason.asSemantics.Intention;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.DefaultTerm;
import jason.asSyntax.Literal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.*;

public class EpistemicDistributionTest {

    @ParameterizedTest
    @MethodSource(value = "perceptsFixture")
    void testBufBrfCall(StubAgArch agArch, List<Literal> literalList) {
        var dist = agArch.getEpistemicDistributionSpy();
        dist.setShouldUpdate(false);

        agArch.getAgSpy().buf(literalList);

//        verify(dist, times(1)).buf(eq(literalList), isNotNull());

        verify(dist, times(literalList.size())).brf(any(Literal.class), isNull());

        for (var percept : literalList)
            verify(dist).brf(percept, null);
    }

    @ParameterizedTest
    @MethodSource(value = "perceptsFixture")
    void testBrfDelete(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to delete non-existent prop value
        for (var bel : literalList)
            agArch.getAgSpy().brf(null, bel, Intention.EmptyInt);

        // Nothing should change
        assertTrue(dist.getCurrentPropValues().isEmpty());

    }

    @ParameterizedTest
    @MethodSource(value = "managedBeliefs")
    void testBrfAdd(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to add
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Nothing should change
        assertEquals(literalList.size(), dist.getCurrentPropValues().size());

    }

    @ParameterizedTest
    @MethodSource(value = "managedBeliefs")
    void testBrfAddExisting(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to add
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Attempting to add again
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Nothing should change
        assertEquals(literalList.size(), dist.getCurrentPropValues().size());

    }

    @ParameterizedTest
    @MethodSource(value = "evaluateFormulas")
    void testEvaluateFormulas(StubAgArch agArch, Set<EpistemicFormula> formulaSet) {
        var dist = agArch.getEpistemicDistributionSpy();
        agArch.getReasonerSDKSpy().setDefaultValuation(true);

        // Attempting to evaluate
        var resultEntries = dist.evaluateFormulas(formulaSet).entrySet();

        assertEquals(formulaSet.size(), resultEntries.size());

        // All formulas should evaluate to true (due to reasoner having default true value)
        for (var entry : resultEntries)
            assertEquals(true, entry.getValue(), "formula should have evaluated to true");


        agArch.getReasonerSDKSpy().setDefaultValuation(false);

        // Attempting to evaluate
        resultEntries = dist.evaluateFormulas(formulaSet).entrySet();

        assertEquals(formulaSet.size(), resultEntries.size());

        // All formulas should evaluate to false (due to reasoner having default false value)
        for (var entry : resultEntries)
            assertEquals(false, entry.getValue(), "formula should have evaluated to false");

    }

    @ParameterizedTest
    @MethodSource(value = "managedBeliefs")
    void testBufAfterBrf(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Add propositions
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        agArch.getAgSpy().buf(literalList);

        // Assert that the updated propositions were sent to the reasoner
        assertEquals(literalList.size(), agArch.getReasonerSDKSpy().getCurrentPropositionValues().size());
        for (var literal : literalList) {
            WrappedLiteral wrapped = new WrappedLiteral(literal);
            assertTrue(agArch.getReasonerSDKSpy().getCurrentPropositionValues().containsValue(wrapped), "props should contain " + wrapped);
        }

    }

    @ParameterizedTest
    @MethodSource(value = "updateDuplicatePropositions")
    void testBufAfterBrf(StubAgArch agArch, List<Literal> literalList, List<WrappedLiteral> expectedPropositions) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Add propositions
        for (var bel : literalList)
            agArch.getAgSpy().addBel(bel.addSource(ASSyntax.createString("self")));

        agArch.getAgSpy().buf(new ArrayList<>());

        // Assert that the correct updated propositions were sent to the reasoner
        assertEquals(expectedPropositions.size(), agArch.getReasonerSDKSpy().getCurrentPropositionValues().size());

        // Assert that the BB is consistent with expected props
        assertEquals(expectedPropositions.size(), agArch.getAgSpy().getBB().size(), "The belief base should be updated to maintain proposition consistency");

        for (var belief : agArch.getAgSpy().getBB()) {
            var wrapped = new WrappedLiteral(belief);
            assertTrue(expectedPropositions.contains(wrapped),
                    "the belief base should be consistent with expected propositions. \r\nExpected: " + expectedPropositions + ", BB: " + agArch.getAgSpy().getBB());
        }


        for (var wrapped : expectedPropositions) {
            assertTrue(agArch.getReasonerSDKSpy().getCurrentPropositionValues().containsValue(wrapped), "props should contain " + wrapped);
        }

    }


    @ParameterizedTest
    @MethodSource(value = "deletePropositions")
    void testBrfDelExisting(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to add
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Attempting to remove
        for (var bel : literalList)
            agArch.getAgSpy().brf(null, bel, Intention.EmptyInt);

        // Sets should be empty after deletion
        for (var prop : dist.getCurrentPropValues().values())
            assertEquals(0, prop.size(), "value should be empty after deletion");

    }

    @ParameterizedTest
    @MethodSource(value = "unmanagedBeliefs")
    void testBrfAddUnmanaged(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to delete non-existent prop value
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Nothing should change
        assertTrue(dist.getCurrentPropValues().isEmpty(), "should not set any unmanaged beliefs");
        assertEquals(literalList.size(), agArch.getBeliefBaseSpy().size(), "should add unmanaged beliefs to bb");

    }

    @ParameterizedTest
    @MethodSource(value = "notGroundBeliefs")
    void testBrfAddNotGround(StubAgArch agArch, List<Literal> literalList) throws RevisionFailedException {
        var dist = agArch.getEpistemicDistributionSpy();
        assertTrue(dist.getCurrentPropValues().isEmpty());

        // Attempting to delete non-existent prop value
        for (var bel : literalList)
            agArch.getAgSpy().brf(bel, null, Intention.EmptyInt);

        // Nothing should change
        assertTrue(dist.getCurrentPropValues().isEmpty(), "should not set any unmanaged beliefs");

    }

    private static Stream<Arguments> evaluateFormulas() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);

        return Stream.of(
                builder.buildArguments(
                        builder.buildFormulas()
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(hand('Alice', 'AA'))")
                )

        );
    }

    private static Stream<Arguments> notGroundBeliefs() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList()),
                builder.buildArguments(toLiteralList("hand('Alice', Card)", "hand(Name, 'AA')")),
                builder.buildArguments(toLiteralList("nothand('Alice', _)", "hand(_, 'AA')"))
        );
    }

    private static Stream<Arguments> perceptsFixture() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList()),
                builder.buildArguments(toLiteralList("hand('Alice', 'AA')")),
                builder.buildArguments(toLiteralList("hand('Alice', 'AA')", "test('test', 'wow')"))
        );
    }

    private static Stream<Arguments> unmanagedBeliefs() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList()),
                builder.buildArguments(toLiteralList("nothand('Alice', 'AA')", "hand('Craig', 'AA')"))
        );
    }

    private static Stream<Arguments> updateDuplicatePropositions() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList(), toWrappedLiteralList()),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "hand('Alice', '88')"),
                        toWrappedLiteralList("hand('Alice', '88')")
                ),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "hand('Alice', '88')", "hand('Bob', 'AA')"),
                        toWrappedLiteralList("hand('Alice', '88')", "hand('Bob', 'AA')")
                ),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "~hand('Alice', 'AA')"),
                        toWrappedLiteralList("~hand('Alice', 'AA')")
                ),
                builder.buildArguments(
                        toLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')"),
                        toWrappedLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')")
                ),
                builder.buildArguments(
                        toLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')", "hand('Alice', 'A8')"),
                        toWrappedLiteralList("hand('Alice', 'A8')")
                )
        );

    }

    private static Stream<Arguments> deletePropositions() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList(), toWrappedLiteralList()),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "hand('Alice', '88')"),
                        toWrappedLiteralList("hand('Alice', '88')")
                ),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "hand('Alice', '88')", "hand('Bob', 'AA')"),
                        toWrappedLiteralList("hand('Alice', '88')", "hand('Bob', 'AA')")
                ),
                builder.buildArguments(
                        toLiteralList("hand('Alice', 'AA')", "~hand('Alice', 'AA')"),
                        toWrappedLiteralList("~hand('Alice', 'AA')")
                ),
                builder.buildArguments(
                        toLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')"),
                        toWrappedLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')")
                ),
                builder.buildArguments(
                        toLiteralList("~hand('Alice', 'AA')", "~hand('Alice', '88')", "hand('Alice', 'A8')"),
                        toWrappedLiteralList("hand('Alice', 'A8')")
                )
        );

    }

    private static Stream<Arguments> managedBeliefs() {
        var builder = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(toLiteralList()),
                builder.buildArguments(toLiteralList("hand('Alice', 'AA')", "hand('Bob', 'AA')"))
        );
    }

}
