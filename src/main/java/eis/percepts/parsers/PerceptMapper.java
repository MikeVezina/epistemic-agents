package eis.percepts.parsers;

import eis.iilang.Percept;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PerceptMapper<R> {

    protected PerceptMapper() {
    }

    protected abstract boolean shouldHandlePercept(Percept p);

    /**
     * This method should map the percept to an Object. Do not modify the percept parameter.
     *
     * @param p The percept to map
     * @return
     */
    protected abstract R mapPercept(Percept p);

    public List<R> mapAllPercepts(List<Percept> rawPercepts) {
        if (rawPercepts == null || rawPercepts.isEmpty())
            return new ArrayList<>();

        List<R> mappedPercepts = rawPercepts.parallelStream()
                .filter(this::shouldHandlePercept)
                .map(p -> {
                    R mappedItem = mapPercept(p);

                    if (mappedItem == null)
                        throw new NullPointerException("Mapped item should not be null. Raw Percept: " + p);

                    return mappedItem;
                }).collect(Collectors.toList());

        if (mappedPercepts.size() != rawPercepts.size()) {
            throw new RuntimeException("There was an issue mapping percepts.");
        }

        return mappedPercepts;
    }
}

