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

class EpistemicPlanLibraryTest {
    private EpistemicPlanLibrary epistemicLibrary;

    @BeforeEach
    void setUp() {
        this.epistemicLibrary = new EpistemicPlanLibrary();
        assertEquals(0, epistemicLibrary.size(), "library should be empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void getSubscribedFormulas(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to library
        epistemicLibrary.addAll(allPlans);

        assertSubscribedPlans(subscribed);

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addPlan(List<Plan> subscribed, List<Plan> other) throws JasonException, ParseException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        /* Test all of the add(...) overloads */

        for (Plan p : allPlans)
            epistemicLibrary.add(p);

        assertSubscribedPlans(subscribed);
        epistemicLibrary.clear();

        for (Plan p : allPlans)
            epistemicLibrary.add(p, true);

        assertSubscribedPlans(subscribed);
        epistemicLibrary.clear();

        for (Plan p : allPlans)
            epistemicLibrary.add(p, ASSyntax.createString("source"), true);

        assertSubscribedPlans(subscribed);
        epistemicLibrary.clear();

        /*
         * Ensure the deprecated methods still register subscribed formula plans
         */

        for (Plan p : allPlans)
            epistemicLibrary.add(ASSyntax.createString(p.toASString()), ASSyntax.createString("source"), true);

        assertSubscribedPlans(subscribed);
        epistemicLibrary.clear();

        for (Plan p : allPlans)
            epistemicLibrary.add(ASSyntax.createString(p.toASString()), ASSyntax.createString("source"));

        assertSubscribedPlans(subscribed);
        epistemicLibrary.clear();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void getExistingSubscribedFormulas(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a new PlanLibrary
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);

        this.epistemicLibrary = new EpistemicPlanLibrary(newLibrary);

        // Confirm plans were added to the library and that there are no duplicates
        assertSubscribedPlans(subscribed);

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllList(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans using a list
        this.epistemicLibrary.addAll(allPlans);

        // Confirm all plans were added
        assertEquals(allPlans.size(), epistemicLibrary.size(), "library should have added all plans");

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllPL(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a separate PL
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);

        // Add all plans in the separate PL
        epistemicLibrary.addAll(newLibrary);

        // Confirm all plans were added
        assertEquals(allPlans.size(), epistemicLibrary.size(), "library should have added all plans");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void testClear(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a separate PL
        this.epistemicLibrary.addAll(allPlans);
        assertEquals(allPlans.size(), epistemicLibrary.size(), "should be equal size");

        this.epistemicLibrary.clear();
        assertEquals(0, epistemicLibrary.size(), "should be empty after clear");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void testClone(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans to a separate PL
        this.epistemicLibrary.addAll(allPlans);

        var clonedLibrary = epistemicLibrary.clone();

        assertNotSame(epistemicLibrary, clonedLibrary);
        assertEquals(epistemicLibrary.size(), clonedLibrary.size(), "should be equal size");
        assertEquals(epistemicLibrary.getSubscribedFormulas(), clonedLibrary.getSubscribedFormulas(), "should have same formulas");

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "subscribedFormulasFixture")
    void addAllConstructor(List<Plan> subscribed, List<Plan> other) throws JasonException {
        List<Plan> allPlans = aggregateLists(subscribed, other);

        // Add all plans using an existing PL
        PlanLibrary newLibrary = new PlanLibrary();
        newLibrary.addAll(allPlans);
        this.epistemicLibrary = new EpistemicPlanLibrary(newLibrary);

        // Confirm all plans were added
        assertEquals(allPlans.size(), epistemicLibrary.size(), "library should have added all plans");
        assertEquals(newLibrary.size(), epistemicLibrary.size(), "library should have added all plans from new library");

    }


    private void assertSubscribedPlans(List<Plan> subscribed) {
        var subscribedFormulas = epistemicLibrary.getSubscribedFormulas();
        assertEquals(subscribed.size(), subscribedFormulas.size(), "all plans should have been added to subscribed formulas");

        for (Plan plan : subscribed) {
            var parsedFormula = EpistemicFormula.fromLiteral(plan.getTrigger().getLiteral());
            assertNotNull(parsedFormula, "formula should not be null here");
            if (!subscribedFormulas.contains(parsedFormula)) {
                subscribedFormulas.get(9).getOriginalWrappedLiteral().hashCode();

            }

            assertTrue(subscribedFormulas.contains(parsedFormula), "epistemic formula " + parsedFormula + " was not added");
        }
    }

    private static Stream<Arguments> subscribedFormulasFixture() {
        var subscribedList = List.of( // List of random epistemic formulas
                addBeliefPlan("know(hand('alice', Card))"),
                addBeliefPlan("~know(hand('alice', Card))"),
                addBeliefPlan("know(~hand('alice', Card))"),
                addBeliefPlan("know(~know(know(hand('alice', Card))))"),
                addBeliefPlan("possible(hand('alice', Card))"),
                addBeliefPlan("~possible(hand('alice', Card))"),
                addBeliefPlan("know(~possible(know(hand('alice', Card))))"),

                delBeliefPlan("know(hand('alice', Card))"),
                delBeliefPlan("~know(hand('alice', Card))"),
                delBeliefPlan("know(~hand('alice', Card))"),
                delBeliefPlan("know(~know(know(hand('alice', Card))))"),
                delBeliefPlan("possible(hand('alice', Card))"),
                delBeliefPlan("~possible(hand('alice', Card))"),
                delBeliefPlan("know(~possible(know(hand('alice', Card))))")
        );

        var nonSubscribedList = List.of( // List of random epistemic formulas

                // Should not subscribe to +belief plans if the literal is not an epistemic formula
                addBeliefPlan("hand('alice', Card)"),
                addBeliefPlan("test('alice', Card)"),
                addBeliefPlan("wow(~hand('alice', Card))"),
                addBeliefPlan("test_funct(~know(know(hand('alice', Card))))"),
                addBeliefPlan("p(hand('alice', Card))"),
                addBeliefPlan("~poss(hand('alice', Card))"),
                addBeliefPlan("knows(~possible(know(hand('alice', Card))))"),

                delBeliefPlan("hand('alice', Card)"),
                delBeliefPlan("test('alice', Card)"),
                delBeliefPlan("wow(~hand('alice', Card))"),
                delBeliefPlan("test_funct(~know(know(hand('alice', Card))))"),
                delBeliefPlan("p(hand('alice', Card))"),
                delBeliefPlan("~poss(hand('alice', Card))"),
                delBeliefPlan("knows(~possible(know(hand('alice', Card))))"),

                // We should not subscribe to '+?' (test)
                addTestPlan("know(hand('alice', Card))"),
                addTestPlan("~know(hand('alice', Card))"),
                addTestPlan("know(~hand('alice', Card))"),
                addTestPlan("know(~know(know(hand('alice', Card))))"),
                addTestPlan("possible(hand('alice', Card))"),
                addTestPlan("~possible(hand('alice', Card))"),
                addTestPlan("know(~possible(know(hand('alice', Card))))"),
                addTestPlan("hand('alice', Card)"),
                addTestPlan("test('alice', Card)"),
                addTestPlan("wow(~hand('alice', Card))"),
                addTestPlan("test_funct(~know(know(hand('alice', Card))))"),
                addTestPlan("p(hand('alice', Card))"),
                addTestPlan("~poss(hand('alice', Card))"),
                addTestPlan("knows(~possible(know(hand('alice', Card))))"),

                // We should not subscribe to '-?' (test)
                delTestPlan("know(hand('alice', Card))"),
                delTestPlan("~know(hand('alice', Card))"),
                delTestPlan("know(~hand('alice', Card))"),
                delTestPlan("know(~know(know(hand('alice', Card))))"),
                delTestPlan("possible(hand('alice', Card))"),
                delTestPlan("~possible(hand('alice', Card))"),
                delTestPlan("know(~possible(know(hand('alice', Card))))"),
                delTestPlan("hand('alice', Card)"),
                delTestPlan("test('alice', Card)"),
                delTestPlan("wow(~hand('alice', Card))"),
                delTestPlan("test_funct(~know(know(hand('alice', Card))))"),
                delTestPlan("p(hand('alice', Card))"),
                delTestPlan("~poss(hand('alice', Card))"),
                delTestPlan("knows(~possible(know(hand('alice', Card))))"),

                // We should not subscribe to '+!' achieve plans
                addAchievePlan("know(hand('alice', Card))"),
                addAchievePlan("~know(hand('alice', Card))"),
                addAchievePlan("know(~hand('alice', Card))"),
                addAchievePlan("know(~know(know(hand('alice', Card))))"),
                addAchievePlan("possible(hand('alice', Card))"),
                addAchievePlan("~possible(hand('alice', Card))"),
                addAchievePlan("know(~possible(know(hand('alice', Card))))"),
                addAchievePlan("hand('alice', Card)"),
                addAchievePlan("test('alice', Card)"),
                addAchievePlan("wow(~hand('alice', Card))"),
                addAchievePlan("test_funct(~know(know(hand('alice', Card))))"),
                addAchievePlan("p(hand('alice', Card))"),
                addAchievePlan("~poss(hand('alice', Card))"),
                addAchievePlan("knows(~possible(know(hand('alice', Card))))"),

                // We should not subscribe to '-!' achieve plans
                delAchievePlan("know(hand('alice', Card))"),
                delAchievePlan("~know(hand('alice', Card))"),
                delAchievePlan("know(~hand('alice', Card))"),
                delAchievePlan("know(~know(know(hand('alice', Card))))"),
                delAchievePlan("possible(hand('alice', Card))"),
                delAchievePlan("~possible(hand('alice', Card))"),
                delAchievePlan("know(~possible(know(hand('alice', Card))))"),
                delAchievePlan("hand('alice', Card)"),
                delAchievePlan("test('alice', Card)"),
                delAchievePlan("wow(~hand('alice', Card))"),
                delAchievePlan("test_funct(~know(know(hand('alice', Card))))"),
                delAchievePlan("p(hand('alice', Card))"),
                delAchievePlan("~poss(hand('alice', Card))"),
                delAchievePlan("knows(~possible(know(hand('alice', Card))))")

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

    private static Plan addPlan(Trigger.TEOperator op, Trigger.TEType type, String triggerHead) {
        try {
            Trigger te = new Trigger(op, type, ASSyntax.parseLiteral(triggerHead));
            return new Plan(null, te, null, null);
        } catch (ParseException e) {
            throw new AssertionError("failed to create belief plan for " + triggerHead + ". Message: " + e.getMessage());
        }
    }
}