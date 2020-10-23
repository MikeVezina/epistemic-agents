package localization;

import jason.asSyntax.Literal;

import javax.xml.stream.Location;
import java.util.List;

public interface MapEventListener {
    void agentMoved(List<Literal> newPercepts);
}
