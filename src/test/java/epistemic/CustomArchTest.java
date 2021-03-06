package epistemic;

import epistemic.agent.stub.StubAgArch;
import jason.bb.BeliefBase;
import jason.infra.centralised.CentralisedAgArch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.WorldGenerationFixture;

import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static utils.TestUtils.createHandEnumeration;


public class CustomArchTest {


    private CentralisedAgArch customArch;

    private BeliefBase beliefBase;

    private List<WorldGenerationFixture> worldGenerationFixtures;

    public CustomArchTest() {
        this.customArch = new StubAgArch(false);
    }


    @BeforeEach
    public void setUp() {
        createFixtures();
    }

    private void createFixtures() {
        worldGenerationFixtures = new ArrayList<>();

        var worldGenOne = new WorldGenerationFixture();
        worldGenOne.addAllProps(createHandEnumeration("Alice", "AA", "A8", "88"));
        worldGenOne.addAllProps(createHandEnumeration("Bob", "AA", "A8", "88"));
        worldGenOne.addAllProps(createHandEnumeration("Charlie", "AA", "A8", "88"));

        worldGenOne.addWorld("AA", "AA", "AA");
        worldGenOne.addWorld("AA", "AA", "A8");
        worldGenOne.addWorld("AA", "AA", "88");

        worldGenOne.addWorld("AA", "A8", "AA");
        worldGenOne.addWorld("AA", "A8", "A8");
        worldGenOne.addWorld("AA", "A8", "88");

        worldGenOne.addWorld("AA", "88", "AA");
        worldGenOne.addWorld("AA", "88", "A8");
        worldGenOne.addWorld("AA", "88", "88");

        worldGenOne.addWorld("A8", "AA", "AA");
        worldGenOne.addWorld("A8", "AA", "A8");
        worldGenOne.addWorld("A8", "AA", "88");

        worldGenOne.addWorld("A8", "A8", "AA");
        worldGenOne.addWorld("A8", "A8", "A8");
        worldGenOne.addWorld("A8", "A8", "88");

        worldGenOne.addWorld("A8", "88", "AA");
        worldGenOne.addWorld("A8", "88", "A8");
        worldGenOne.addWorld("A8", "88", "88");

        worldGenOne.addWorld("88", "AA", "AA");
        worldGenOne.addWorld("88", "AA", "A8");
        worldGenOne.addWorld("88", "AA", "88");

        worldGenOne.addWorld("88", "A8", "AA");
        worldGenOne.addWorld("88", "A8", "A8");
        worldGenOne.addWorld("88", "A8", "88");

        worldGenOne.addWorld("88", "88", "AA");
        worldGenOne.addWorld("88", "88", "A8");
        worldGenOne.addWorld("88", "88", "88");

        worldGenerationFixtures.add(worldGenOne);
    }


    @Test
    public void generateWorlds() {
        for(var fixture : worldGenerationFixtures)
        {
//            var managedWorld = customArch.generateWorlds(fixture.getAllProps());

//            assertManagedWorlds(fixture.getManagedWorlds(), managedWorld);
        }
    }

    private static void assertManagedWorlds(ManagedWorlds expected, ManagedWorlds actual) {
        if(expected == actual)
            return;

        assertNotNull(expected, "expected is null");
        assertNotNull(actual, "actual is null");
        assertEquals(expected.size(), actual.size(), "ManagedWorlds objects must be the same size");
        assertTrue(expected.containsAll(actual), "Both ManagedWorlds objects must contain the same elements");
    }
}

