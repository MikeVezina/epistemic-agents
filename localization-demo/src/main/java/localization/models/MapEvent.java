package localization.models;

import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.util.EventObject;
import java.util.List;

public class MapEvent extends EventObject {

    private final LocalizationMapModel source;
    private final Location newLocation;
    private final Atom moveDirection;

    public MapEvent(LocalizationMapModel source, Location newLocation, Atom direction) {
        super(source);
        this.source = source;
        this.newLocation = newLocation;
        this.moveDirection = direction;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public Atom getMoveDirection() {
        return moveDirection;
    }

    public List<Literal> getPerceptions()
    {
        return source.getPercepts(newLocation);
    }

    @Override
    public LocalizationMapModel getSource() {
        return source;
    }
}
