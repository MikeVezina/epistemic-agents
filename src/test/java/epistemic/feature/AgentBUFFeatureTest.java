package epistemic.feature;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import epistemic.formula.EpistemicFormula;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSyntax.Trigger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.TestUtils.addBeliefPlan;

/**
 * Tests the Variable formula feature
 */
public class AgentBUFFeatureTest {

    private static Stream<Arguments> formulaFixture() {
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
    @MethodSource(value = "formulaFixture")
    void testAgentBUF(StubAgArch stubArch, Set<EpistemicFormula> planFormulaSet, Set<EpistemicFormula> percepts) throws JasonException {
        var ag = stubArch.getAgSpy();
        var pl = ag.getPL();
        List<Event> tsEvents = new ArrayList<>();

        for(var formula : planFormulaSet)
        {
            // Create the belief addition plan
            var beliefPlan = addBeliefPlan(formula.getCleanedOriginal().toString());

            // Set all candidate formulas to evaluate to true
            var candidates = ag.getCandidateFormulas(formula);
            stubArch.getReasonerSDKSpy().setFormulaValuation(ag.getCandidateFormulas(formula), true);

            for(var candidate : candidates) {
                var trigger = new Trigger(beliefPlan.getTrigger().getOperator(), beliefPlan.getTrigger().getType(), candidate.getCleanedOriginal());
                tsEvents.add(new Event(trigger, Intention.EmptyInt));
            }
            pl.add(beliefPlan);
        }

        ag.buf(List.of());
        assertTSEvents(stubArch, tsEvents);
    }

    static void assertTSEvents(AgArch arch, List<Event> events)
    {
        assertTrue(arch.getTS().getC().getEvents().containsAll(events), "ts must contain all events");
    }

}
