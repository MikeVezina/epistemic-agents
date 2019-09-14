package eis.percepts.parsers;

import eis.iilang.Percept;

import java.util.ArrayList;
import java.util.List;

public abstract class PerceptMapper<R> {

    protected PerceptMapper() {
    }

    protected abstract boolean shouldHandlePercept(Percept p);

    protected abstract R mapPercept(Percept p);

    public List<R> mapAllPercepts(List<Percept> rawPercepts) {
        if (rawPercepts == null || rawPercepts.isEmpty())
            return new ArrayList<>();

        List<R> mappedPercepts = new ArrayList<>();
        rawPercepts.stream().filter(this::shouldHandlePercept).forEach(p -> {
            Percept cloned = (Percept) p.clone();
            R mappedItem = mapPercept(cloned);

            if (mappedItem == null)
                throw new NullPointerException("Mapped item should not be null. Raw Percept: " + p);

            mappedPercepts.add(mappedItem);
        });

        if(mappedPercepts.size() != rawPercepts.size())
        {
            System.err.println("There may be an issue mapping. " + getClass());
        }

        return mappedPercepts;
    }
}

