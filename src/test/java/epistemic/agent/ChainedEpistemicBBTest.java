package epistemic.agent;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import epistemic.formula.EpistemicFormula;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
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
    private static final AgArchFixtureBuilder BUILDER = new AgArchFixtureBuilder(DEFAULT_DISTRIBUTION_FIXTURE)
            .initialBeliefs("test");

    @ParameterizedTest
    @MethodSource(value = "candidateNonBeliefLiterals")
    public void getNoCandidateBeliefsFromBB(StubAgArch arch, List<Literal> queryBeliefs) {

        for (var belief : queryBeliefs) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertNull(iter, "iter should be null since belief should not exist in BB");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any());
        }

        verify(arch.getBeliefBaseSpy(), times(queryBeliefs.size())).getCandidateBeliefs(any(Literal.class), any());
    }


    @ParameterizedTest
    @MethodSource(value = "candidateBeliefBaseLiterals")
    public void getCandidateBeliefsFromBB(StubAgArch arch, List<Literal> queryBeliefs) {

        for (var belief : queryBeliefs) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertEquals(belief.clearAnnots(), iter.next().clearAnnots(), "belief should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one belief should be returned since it is ground");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any());
        }

        verify(arch.getBeliefBaseSpy(), times(queryBeliefs.size())).getCandidateBeliefs(any(Literal.class), any());
    }

    @ParameterizedTest
    @MethodSource(value = "candidateBeliefGroundFormulas")
    public void getCandidateBeliefsFromReasoner(StubAgArch arch, Set<EpistemicFormula> formulas) {

        // Set the reasoner to return true for these formulas
        arch.getReasonerSDKSpy().setFormulaValuation(formulas, true);

        for (var formula : formulas) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getOriginalLiteral(), new Unifier());

            assertEquals(formula.getOriginalLiteral(), iter.next(), "formula should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one formula should be returned since it is ground");

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(), any());

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(), any());

        }

    }

    @ParameterizedTest
    @MethodSource(value = "candidateBeliefGroundFormulas")
    public void getNoCandidateBeliefsFromReasoner(StubAgArch arch, Set<EpistemicFormula> formulas) {

        // Set the reasoner to return false for these formulas
        arch.getReasonerSDKSpy().setFormulaValuation(formulas, false);

        for (var formula : formulas) {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getOriginalLiteral(), new Unifier());

            assertNull(iter, "iter should be null since formula should evaluate to false");

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(), any());

            // Verify that the internal belief base does not get called since this is a managed epistemic formula
            verify(arch.getBeliefBaseSpy(), times(0)).getCandidateBeliefs(any(), any());

        }

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
    private static Stream<Arguments> candidateBeliefBaseLiterals() {

        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildQueryBeliefsFromInitial()
                )
        );
    }

    private static Stream<Arguments> candidateBeliefGroundFormulas() {

        return Stream.of(
                BUILDER.buildArguments(
                        BUILDER.buildFormulas(ALL_EPISTEMIC_TEMPLATES)
                )
        );
    }


}