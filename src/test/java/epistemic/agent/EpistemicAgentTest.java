package epistemic.agent;

import epistemic.EpistemicDistribution;
import epistemic.agent.stub.StubAgArch;
import epistemic.formula.EpistemicFormula;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.converters.FormulaArg;
import utils.converters.LiteralArg;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static utils.TestUtils.createFormula;
import static utils.TestUtils.createLiteral;


public class EpistemicAgentTest {
    private EpistemicAgent epistemicAgent;
    private EpistemicDistribution epistemicDistribution;


    @BeforeEach
    public void setUp() throws Exception {
        // Sets up the stub AgArch.
        StubAgArch stubAgArch = new StubAgArch();

        // The epistemic agent and distribution are wrapped with spy objects
        epistemicAgent = stubAgArch.getAg();
        epistemicAgent.agentLoaded();

        epistemicDistribution = epistemicAgent.getEpistemicDistribution();

    }


    @ParameterizedTest
    @MethodSource(value = "bufValidFixture")
    public void testBuf(Collection<Literal> percepts) {
        assertDoesNotThrow(() -> {
            epistemicAgent.buf(percepts);
        });

        // Ensure BRF calls epistemic distribution
        verify(epistemicDistribution).buf(percepts, epistemicAgent.getPL().getSubscribedFormulas());
    }

    @ParameterizedTest
    @MethodSource(value = "brfValidFixture")
    public void testBrf(@LiteralArg Literal add, @LiteralArg Literal del) {
        assertDoesNotThrow(() -> {
            epistemicAgent.brf(add, del, null);
        });

        // Ensure BRF calls epistemic distribution
        verify(epistemicDistribution).brf(add, del);
    }


    @Test
    void testLoadString() {
        // This should be called once already from setup.
        assertDoesNotThrow(() -> epistemicAgent.load(""));

        // This should be called twice. One agentLoaded call occurs in setup
        verify(epistemicAgent, times(2)).agentLoaded();
    }

    @Test
    void testLoadStream() {
        InputStream stream = mock(InputStream.class);
        assertDoesNotThrow(() -> epistemicAgent.load(stream, ""));

        // This should be called twice. One agentLoaded call occurs in setup
        verify(epistemicAgent, times(2)).agentLoaded();
    }

    @Test
    void testAgentLoaded() {
        epistemicAgent.agentLoaded();

        // Ensure BRF calls epistemic distribution
        verify(epistemicDistribution).agentLoaded();
    }

    @Test
    void testSetPLNonProxy() {
        EpistemicPlanLibraryProxy original = epistemicAgent.getPL();

        PlanLibrary library = new PlanLibrary();
        epistemicAgent.setPL(library);

        assertNotSame(original, epistemicAgent.getPL());
    }

    @Test
    void testSetPLNewProxy() {
        EpistemicPlanLibraryProxy original = epistemicAgent.getPL();

        EpistemicPlanLibraryProxy library = new EpistemicPlanLibraryProxy(new PlanLibrary());
        epistemicAgent.setPL(library);

        assertNotSame(original, epistemicAgent.getPL());
        assertSame(library, epistemicAgent.getPL());
    }

    @Test
    void getEpistemicDistribution() {
        assertNotNull(epistemicAgent.getEpistemicDistribution(), "epistemic distribution should not be null");
    }


    /**
     * This just tests the basic input / output of the getCandidateFormula function.
     * Integration/Feature tests will test this more thoroughly due to the interactions
     * with epistemicDistribution.
     *
     * @param formula The formula to test
     */
    @ParameterizedTest
    @MethodSource(value = "candidateFormulaFixture")
    void testGetCandidateFormulas(@FormulaArg EpistemicFormula formula, Set<EpistemicFormula> resultSet) {
        assertEquals(resultSet, epistemicAgent.getCandidateFormulas(formula) , "result sets should be equal");
    }

    private static Stream<Arguments> candidateFormulaFixture() {
        return Stream.of(
                Arguments.of(
                        null,
                        Set.of()
                ),

                Arguments.of(
                        "know(alice(test))",
                        Set.of(
                                createFormula("know(alice(test))")
                        )
                )
        );
    }

    private static Stream<Arguments> brfValidFixture() {
        return Stream.of(
                Arguments.of("test", "test"),
                Arguments.of("test", null),
                Arguments.of(null, "test"),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> bufValidFixture() {
        return Stream.of(
                Arguments.of(
                        Set.of()
                ),

                Arguments.of(
                        Set.of(
                                createLiteral("welcome")
                        )
                )
        );
    }


}