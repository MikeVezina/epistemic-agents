package eis.agent;

import jason.asSyntax.Atom;
import map.Position;

import java.util.function.Function;

public enum Rotation {
    CLOCKWISE(new Atom("cw"), p -> new Position(-p.getY(), p.getX())),
    COUNTER_CLOCKWISE(new Atom("ccw"), p -> new Position(p.getY(), -p.getX()));

    private Function<Position, Position> transform;
    private Atom rotationAtom;

    Rotation(Atom rotationAtom, Function<Position, Position> transform)
    {
        this.rotationAtom = rotationAtom;
        this.transform = transform;
    }

    public Atom getAtom()
    {
        return rotationAtom;
    }

    public String getFunctor()
    {
        return rotationAtom.getFunctor();
    }

    public Position rotate(Position position)
    {
        if(position == null)
            return null;

        if(position.equals(Position.ZERO))
            return position.clone();

        return transform.apply(position);
    }

    public static Rotation getRotation(Atom atom)
    {
        return getRotation(atom.getFunctor());
    }

    public static Rotation getRotation(String functor)
    {
        for(Rotation r : Rotation.values())
        {
           if(r.getFunctor().equals(functor))
               return r;
        }
        return null;
    }
}
