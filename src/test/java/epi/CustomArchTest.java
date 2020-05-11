package epi;

import jason.asSemantics.Agent;
import jason.asSemantics.Circumstance;
import jason.asSemantics.TransitionSystem;
import jason.bb.DefaultBeliefBase;
import jason.runtime.Settings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import utils.WorldGenerationFixture;

import java.util.*;


import static utils.TestUtils.createHandEnumeration;


public class CustomArchTest {


    private CustomArch customArch;
    private List<WorldGenerationFixture> worldGenerationFixtures;

    public CustomArchTest() {
        this.customArch = new CustomArch();
        createMockTS();
    }

    public void createMockTS()
    {
        var mockAgent = new Agent();
        var mockBB = new DefaultBeliefBase();

        mockAgent.setBB(mockBB);
        var ts = new TransitionSystem(mockAgent, new Circumstance(), new Settings(), customArch);

        mockAgent.setTS(ts);
        customArch.setTS(ts);
    }

    @Before
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
            var managedWorld = customArch.generateWorlds(fixture.getAllProps());

            assertManagedWorlds(fixture.getManagedWorlds(), managedWorld);
        }
    }

    private static void assertManagedWorlds(ManagedWorlds expected, ManagedWorlds actual) {
        if(expected == actual)
            return;

        Assert.assertNotNull("expected is null", expected);
        Assert.assertNotNull("actual is null", actual);
        Assert.assertEquals("ManagedWorlds objects must be the same size", expected.size(), actual.size());
        Assert.assertTrue("Both ManagedWorlds objects must contain the same elements", expected.containsAll(actual));
    }
}

