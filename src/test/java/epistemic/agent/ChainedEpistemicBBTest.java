package epistemic.agent;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.*;

public class ChainedEpistemicBBTest {

    // A builder that uses the default distribution fixture and inserts the 'test' literal as an initial belief.
    private static final AgArchFixtureBuilder BUILDER = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE, "test");

    private static Stream<Arguments> managedBeliefPI() {
        return null;
    }

    @ParameterizedTest
    @MethodSource(value = "candidateNonBeliefLiterals")
    public void getNoCandidateBeliefsFromBB(StubAgArch arch, List<Literal> queryBeliefs) {

        for (var belief : queryBeliefs) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertNull(iter, "iter should be null since belief should not exist in BB");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any(Unifier.class));
        }

        verify(arch.getBeliefBaseSpy(), times(queryBeliefs.size())).getCandidateBeliefs(any(Literal.class), any());
    }


    @ParameterizedTest
    @MethodSource(value = "candidateBeliefLiterals")
    public void getCandidateBeliefsFromBB(StubAgArch arch, List<Literal> queryBeliefs) {

        for (var belief : queryBeliefs) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertEquals(belief.clearAnnots(), iter.next().clearAnnots(), "belief should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one belief should be returned since it is ground");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any(Unifier.class));
        }

        verify(arch.getBeliefBaseSpy(), times(queryBeliefs.size())).getCandidateBeliefs(any(Literal.class), any());
    }

    @ParameterizedTest
    @MethodSource(value = "candidateGroundFormulas")
    public void getCandidateBeliefsFromReasoner(StubAgArch arch, Set<EpistemicFormula> formulas) {

        // Set the reasoner to return true for these formulas
        arch.getReasonerSDKSpy().setFormulaValuation(formulas, true);

        for (var formula : formulas) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getCleanedOriginal(), new Unifier());

            assertEquals(formula.getCleanedOriginal(), iter.next(), "formula should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one formula should be returned since it is ground");

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(Literal.class), any(Unifier.class));

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(PredicateIndicator.class));

        }

    }

    @ParameterizedTest
    @MethodSource(value = "unmanagedGroundFormula")
    public void testUnmanagedFormulasFromReasoner(StubAgArch arch, Set<EpistemicFormula> formulas) {

        // Set the reasoner to return true for these formulas
        arch.getReasonerSDKSpy().setFormulaValuation(formulas, true);

        for (var formula : formulas) {
            arch.getAgSpy().getBB().getCandidateBeliefs(formula.getCleanedOriginal(), new Unifier());

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(1)).getCandidateBeliefs(eq(formula.getCleanedOriginal()), any(Unifier.class));


        }
    }

    @ParameterizedTest
    @MethodSource(value = "candidateBeliefLiterals")
    public void testPredicateIndicatorCandidates(StubAgArch arch, List<Literal> beliefs) {

        for (var belief : beliefs) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief.getPredicateIndicator());
            var iterSubset = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            // Assert that the predicate indicator contains (at the least) beliefs corresponding to the current belief
            assertIteratorContains(iter, iterSubset);

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(1)).getCandidateBeliefs(any(PredicateIndicator.class));

        }
    }

    @ParameterizedTest
    @MethodSource(value = "candidateGroundFormulas")
    public void getNoCandidateBeliefsFromReasoner(StubAgArch arch, Set<EpistemicFormula> formulas) {

        // Set the reasoner to return false for these formulas
        arch.getReasonerSDKSpy().setFormulaValuation(formulas, false);

        for (var formula : formulas) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getCleanedOriginal(), new Unifier());

            assertNull(iter, "iter should be null since formula should evaluate to false");

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(PredicateIndicator.class));

        }

    }


    private static Stream<Arguments> unmanagedGroundFormula() {
        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildFormulas("know(unmanaged('Alice'))")
                )
        );
    }

    /**
     * @return AgArch fixture that contains query beliefs that should not be
     * in the belief base.
     */
    private static Stream<Arguments> candidateNonBeliefLiterals() {
        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildQueryBeliefs(
                                "falsetest",
                                "nontest"
                        )

                )
        );
    }

    /**
     * @return AgArch fixture that contains query beliefs that should be
     * in the belief base.
     */
    private static Stream<Arguments> candidateBeliefLiterals() {

        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildQueryBeliefsFromInitial()
                )
        );
    }

    private static Stream<Arguments> candidateGroundFormulas() {

        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildFormulas(ALL_EPISTEMIC_TEMPLATES)
                )
        );
    }


}