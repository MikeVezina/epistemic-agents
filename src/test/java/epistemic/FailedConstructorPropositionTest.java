package epistemic;

import converters.WrappedLiteralArg;
import epistemic.wrappers.WrappedLiteral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FailedConstructorPropositionTest  {

    public FailedConstructorPropositionTest() {

    }

    @ParameterizedTest
    @MethodSource(value = "failToUnifyFixture")
    @DisplayName("Fail to unify key/value in proposition")
    public void failToUnifyFixture(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Proposition(key, value));
        assertEquals(exception.getMessage(), "The literalValue can not unify the literalKey. Failed to create Proposition.");
    }

    @ParameterizedTest
    @MethodSource(value = "valueNotGroundFixture")
    @DisplayName("Fail to create proposition, value must be ground.")
    public void valueNotGroundFixture(@WrappedLiteralArg WrappedLiteral key, @WrappedLiteralArg WrappedLiteral value) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Proposition(key, value));
        assertEquals(exception.getMessage(), "literalValue is not ground");

    }

    private static Stream<Arguments> failToUnifyFixture() {
        return Stream.of(
                Arguments.of("test('Bob')", "test('Bob', 'Test')"),
                Arguments.of("test('Alice', 'Test')", "test('Bob', 'Test')")
        );
    }

    private static Stream<Arguments> valueNotGroundFixture() {
        return Stream.of(
                Arguments.of("test('Bob', Test)", "test('Bob', Test)"),
                Arguments.of("test('Alice', Test)", "test('Bob', _)")
        );
    }

}