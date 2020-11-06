package localization;

import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;
import localization.models.MapEvent;

import java.util.List;

public interface MapEventListener {
    void agentMoved(MapEvent mapEvent);
}
