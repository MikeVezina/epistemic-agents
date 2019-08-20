package utils;

import utils.requirements.Requirement;

import java.util.*;

import static utils.Direction.SOUTH;
import static utils.Direction.EAST;
import static utils.Direction.WEST;


public final class RequirementPlanner {

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
                if (previousRequirement == null && req.getPosition().equals(SOUTH)) {
                    sortedRequirements.add(req);
                    requirementIterator.remove();
                }

                if (previousRequirement != null) {
                    // Calculate the transition vector between the two requirements
                    Position transition = req.getPosition().subtract(previousRequirement.getPosition());

                    if (transition.equals(EAST) || transition.equals(WEST) || transition.equals(SOUTH)) {
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


    public static void main(String[] args) {

    }
}
