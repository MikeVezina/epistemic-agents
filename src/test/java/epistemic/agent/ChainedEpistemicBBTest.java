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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static utils.TestUtils.*;

public class ChainedEpistemicBBTest {



    @Test
    public void contains() {

    }

    @ParameterizedTest
    @MethodSource(value = "candidateNonBeliefLiterals")
    public void getNonCandidateBeliefsFromBB(AgArchFixture agArchFixture) {
        var arch = agArchFixture.getAgArchSpy();

        for(var belief : agArchFixture.getBeliefs())
        {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertNull(iter, "iter should be null if belief does not exist in BB");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any());
        }

        verify(arch.getBeliefBaseSpy(), times(agArchFixture.getBeliefs().size())).getCandidateBeliefs(any(Literal.class), any());
    }


    @ParameterizedTest
    @MethodSource(value = "candidateBeliefBaseLiterals")
    public void getCandidateBeliefsFromBB(AgArchFixture agArchFixture) {
        var arch = agArchFixture.getAgArchSpy();

        for(var belief : agArchFixture.getBeliefs())
        {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(belief, new Unifier());

            assertEquals(belief.clearAnnots(), iter.next().clearAnnots(), "belief should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one belief should be returned since it is ground");

            // Verify that the internal belief base (not the reasoner) gets called since
            // this is not an epistemic formula
            verify(arch.getBeliefBaseSpy()).getCandidateBeliefs(eq(belief), any());
        }

        verify(arch.getBeliefBaseSpy(), times(agArchFixture.getBeliefs().size())).getCandidateBeliefs(any(Literal.class), any());
    }

    @ParameterizedTest
    @MethodSource(value = "candidateBeliefGroundFormulas")
    public void getCandidateBeliefsFromReasoner(AgArchFixture agArchFixture) {
        var arch = agArchFixture.getAgArchSpy();

        for(var formula : agArchFixture.getFormulas())
        {
            var iter = arch.getAgSpy().getBB().getCandidateBeliefs(formula.getOriginalLiteral(), new Unifier());

            assertEquals(formula.getOriginalLiteral(), iter.next(), "formula should be returned since it is ground");
            assertFalse(iter.hasNext(), "only one formula should be returned since it is ground");

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
                Arguments.of(
                        createAgArchFixture(
                                List.of(
                                        "falsetest",
                                        "nontest"
                                )
                        )
                )
        );
    }

    private static Stream<Arguments> candidateBeliefBaseLiterals() {
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


}