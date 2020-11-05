package epistemic.distribution.processor;

import epistemic.World;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Literal;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// Turns possibly rules into worlds
public class PossiblyWorld extends WorldProcessorChain {

    public PossiblyWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals) {
        super(keyLiteral, worldLiterals);
    }

    public PossiblyWorld(WrappedLiteral keyLiteral, List<Literal> worldLiterals, List<Literal> filterLiterals) {
        super(keyLiteral, worldLiterals, filterLiterals);
    }

    @Override
    protected List<World> transformWorld(@NotNull World world, WrappedLiteral literalKey, List<Literal> literalValues) {
        List<World> transformedWorlds = new ArrayList<>();

        for (Literal lit : literalValues) {
            World transformed = world.clone();
            transformed.putLiteral(new WrappedLiteral(lit), lit);
            transformedWorlds.add(transformed);
        }

        return transformedWorlds;
    }
}
