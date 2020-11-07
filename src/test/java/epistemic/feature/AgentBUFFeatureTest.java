package epistemic.feature;

import epistemic.agent.stub.StubAgArch;
import epistemic.fixture.AgArchFixtureBuilder;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSyntax.Plan;
import jason.asSyntax.Trigger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.TestUtils.toTriggerList;

/**
 * Tests the Variable formula feature
 */
public class AgentBUFFeatureTest {

    private static Stream<Arguments> formulaFixture() {
        var builder = new AgArchFixtureBuilder(TestUtils.DEFAULT_DISTRIBUTION_FIXTURE);
        return Stream.of(
                createBUFArguments(builder,
                        toTriggerList("+know(hand('Alice', Card))"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))",
                                "+know(hand('Alice', '88'))",
                                "+know(hand('Alice', 'A8'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(hand(Name, Card))"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))", "+know(hand('Alice', 'A8'))", "+know(hand('Alice', '88'))",
                                "+know(hand('Bob', 'AA'))", "+know(hand('Bob', 'A8'))", "+know(hand('Bob', '88'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(hand('Alice', 'AA'))"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(ns::hand('Alice', 'AA'))"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(NS::hand('Alice', 'AA'))"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(NS::~hand('Alice', Card))[Annot, _]"),
                        toTriggerList(
                                "+know(~hand('Alice', 'AA'))", "+know(~hand('Alice', '88'))", "+know(~hand('Alice', 'A8'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(NS::~hand('Alice', Card))[Card, _]"),
                        toTriggerList(
                                "+know(~hand('Alice', 'AA'))", "+know(~hand('Alice', '88'))", "+know(~hand('Alice', 'A8'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(hand('Alice', 'AA')[Card])"),
                        toTriggerList(
                                "+know(hand('Alice', 'AA'))"
                        )
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(badhand('Alice', Card))"),
                        toTriggerList()
                ),
                createBUFArguments(builder,
                        toTriggerList("+know(ns::hand('Alice', ns::Card))"),
                        toTriggerList("+know(hand('Alice', 'AA'))",
                                "+know(hand('Alice', 'A8'))",
                                "+know(hand('Alice', '88'))")
                )
        );
    }



    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "formulaFixture")
    void testAgentBUF(StubAgArch stubArch,List<Event> expectedEvents) throws JasonException {
        var ag = stubArch.getAgSpy();

        // All formulas should resolve to true.
        stubArch.getReasonerSDKSpy().setDefaultValuation(true);

        ag.buf(List.of());
        assertEquals(expectedEvents.size(), stubArch.getTS().getC().getEvents().size(),"ts should have all events");
        assertTSEvents(stubArch, expectedEvents);
    }

    @Tag(value = "feature")
    @ParameterizedTest(name = "Knowledge deletion events, generated from => Events {1} and Plans {2}")
    @MethodSource(value = "formulaFixture")
    void testTrueThenFalseFormulaAgentBUF(StubAgArch stubArch,List<Event> expectedEvents, List<Plan> plans) throws JasonException {
        var ag = stubArch.getAgSpy();

        // All formulas should resolve to false at first.
        stubArch.getReasonerSDKSpy().setDefaultValuation(true);

        // Run the BUF
        ag.buf(List.of());

        assertEquals(expectedEvents.size(), stubArch.getTS().getC().getEvents().size(),"ts should have generated positive events");
        assertTSEvents(stubArch, expectedEvents);

        // We don't want to look at the addition events
        stubArch.getTS().getC().clearEvents();

        // All formulas should now resolve to false (re-run BUF)
        stubArch.getReasonerSDKSpy().setDefaultValuation(false);

        // convert the expected events and plans to handle del belief events
        for(var event : expectedEvents)
            event.getTrigger().setTrigOp(Trigger.TEOperator.del);

        // Clear the PL and add negation events
        ag.getPL().clear();
        for(var plan : plans) {
            plan.getTrigger().setTrigOp(Trigger.TEOperator.del);
            ag.getPL().add(plan);
        }

        // This should generate negated events
        ag.buf(List.of());
        assertEquals(expectedEvents.size(), stubArch.getTS().getC().getEvents().size(),"ts should have generated the negative events");
        assertTSEvents(stubArch, expectedEvents);
    }

    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "formulaFixture")
    void testFalseThenTrueFormulaAgentBUF(StubAgArch stubArch,List<Event> expectedEvents) throws JasonException {
        var ag = stubArch.getAgSpy();

        // All formulas should resolve to false at first.
        stubArch.getReasonerSDKSpy().setDefaultValuation(false);

        // Run the BUF
        ag.buf(List.of());

        // All formulas should now resolve to true (re-run BUF)
        stubArch.getReasonerSDKSpy().setDefaultValuation(true);
        ag.buf(List.of());

        assertEquals(expectedEvents.size(), stubArch.getTS().getC().getEvents().size(),"ts should have all events");
        assertTSEvents(stubArch, expectedEvents);
    }

    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "formulaFixture")
    void testNoUpdateAgentBUF(StubAgArch stubArch) throws JasonException {
        var ag = stubArch.getAgSpy();

        stubArch.getEpistemicDistributionSpy().setShouldUpdate(false);

        // All formulas should resolve to false
        stubArch.getReasonerSDKSpy().setDefaultValuation(true);

        ag.buf(List.of());
        assertTrue(stubArch.getTS().getC().getEvents().isEmpty(), "no events should be added when all formulas evaluate to false");
    }

    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "formulaFixture")
    void testTrueToTrueNoUpdateAgentBUF(StubAgArch stubArch) throws JasonException {
        var ag = stubArch.getAgSpy();

        // All formulas should resolve to false
        stubArch.getReasonerSDKSpy().setDefaultValuation(true);

        // Generate add events and clear them
        ag.buf(List.of());
        stubArch.getTS().getC().clearEvents();

        // Add events should not be added again.
        ag.buf(List.of());
        assertTrue(stubArch.getTS().getC().getEvents().isEmpty(), "no events should be added when formula evaluation hasn't changed");
    }

    @Tag(value = "feature")
    @ParameterizedTest
    @MethodSource(value = "formulaFixture")
    void testFalseFirstValuationAgentBUF(StubAgArch stubArch) throws JasonException {
        var ag = stubArch.getAgSpy();

        // All formulas should resolve to true.
        stubArch.getReasonerSDKSpy().setDefaultValuation(false);

        ag.buf(List.of());
        assertTrue(stubArch.getTS().getC().getEvents().isEmpty(), "no events should be added when no updates occur");
    }

    static void assertTSEvents(AgArch arch, List<Event> events) {
        assertTrue(events.containsAll(arch.getTS().getC().getEvents()), "ts must contain all events. \r\nExpected: " + events + " \nActual: " + arch.getTS().getC().getEvents());
    }

    static Arguments createBUFArguments(AgArchFixtureBuilder builder, List<Trigger> planTriggers, List<Trigger> expectedEventTriggers) {
        // Convert plan triggers to blank plans
        List<Plan> plans = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        for (var trigger : planTriggers)
            plans.add(new Plan(null, trigger, null, null));

        for (var trigger : expectedEventTriggers)
            events.add(new Event(trigger, Intention.EmptyInt));

        // Add all parsed plans to the plan library
        var arch = builder.buildArchSpy();

        try {
            arch.getAgSpy().getPL().addAll(plans);
        } catch (JasonException e) {
            throw new AssertionError("Failed to add plans to PL: ", e);
        }

        return Arguments.of(
                arch,
                events,
                plans
        );
    }


}
