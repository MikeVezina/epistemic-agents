package epi;

import jason.JasonException;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;

import java.util.HashMap;
import java.util.Map;

public class World extends HashMap<Literal, Literal> {

    public World()
    {

    }

    protected World(World world)
    {
        super(world);
    }

    public boolean containsPredicate(Literal literal)
    {
        return this.containsPredicate(literal.getPredicateIndicator());
    }

    public boolean containsPredicate(PredicateIndicator predicateIndicator)
    {
        return this.containsKey(predicateIndicator);
    }

    public void putLiteral(Literal literal, Literal unifiedLiteral)
    {
        if(!unifiedLiteral.isGround())
            throw new RuntimeException("Unified literal is not ground");
        this.put(literal, unifiedLiteral);
    }

    public World clone()
    {
        return new World(this);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
