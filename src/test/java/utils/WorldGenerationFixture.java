package utils;

import epistemic.agent.EpistemicAgent;
import epistemic.wrappers.WrappedLiteral;
import epistemic.ManagedWorlds;
import epistemic.World;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static utils.TestUtils.createHandEntry;

public class WorldGenerationFixture {
    private Map<WrappedLiteral, LinkedList<Literal>> allProps;
    private ManagedWorlds managedWorlds;

    public WorldGenerationFixture() {
        this(new HashMap<>(), new ManagedWorlds(new EpistemicAgent()));
    }

    public WorldGenerationFixture(Map<WrappedLiteral, LinkedList<Literal>> allPropsMap, ManagedWorlds worldsResult) {
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

        world.addAll(aliceEntry.values());
        world.addAll(bobEntry.values());
        world.addAll(charlieEntry.values());

        addWorld(world);
    }

    public void addProp(WrappedLiteral wrappedLiteral, Literal literal) {
        allProps.compute(wrappedLiteral, (key, val) -> {
            if (val == null)
                val = new LinkedList<>();

            val.add(literal);
            return val;
        });
    }

    public void addProp(Literal literalKey, Literal literal) {
        addProp(new WrappedLiteral(literalKey), literal);
    }

    public void addAllProps(Map<WrappedLiteral, LinkedList<Literal>> map) {
        this.allProps.putAll(map);
    }

    public void addProp(String literalKey, Literal literal) {
        try {
            WrappedLiteral litKey = new WrappedLiteral(ASSyntax.parseLiteral(literalKey));
            addProp(litKey, literal);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addProp(String literalKey, String literalVal) {
        try {
            WrappedLiteral litKey = new WrappedLiteral(ASSyntax.parseLiteral(literalKey));
            addProp(litKey, ASSyntax.parseLiteral(literalVal));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<WrappedLiteral, LinkedList<Literal>> getAllProps() {
        return allProps;
    }

    public ManagedWorlds getManagedWorlds() {
        return managedWorlds;
    }
}
