package epistemic;

import aggregators.PropAggregator;
import aggregators.PropositionAggregator;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.ASSyntax;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createString;
import static org.junit.jupiter.api.Assertions.*;


public class ValidPropositionTest {

    public ValidPropositionTest() {
    }

    private static Stream<Arguments> validTestPropositionFixture() {
        return Stream.of(
                Arguments.of("hand('Alice', Card)", "hand('Alice', 'AA')"),
                Arguments.of("ns::hand('Alice', Card)", "hand('Alice', 'AA')"),
                Arguments.of("hand('Alice', Card)", "ns::hand('Alice', 'AA')"),
                Arguments.of("ns::hand('Alice', Card)", "ns::hand('Alice', 'AA')")
        );
    }


    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void getKey(@PropAggregator Proposition currentProposition) {
        assertNotNull(currentProposition.getKey(), "key should not be null");
        assertTrue(currentProposition.getKey().isNormalized(), "key should be normalized");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void getValue(@PropAggregator Proposition currentProposition) {
        assertNotNull(currentProposition.getValue(), "value should not be null");
        assertTrue(currentProposition.getValue().isNormalized(), "value should be normalized");
        assertTrue(currentProposition.getValue().getOriginalLiteral().isGround(), "value should be ground");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void getKeyLiteral(@PropAggregator Proposition currentProposition) {
        assertNotNull(currentProposition.getKeyLiteral(), "key literal should not be null");
        assertEquals(currentProposition.getKeyLiteral(), currentProposition.getKey().getOriginalLiteral(), "key literal should not be the same as the original wrapped key literal");
        assertTrue(new WrappedLiteral(currentProposition.getKeyLiteral()).isNormalized(), "key literal should be normalized");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void getValueLiteral(@PropAggregator Proposition currentProposition) {
        assertNotNull(currentProposition.getValueLiteral(), "value literal should not be null");
        assertEquals(currentProposition.getValueLiteral(), currentProposition.getValue().getOriginalLiteral(), "value literal should not be the same as the original wrapped value literal");
        assertTrue(new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized(), "value literal should be normalized");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void setValue(@PropAggregator Proposition currentProposition) {
        currentProposition.setValue(new WrappedLiteral(createLiteral("hand", createString("Bob"))));
        assertTrue(new WrappedLiteral(currentProposition.getValueLiteral()).isNormalized(), "set value should be rejected since it doesnt unify with the key");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void testEquals(@PropAggregator Proposition currentProposition) {
        var clonedProposition = cloneProposition(currentProposition);
        assertEquals(clonedProposition, currentProposition, "clone should equal current prop");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    public void testHashCode(@PropAggregator Proposition currentProposition) {
        var clonedProposition = cloneProposition(currentProposition);
        assertEquals(clonedProposition.hashCode(), currentProposition.hashCode(), "clone hash should equal current prop hash");
    }

    @ParameterizedTest
    @MethodSource(value = "validTestPropositionFixture")
    private Proposition cloneProposition(Proposition current) {
        if (current == null)
            return null;

        return new Proposition(current.getKey(), current.getValue());
    }
}