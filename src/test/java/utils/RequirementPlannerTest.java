package utils;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RequirementPlannerTest {

    private static final String BLOCK = "b0";
    Map<List<Requirement>, List<Requirement>> validTestFixture = new HashMap<>();


    @Before
    public void setUp() throws Exception {
        // One Requirement
        addToTestFixture(
                CreateRequirementList(CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1))
        );

        // Two Requirements, second block below other block
        addToTestFixture(
                CreateRequirementList(CreateRequirement(0, 2), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(0, 2))
        );

        // Two Requirements, second block right of other block
        addToTestFixture(
                CreateRequirementList(CreateRequirement(1, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1))
        );

        // Two Requirements, second block left of other block
        addToTestFixture(
                CreateRequirementList(CreateRequirement(-1, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(-1, 1))
        );

        // Three Requirements
        addToTestFixture(
                CreateRequirementList(CreateRequirement(0, 3), CreateRequirement(0, 2), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(0, 2), CreateRequirement(0, 3))
        );

        // Three Requirements, second variation
        addToTestFixture(
                CreateRequirementList(CreateRequirement(1, 1), CreateRequirement(1, 2), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(1, 2))
        );

        // Three Requirements, third variation
        addToTestFixture(
                CreateRequirementList(CreateRequirement(1, 1), CreateRequirement(2, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(2, 1))
        );

        // Four Requirements, first variation
        // - - - -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(1, 1), CreateRequirement(3, 1), CreateRequirement(2, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(2, 1), CreateRequirement(3, 1))
        );

        // Four Requirements, second variation
        // - -
        // - -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(0, 2), CreateRequirement(1, 1), CreateRequirement(0, 1), CreateRequirement(1, 2)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(1, 2), CreateRequirement(0, 2))
        );

        // Four Requirements, third variation
        //  - - -
        //      -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(2, 1), CreateRequirement(2, 2), CreateRequirement(1, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(2, 1), CreateRequirement(2, 2))
        );

        // Four Requirements, fourth variation
        //  - -
        //    -
        //    -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(1, 3), CreateRequirement(1, 2)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(1, 2), CreateRequirement(1, 3))
        );

        // Four Requirements, fifth variation
        //  -
        //  -
        //  -
        //  -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(2, 1), CreateRequirement(2, 2), CreateRequirement(1, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(1, 1), CreateRequirement(2, 1), CreateRequirement(2, 2))
        );

        // Four Requirements, sixth variation
        //  - - -
        //  -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(-2, 1), CreateRequirement(-2, 2), CreateRequirement(-1, 1), CreateRequirement(0, 1)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(-1, 1), CreateRequirement(-2, 1), CreateRequirement(-2, 2))
        );

        // Four Requirements, fifth variation
        //  - - -
        //  -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(-1, 1), CreateRequirement(0, 1), CreateRequirement(-2, 1), CreateRequirement(-2, 2)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(-1, 1), CreateRequirement(-2, 1), CreateRequirement(-2, 2))
        );

        // Five Requirements, first variation (this is where our algorithm might fail since it is possible to get stuck.)
        //  - - -
        //  - -
        addToTestFixture(
                CreateRequirementList(CreateRequirement(-1, 1), CreateRequirement(0, 1), CreateRequirement(-2, 1), CreateRequirement(-2, 2)),
                CreateRequirementList(CreateRequirement(0, 1), CreateRequirement(-1, 1), CreateRequirement(-2, 1), CreateRequirement(-2, 2))
        );

    }

    private void addToTestFixture(List<Requirement> unsorted, List<Requirement> sorted) {
        validTestFixture.put(unsorted, sorted);
    }

    private static Requirement CreateRequirement(int x, int y) {
        return new Requirement(x, y, BLOCK);
    }

    private static List<Requirement> CreateRequirementList(Requirement... requirements) {
        return Arrays.asList(requirements);
    }

    @Test
    public void getSortedRequirementsList() {

        for (Map.Entry<List<Requirement>, List<Requirement>> entry : validTestFixture.entrySet()) {
            List<Requirement> unsortedRequirements = entry.getKey();
            List<Requirement> sortedRequirements = entry.getValue();

            List<Requirement> actualRequirements = RequirementPlanner.SortRequirements(unsortedRequirements);

            try {
                assertEquals(sortedRequirements, actualRequirements);
            } catch (AssertionError e) {
                System.out.println("Assertion Failed. Requirement Size: " + sortedRequirements.size());
                throw e;
            }
        }


        System.out.println("Number of test cases for 'getSortedRequirementsList()': " + validTestFixture.size());
    }
}