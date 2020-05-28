package epistemic.agent;

import epistemic.EpistemicDistribution;
import epistemic.agent.mock.MockAgArch;
import epistemic.agent.mock.MockEpistemicAgent;
import jason.asSyntax.Literal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import utils.converters.LiteralArg;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class EpistemicAgentTest {
    private MockEpistemicAgent epistemicAgent;
    private EpistemicDistribution epistemicDistribution;
    private MockAgArch mockAgArch;


    @BeforeEach
    public void setUp() throws Exception {
        // Sets up the mock AgArch.
        this.mockAgArch = new MockAgArch();

        epistemicAgent = mockAgArch.getAg();
        epistemicDistribution = epistemicAgent.getEpistemicDistribution();
        verify(epistemicDistribution).agentLoaded();
    }

    @Test
    public void load() {
    }

    @Test
    public void testLoad() {
    }

    @Test
    public void initAg() {
    }

    @Test
    public void setPL() {
    }

    @Test
    public void getPL() {
    }

    @ParameterizedTest
    @MethodSource(value = "bufValidFixture")
    public void buf() {
    }

    @ParameterizedTest
    @CsvSource(value = {
            "test, test",
    })
    public void brf(@LiteralArg Literal add, @LiteralArg Literal del) {
        Assertions.assertDoesNotThrow(() -> {
            epistemicAgent.brf(add, del, null);
        });

        // Ensure BRF calls epistemic distribution
        verify(epistemicDistribution).brf(add, del);
    }

    @Test
    public void createKnowledgeEvent() {
    }

    @Test
    public void getCandidateFormulas() {
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
                Arguments.of()
        );
    }
}