package epistemic.wrappers;

import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import utils.TestUtils;
import utils.converters.LiteralArg;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static utils.TestUtils.createLiteral;

class WrappedTermTest {


    @ParameterizedTest
    @MethodSource(value = "validWrappedTermFixture")
    public void testWrappedTerm(@LiteralArg Term literal) {
        WrappedTerm wrappedTerm = new WrappedTerm(literal);

        // Get the list of wrapped terms from the wrapped literal
        assertEquals(literal, wrappedTerm.getOriginalTerm(), "the original term should be returned ");

    }

    @ParameterizedTest
    @MethodSource(value = "equalWrappedTermFixture")
    public void testWrappedTerm(@LiteralArg Term literal, @LiteralArg Term literalTwo) {
        WrappedTerm wrappedTerm = new WrappedTerm(literal);
        WrappedTerm wrappedTermTwo = new WrappedTerm(literal);

        // Get the list of wrapped terms from the wrapped literal
        assertEquals(wrappedTerm.hashCode(), wrappedTermTwo.hashCode(), "should provide the same hash");
        assertEquals(wrappedTerm, wrappedTermTwo.getOriginalTerm(), "should be equal terms ");

    }

    @ParameterizedTest
    @MethodSource(value = "notEqualWrappedTermFixture")
    public void testDifferentWrappedTerm(@LiteralArg Term literal, @LiteralArg Term literalTwo) {
        WrappedTerm wrappedTerm = new WrappedTerm(literal);
        WrappedTerm wrappedTermTwo = new WrappedTerm(literalTwo);

        // Get the list of wrapped terms from the wrapped literal
        assertNotEquals(wrappedTerm.hashCode(), wrappedTermTwo.hashCode(), "should not share the same hash");
        assertNotEquals(wrappedTerm, wrappedTermTwo.getOriginalTerm(), "should not be equal ");

    }

    @Test
    public void testWrappedTermNotTerm() {
        WrappedTerm wrappedTerm = new WrappedTerm(createLiteral("test"));

        // Get the list of wrapped terms from the wrapped literal
        assertNotEquals(wrappedTerm, "not term", "the original term should return false on non-term object");

    }

    private static Stream<Arguments> equalWrappedTermFixture() {
        return Stream.of(
                Arguments.of(
                        "test(Var)",
                        "test(Var)"
                ),
                Arguments.of(
                        "test(Test)",
                        "test(Test)"
                ),
                Arguments.of(
                        "test(asd)",
                        "test(asd)"
                ),
                Arguments.of(
                        "test(Var)",
                        "test(OtherVar)"
                ),
                Arguments.of(
                        "test(Var)",
                        "test(_)"
                ),
                Arguments.of(
                        "test(_)",
                        "test(_)"
                ),
                Arguments.of(
                        "test(asd, Var)",
                        "test(asd, Var)"
                ),
                Arguments.of(
                        "test(asd, Var)",
                        "test(asd, _)"
                ),
                Arguments.of(
                        "test(test(_), Var)",
                        "test(test(Var), Var)"
                )
        );
    }

    private static Stream<Arguments> notEqualWrappedTermFixture() {
        return Stream.of(
                Arguments.of(
                        "test(Var)",
                        "test(asd)"
                ),
                Arguments.of(
                        "test(Test)",
                        "test(Test, asd)"
                ),
                Arguments.of(
                        "test(asd)",
                        "test(_)"
                ),
                Arguments.of(
                        "test(Var)",
                        "test(OtherVar, _)"
                ),
                Arguments.of(
                        "test(Var)",
                        "testOther(Var)"
                ),
                Arguments.of(
                        "test",
                        "testother"
                ),
                Arguments.of(
                        "test(asd, Var)",
                        "test(Var, Var)"
                ),
                Arguments.of(
                        "test(asd, Var)",
                        "test(_, _)"
                ),
                Arguments.of(
                        "test(test(_), Var)",
                        "test(test(asd), Var)"
                )
        );
    }

