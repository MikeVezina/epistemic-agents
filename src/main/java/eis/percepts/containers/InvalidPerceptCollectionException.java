package eis.percepts.containers;

import eis.iilang.Percept;

import java.util.Map;
import java.util.Set;

public class InvalidPerceptCollectionException extends RuntimeException {

    private boolean isStartPercepts;

    public InvalidPerceptCollectionException(String description, boolean isStartPercepts) {
        super(description);
        this.isStartPercepts = isStartPercepts;
    }

    public InvalidPerceptCollectionException(String description) {
        this(description, false);
    }

    public boolean isStartPercepts()
    {
        return isStartPercepts;
    }
}
