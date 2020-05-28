package epistemic.agent;

import epistemic.formula.EpistemicFormula;
import jason.JasonException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanLibrary;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TestUtils.aggregateLists;

class EpistemicPlanLibraryProxyTest {
    private EpistemicPlanLibraryProxy planLibraryProxy;
    private PlanLibrary planLibrary;

    @BeforeEach
    void setUp() {
        this.planLibrary = new PlanLibrary();
        this.planLibraryProxy = new EpistemicPlanLibraryProxy(this.planLibrary);
        assertEquals(this.planLibrary.size(), planLibraryProxy.size(), "proxy should be the same size as the internal object");
        assertEquals(0, planLibraryProxy.size(), "proxy should be empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void getSubscribedFormulas(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = new ArrayList<>();
        allPlans.addAll(subscribed);
        allPlans.addAll(other);

        // Add all plans to proxy
        planLibraryProxy.addAll(allPlans);

        assertSubscribedPlans(allPlans, subscribed, planLibrary);

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void getExistingSubscribedFormulas(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a new PlanLibrary
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);

        this.planLibraryProxy = new EpistemicPlanLibraryProxy(newLibrary);

        // Confirm plans were added to the proxy and that there are no duplicates
        assertSubscribedPlans(allPlans, subscribed, newLibrary);

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllList(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans using a list
        this.planLibraryProxy.addAll(allPlans);

        // Confirm all plans were added
        assertEquals(allPlans.size(), planLibraryProxy.size(),"proxy should have added all plans");
        assertEquals(allPlans.size(), planLibrary.size(),"proxy should have added all plans to internal object");

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllPL(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a separate PL
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);

        // Add all plans in the separate PL
        planLibraryProxy.addAll(newLibrary);

        // Confirm all plans were added
        assertEquals(allPlans.size(), planLibraryProxy.size(),"proxy should have added all plans");
        assertEquals(allPlans.size(), planLibrary.size(),"proxy should have added all plans to internal object");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllConstructor(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans using an existing PL
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);
        this.planLibraryProxy = new EpistemicPlanLibraryProxy(newLibrary);

        // Confirm all plans were added
        assertEquals(allPlans.size(), planLibraryProxy.size(),"proxy should have added all plans");
        assertEquals(newLibrary.size(), planLibraryProxy.size(),"proxy should have added all plans from new library");

    }


    private void assertSubscribedPlans(List<Plan> allPlans, List<Plan> subscribed, PlanLibrary planLibrary) {
        var subscribedFormulas = planLibraryProxy.getSubscribedFormulas();
        assertEquals(subscribed.size(), subscribedFormulas.size(), "all plans should have been added to subscribed formulas");

        for (Plan plan : subscribed) {
            var parsedFormula = EpistemicFormula.parseLiteral(plan.getTrigger().getLiteral());
            assertNotNull(parsedFormula, "formula should not be null here");
            assertTrue(subscribedFormulas.contains(parsedFormula), "epistemic formula was not added");
        }
    }

    private static Stream<Arguments> subscribedFormulasFixture() {
        var subscribedList = List.of( // List of random epistemic formulas
                addBeliefPlan("know(hand('Alice', Card))"),
                addBeliefPlan("~know(hand('Alice', Card))"),
                addBeliefPlan("know(~hand('Alice', Card))"),
                addBeliefPlan("know(~know(know(hand('Alice', Card))))"),
                addBeliefPlan("possible(hand('Alice', Card))"),
                addBeliefPlan("~possible(hand('Alice', Card))"),
                addBeliefPlan("know(~possible(know(hand('Alice', Card))))"),

                delBeliefPlan("know(hand('Alice', Card))"),
                delBeliefPlan("~know(hand('Alice', Card))"),
                delBeliefPlan("know(~hand('Alice', Card))"),
                delBeliefPlan("know(~know(know(hand('Alice', Card))))"),
                delBeliefPlan("possible(hand('Alice', Card))"),
                delBeliefPlan("~possible(hand('Alice', Card))"),
                delBeliefPlan("know(~possible(know(hand('Alice', Card))))")
        );

        var nonSubscribedList = List.of( // List of random epistemic formulas

                // Should not subscribe to +belief plans if the literal is not an epistemic formula
                addBeliefPlan("hand('Alice', Card)"),
                addBeliefPlan("test('Alice', Card)"),
                addBeliefPlan("wow(~hand('Alice', Card))"),
                addBeliefPlan("test_funct(~know(know(hand('Alice', Card))))"),
                addBeliefPlan("p(hand('Alice', Card))"),
                addBeliefPlan("~poss(hand('Alice', Card))"),
                addBeliefPlan("knows(~possible(know(hand('Alice', Card))))"),

                delBeliefPlan("hand('Alice', Card)"),
                delBeliefPlan("test('Alice', Card)"),
                delBeliefPlan("wow(~hand('Alice', Card))"),
                delBeliefPlan("test_funct(~know(know(hand('Alice', Card))))"),
                delBeliefPlan("p(hand('Alice', Card))"),
                delBeliefPlan("~poss(hand('Alice', Card))"),
                delBeliefPlan("knows(~possible(know(hand('Alice', Card))))"),

                // We should not subscribe to '+?' (test)
                addTestPlan("know(hand('Alice', Card))"),
                addTestPlan("~know(hand('Alice', Card))"),
                addTestPlan("know(~hand('Alice', Card))"),
                addTestPlan("know(~know(know(hand('Alice', Card))))"),
                addTestPlan("possible(hand('Alice', Card))"),
                addTestPlan("~possible(hand('Alice', Card))"),
                addTestPlan("know(~possible(know(hand('Alice', Card))))"),
                addTestPlan("hand('Alice', Card)"),
                addTestPlan("test('Alice', Card)"),
                addTestPlan("wow(~hand('Alice', Card))"),
                addTestPlan("test_funct(~know(know(hand('Alice', Card))))"),
                addTestPlan("p(hand('Alice', Card))"),
                addTestPlan("~poss(hand('Alice', Card))"),
                addTestPlan("knows(~possible(know(hand('Alice', Card))))"),

                // We should not subscribe to '-?' (test)
                delTestPlan("know(hand('Alice', Card))"),
                delTestPlan("~know(hand('Alice', Card))"),
                delTestPlan("know(~hand('Alice', Card))"),
                delTestPlan("know(~know(know(hand('Alice', Card))))"),
                delTestPlan("possible(hand('Alice', Card))"),
                delTestPlan("~possible(hand('Alice', Card))"),
                delTestPlan("know(~possible(know(hand('Alice', Card))))"),
                delTestPlan("hand('Alice', Card)"),
                delTestPlan("test('Alice', Card)"),
                delTestPlan("wow(~hand('Alice', Card))"),
                delTestPlan("test_funct(~know(know(hand('Alice', Card))))"),
                delTestPlan("p(hand('Alice', Card))"),
                delTestPlan("~poss(hand('Alice', Card))"),
                delTestPlan("knows(~possible(know(hand('Alice', Card))))"),

                // We should not subscribe to '+!' achieve plans
                addAchievePlan("know(hand('Alice', Card))"),
                addAchievePlan("~know(hand('Alice', Card))"),
                addAchievePlan("know(~hand('Alice', Card))"),
                addAchievePlan("know(~know(know(hand('Alice', Card))))"),
                addAchievePlan("possible(hand('Alice', Card))"),
                addAchievePlan("~possible(hand('Alice', Card))"),
                addAchievePlan("know(~possible(know(hand('Alice', Card))))"),
                addAchievePlan("hand('Alice', Card)"),
                addAchievePlan("test('Alice', Card)"),
                addAchievePlan("wow(~hand('Alice', Card))"),
                addAchievePlan("test_funct(~know(know(hand('Alice', Card))))"),
                addAchievePlan("p(hand('Alice', Card))"),
                addAchievePlan("~poss(hand('Alice', Card))"),
                addAchievePlan("knows(~possible(know(hand('Alice', Card))))"),

                // We should not subscribe to '-!' achieve plans
                delAchievePlan("know(hand('Alice', Card))"),
                delAchievePlan("~know(hand('Alice', Card))"),
                delAchievePlan("know(~hand('Alice', Card))"),
                delAchievePlan("know(~know(know(hand('Alice', Card))))"),
                delAchievePlan("possible(hand('Alice', Card))"),
                delAchievePlan("~possible(hand('Alice', Card))"),
                delAchievePlan("know(~possible(know(hand('Alice', Card))))"),
                delAchievePlan("hand('Alice', Card)"),
                delAchievePlan("test('Alice', Card)"),
                delAchievePlan("wow(~hand('Alice', Card))"),
                delAchievePlan("test_funct(~know(know(hand('Alice', Card))))"),
                delAchievePlan("p(hand('Alice', Card))"),
                delAchievePlan("~poss(hand('Alice', Card))"),
                delAchievePlan("knows(~possible(know(hand('Alice', Card))))")

        );

        return Stream.of(
                Arguments.of(
                        List.of(), // Empty subscribed list
                        List.of() // Empty other list
                ),
                Arguments.of(
                        subscribedList,
                        List.of()
                ),
                Arguments.of(
                        List.of(),
                        nonSubscribedList
                ),
                Arguments.of(
                        subscribedList,
                        nonSubscribedList
                )
        );
    }


    private static Plan addBeliefPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.belief, triggerHead);
    }

    private static Plan delBeliefPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.belief, triggerHead);
    }

    private static Plan addAchievePlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.achieve, triggerHead);
    }

    private static Plan delAchievePlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.achieve, triggerHead);
    }

    private static Plan addTestPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.add, Trigger.TEType.test, triggerHead);
    }

    private static Plan delTestPlan(String triggerHead) {
        return addPlan(Trigger.TEOperator.del, Trigger.TEType.test, triggerHead);
    }

    private static Plan addPlan(Trigger.TEOperator op, Trigger.TEType type, String triggerHead)
    {
        try {
            Trigger te = new Trigger(op, type, ASSyntax.parseLiteral(triggerHead));
            return new Plan(null, te, null, null);
        } catch (ParseException e) {
            throw new AssertionError("failed to create belief plan for " + triggerHead + ". Message: " + e.getMessage());
        }
    }
}