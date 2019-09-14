package eis.percepts.containers;

import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.percepts.Task;
import eis.percepts.parsers.PerceptHandlerFactory;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.PerceptUtils;

import java.util.*;

public abstract class PerceptContainer {
    private Map<String, List<Percept>> filteredPerceptMap;

    protected PerceptContainer(Map<String, List<Percept>> filteredPerceptMap) {
        setFilteredPerceptMap(filteredPerceptMap);
    }

    protected void setFilteredPerceptMap(Map<String, List<Percept>> filteredPerceptMap) {
        Set<String> requiredPerceptNames = getRequiredPerceptNames();

        // Validate perceptions, check that required percepts exist in the map.
        if (requiredPerceptNames != null && !getRequiredPerceptNames().stream().allMatch(filteredPerceptMap::containsKey))
            throw new InvalidPerceptCollectionException("Invalid raw percepts, or percepts mapped incorrectly. Filtered Percepts: " + filteredPerceptMap);

        this.filteredPerceptMap = filteredPerceptMap;
    }

    protected Map<String, List<Percept>> getFilteredPerceptMap() {
        return filteredPerceptMap;
    }

    protected abstract Set<String> getRequiredPerceptNames();

    protected static Map<String, List<Percept>> filterValidPercepts(List<Percept> rawPercepts, Set<String> validPerceptNames) {
        Map<String, List<Percept>> filteredPerceptMap = new HashMap<>();

        // Check to see if the raw percepts contain a step percept
        if (rawPercepts.stream().noneMatch(rawPercept -> rawPercept.getName().equals(SharedPerceptContainer.STEP_PERCEPT_NAME)))
            throw new InvalidPerceptCollectionException("No Step perception was parsed. Evaluate further to check if this is an issue with parsing percepts (or it might just be the initial simulation message)", true);

        rawPercepts.stream().filter(p -> validPerceptNames.contains(p.getName())).forEach(percept -> {
            // Get existing percept list (keyed by percept name)
            List<Percept> matchingPercepts = filteredPerceptMap.getOrDefault(percept.getName(), new ArrayList<>());
            matchingPercepts.add(percept);
            filteredPerceptMap.put(percept.getName(), matchingPercepts);
        });

        return filteredPerceptMap;
    }

    protected static Percept getSinglePercept(List<Percept> percepts) {
        if (percepts == null || percepts.size() != 1)
            throw new RuntimeException("Percepts are null or do not have a size of one. Percepts: " + percepts);

        return percepts.get(0);
    }

    protected static Number parseSingleNumberPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetNumberParameter(percept, 0);
    }

    protected static String parseSingleStringPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetStringParameter(percept, 0);
    }

    protected static boolean parseSingleBooleanPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return PerceptUtils.GetBooleanParameter(percept, 0);
    }

    protected static ParameterList parseSingleParameterListPercept(List<Percept> percepts) {
        Percept percept = getSinglePercept(percepts);
        return (ParameterList) PerceptUtils.GetParameter(percept, 0);
    }
}
