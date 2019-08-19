package utils;

import java.util.*;

public final class RequirementPlanner {

    // Requirements can only be east, west, or south relative to the previous requirement.
    public static final Position EAST_POSITION = new Position(1, 0);
    public static final Position WEST_POSITION = new Position(-1, 0);
    public static final Position SOUTH_POSITION = new Position(0, 1);

    public static List<Requirement> SortRequirements(List<Requirement> originalRequirementList) {

        if (originalRequirementList == null)
            throw new NullPointerException("Requirements List can not be null.");

        if (originalRequirementList.isEmpty())
            throw new NullPointerException("Requirements List can not be empty.");

        // If there is only one requirement, there is no need to sort them.
        if (originalRequirementList.size() == 1)
            return originalRequirementList;

        // Copy the requirements list
        List<Requirement> requirementList = new ArrayList<>(originalRequirementList);

        // Create list for sorted requirements
        LinkedList<Requirement> sortedRequirements = new LinkedList<>();

        // Sort by distance. This greatly reduces the amount of iterations needed to create the sequence of directions.
        requirementList.sort(RequirementPlanner::compare);


        // Sorting does not necessarily give us the correct sequence of requirements,
        // so we have to process the list manually, removing each processed requirement from the requirement list
        while (requirementList.size() != 0) {
            ListIterator<Requirement> requirementIterator = requirementList.listIterator();

            while (requirementIterator.hasNext()) {
                Requirement req = requirementIterator.next();
                Requirement previousRequirement = sortedRequirements.peekLast();

                // If the current requirement position is south (and we don't have any previous requirements), then it is the first requirement
                if (previousRequirement == null && req.getPosition().equals(SOUTH_POSITION)) {
                    sortedRequirements.add(req);
                    requirementIterator.remove();
                }

                if (previousRequirement != null) {
                    // Calculate the transition vector between the two requirements
                    Position transition = req.getPosition().subtract(previousRequirement.getPosition());

                    if (transition.equals(EAST_POSITION) || transition.equals(WEST_POSITION) || transition.equals(SOUTH_POSITION)) {
                        sortedRequirements.add(req);
                        requirementIterator.remove();
                    }
                }
            }
        }
        return sortedRequirements;
    }

    public static int compare(Requirement o1, Requirement o2) {
        return Double.compare(o1.getPosition().getDistance(), o2.getPosition().getDistance());
    }

}
