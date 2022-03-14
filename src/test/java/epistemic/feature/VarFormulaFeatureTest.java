package epistemic.feature;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import epistemic.distribution.formula.EpistemicFormula;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.TestUtils;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Variable formula feature
 */
public class VarFormulaFeatureTest {

    private static Stream<Arguments> variableFormulaCandidate() {
        var builder = new AgArchFixtureBuilder(TestUtils.DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                builder.buildArguments(
                        builder.buildFormulas("know(hand('Alice', Card))"),
                        builder.buildFormulas("know(hand('Alice', 'AA'))", "know(hand('Alice', 'A8'))", "know(hand('Alice', '88'))") // Expected candidates
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(hand(Name, Card))"),
                        builder.buildFormulas(
                                "know(hand('Alice', 'AA'))", "know(hand('Alice', 'A8'))", "know(hand('Alice', '88'))",
                                "know(hand('Bob', 'AA'))", "know(hand('Bob', 'A8'))", "know(hand('Bob', '88'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(hand('Alice', 'AA'))"),
                        builder.buildFormulas("know(hand('Alice', 'AA'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(ns::hand('Alice', 'AA'))"),
                        builder.buildFormulas("know(ns::hand('Alice', 'AA'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(NS::hand('Alice', 'AA'))"),
                        builder.buildFormulas("know(NS::hand('Alice', 'AA'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(ns::hand('Alice', 'AA'))"),
                        builder.buildFormulas("know(ns::hand('Alice', 'AA'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(NS::~hand('Alice', Card))[Annot, _]"),
                        builder.buildFormulas("know(NS::~hand('Alice', 'AA'))[Card, _]", "know(NS::~hand('Alice', '88'))[Card, _]", "know(NS::~hand('Alice', 'A8'))[Card, _]")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(NS::~hand('Alice', Card))[Card, _]"),
                        builder.buildFormulas("know(NS::~hand('Alice', 'AA'))", "know(NS::~hand('Alice', '88'))", "know(NS::~hand('Alice', 'A8'))")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(hand('Alice', 'AA')[Card])"),
                        builder.buildFormulas("know(hand('Alice', 'AA')[Card])")
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(ns::hand('Other', ns::Card))"),
                        builder.buildFormulas()
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(badhand('Alice', Card))"),
                        builder.buildFormulas()
                ),
                builder.buildArguments(
                        builder.buildFormulas("know(ns::hand('Alice', ns::Card))"),
                        builder.buildFormulas("know(ns::hand('Alice', ns::'AA'))",
                                "know(ns::hand('Alice', ns::'A8'))",
                                "know(ns::hand('Alice', ns::'88'))")
                )
        );
    }

    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "variableFormulaCandidate")
    void testVariableCandidateFormulas(StubAgArch stubArch, Set<EpistemicFormula> formulaSet, Set<EpistemicFormula> expectedCandidates) {
        for (var formula : formulaSet) {
            var candidates = stubArch.getAgSpy().getCandidateFormulas(formula);

            assertEquals(expectedCandidates, candidates, "should return all candidate formulas");
        }
    }

}
