package eis.percepts;

import org.jgraph.graph.DefaultEdge;

public class CustomEdge extends DefaultEdge {

    @Override
    public String toString()
    {
        return super.getSource() + " -> " + super.getTarget();
    }
}
