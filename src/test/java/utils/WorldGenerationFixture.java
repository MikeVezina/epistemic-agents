package utils;

import wrappers.LiteralKey;
import epi.ManagedWorlds;
import epi.World;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static utils.TestUtils.createHandEntry;

public class WorldGenerationFixture {
    private Map<LiteralKey, LinkedList<Literal>> allProps;
    private ManagedWorlds managedWorlds;

    public WorldGenerationFixture() {
        this(new HashMap<>(), new ManagedWorlds());
    }

    public WorldGenerationFixture(Map<LiteralKey, LinkedList<Literal>> allPropsMap, ManagedWorlds worldsResult) {
        this.allProps = allPropsMap;
        this.managedWorlds = worldsResult;
    }

    public void addWorld(World w)
    {
        this.managedWorlds.add(w);
    }

    public void addWorld(String alice, String bob, String charlie)
    {
        World world = new World();
        var aliceEntry = createHandEntry("Alice", alice);
        var bobEntry = createHandEntry("Bob", bob);
        var charlieEntry = createHandEntry("Charlie", charlie);

        world.putAll(aliceEntry);
        world.putAll(bobEntry);
        world.putAll(charlieEntry);

        addWorld(world);
    }

    public void addProp(LiteralKey literalKey, Literal literal) {
        allProps.compute(literalKey, (key, val) -> {
            if (val == null)
                val = new LinkedList<>();

            val.add(literal);
            return val;
        });
    }

    public void addProp(Literal literalKey, Literal literal) {
        addProp(new LiteralKey(literalKey), literal);
    }

    public void addAllProps(Map<LiteralKey, LinkedList<Literal>> map) {
        this.allProps.putAll(map);
    }

    public void addProp(String literalKey, Literal literal) {
        try {
            LiteralKey litKey = new LiteralKey(ASSyntax.parseLiteral(literalKey));
            addProp(litKey, literal);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addProp(String literalKey, String literalVal) {
        try {
            LiteralKey litKey = new LiteralKey(ASSyntax.parseLiteral(literalKey));
            addProp(litKey, ASSyntax.parseLiteral(literalVal));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<LiteralKey, LinkedList<Literal>> getAllProps() {
        return allProps;
    }

    public ManagedWorlds getManagedWorlds() {
        return managedWorlds;
    }
}