    private static Stream<Arguments> validWrappedTermFixture() {
        return TestUtils.flattenArguments(
                Stream.of(
                        Arguments.of(
                                "test(Wow)",
                                ASSyntax.createNumber(1),
                                ASSyntax.createString("test"),
                                new WrappedTerm(ASSyntax.createNumber(1)),
                                new WrappedTerm(ASSyntax.createString("test")),
                                new WrappedTerm(createLiteral("test(Var)")),
                                new WrappedTerm(createLiteral("test(Var)")),
                                new WrappedTerm(ASSyntax.createLiteral("test", new WrappedTerm(createLiteral("inner"))))
                        )
                )
        );
    }

    @Test
    public void testProxiedMethods() {
        Term mockTerm = mock(Term.class);
        WrappedTerm wrappedTerm = new WrappedTerm(mockTerm);

        // All other methods should be proxied
        assertAll("all proxied methods should equal the original literal", () -> {
            wrappedTerm.isLiteral();
            verify(mockTerm, times(1)).isLiteral();
            wrappedTerm.isString();
            verify(mockTerm, times(1)).isString();
            wrappedTerm.isArithExpr();
            verify(mockTerm, times(1)).isArithExpr();
            wrappedTerm.isAtom();
            verify(mockTerm, times(1)).isAtom();
            wrappedTerm.isCyclicTerm();
            verify(mockTerm, times(1)).isCyclicTerm();
            wrappedTerm.isGround();
            verify(mockTerm, times(1)).isGround();
            wrappedTerm.isInternalAction();
            verify(mockTerm, times(1)).isInternalAction();
            wrappedTerm.isList();
            verify(mockTerm, times(1)).isList();
            wrappedTerm.isNumeric();
            verify(mockTerm, times(1)).isNumeric();
            wrappedTerm.isPlanBody();
            verify(mockTerm, times(1)).isPlanBody();
            wrappedTerm.isPred();
            verify(mockTerm, times(1)).isPred();
            wrappedTerm.isRule();
            verify(mockTerm, times(1)).isRule();
            wrappedTerm.isStructure();
            verify(mockTerm, times(1)).isStructure();
            wrappedTerm.isUnnamedVar();
            verify(mockTerm, times(1)).isUnnamedVar();
            wrappedTerm.isMap();
            verify(mockTerm, times(1)).isMap();
            wrappedTerm.isSet();
            verify(mockTerm, times(1)).isSet();
            wrappedTerm.isVar();
            verify(mockTerm, times(1)).isVar();
            wrappedTerm.getCyclicVar();
            verify(mockTerm, times(1)).getCyclicVar();
            wrappedTerm.clone();
            verify(mockTerm, times(1)).clone();
            wrappedTerm.getSrcInfo();
            verify(mockTerm, times(1)).getSrcInfo();


            /* Ensure method parameters are passed to the original object */

            Document doc = mock(Document.class);
            wrappedTerm.getAsDOM(doc);
            verify(mockTerm, times(1)).getAsDOM(same(doc));

            String indent = "";
            wrappedTerm.getAsJSON(indent);
            verify(mockTerm, times(1)).getAsJSON(same(indent));

            Term term = mock(Term.class);
            wrappedTerm.compareTo(term);
            verify(mockTerm, times(1)).compareTo(same(term));
            wrappedTerm.subsumes(term);
            verify(mockTerm, times(1)).subsumes(same(term));


            VarTerm varTerm = mock(VarTerm.class);
            Unifier unifier = mock(Unifier.class);
            wrappedTerm.hasVar(varTerm, unifier);
            verify(mockTerm, times(1)).hasVar(same(varTerm), same(unifier));

            wrappedTerm.capply(unifier);
            verify(mockTerm, times(1)).capply(same(unifier));

            var srcInfo = mock(SourceInfo.class);
            wrappedTerm.setSrcInfo(srcInfo);
            verify(mockTerm, times(1)).setSrcInfo(same(srcInfo));

            Atom ns = ASSyntax.createAtom("ns");
            wrappedTerm.cloneNS(ns);
            verify(mockTerm, times(1)).cloneNS(same(ns));

            Map<VarTerm, Integer> empty = Collections.emptyMap();
            wrappedTerm.countVars(empty);
            verify(mockTerm, times(1)).countVars(refEq(empty));

        });

    }
}