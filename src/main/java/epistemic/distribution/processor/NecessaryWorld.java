package epistemic.distribution.processor;

import epistemic.World;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NecessaryWorld extends WorldProcessorChain {

    public NecessaryWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals) {
        super(keyLiteral, worldLiterals);
    }

    public NecessaryWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals, List<Literal> filterLiterals) {
        super(keyLiteral, worldLiterals, filterLiterals);
    }

    @Override
    protected Set<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        Set<World> transformedWorlds = new HashSet<>();

        World transformed = world.clone();

        // Need to handle multiple values per world...
        for (Literal lit : literalValues)
            transformed.putLiteral(new WrappedLiteral(lit), lit);

        transformedWorlds.add(transformed);

        return transformedWorlds;
    }
}
