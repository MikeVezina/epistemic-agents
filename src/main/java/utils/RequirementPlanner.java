package utils;

import eis.percepts.requirements.AttachableRequirement;
import eis.percepts.requirements.Requirement;

import java.util.*;

import static map.Direction.*;


public final class RequirementPlanner {

    public static List<Requirement> SortRequirements(List<Requirement> originalRequirementList) {

        if (originalRequirementList == null)
            throw new NullPointerException("Requirements List can not be null.");

        if (originalRequirementList.isEmpty())
            throw new NullPointerException("Requirements List can not be empty.");

        // If there is only one requirement, there is no need to sort them. Add to list and return.
        if (originalRequirementList.size() == 1)
            return originalRequirementList;

        // Copy the requirements list for processing
        List<Requirement> requirementList = new ArrayList<>(originalRequirementList);

        // Sort by distance. This greatly reduces the amount of iterations needed to create the sequence of directions.
        requirementList.sort(RequirementPlanner::compare);

        // Create a list iterator
        ListIterator<Requirement> requirementIterator = requirementList.listIterator();

        // Grab and remove the first requirement.
        Requirement firstReq = requirementIterator.next();
        requirementIterator.remove();

        if(!firstReq.getPosition().equals(SOUTH))
            throw new NullPointerException("The first requirement should always be south.");

        // Create head node for requirements.
        AttachableRequirement headRequirement = new AttachableRequirement(firstReq);
        AttachableRequirement currentRequirement = headRequirement;


        // Sorting does not necessarily give us the correct sequence of requirements,
        // so we have to process the list manually, removing each processed requirement from the requirement list
        while (headRequirement.size() < originalRequirementList.size()) {

            // Obtain a new iterator so that we can re-iterate the list.
            requirementIterator = requirementList.listIterator();

            while (requirementIterator.hasNext()) {
                Requirement req = requirementIterator.next();

                if (currentRequirement.getAttachableDirection(req).equals(NONE))
                    continue;

                if (!currentRequirement.attachRequirement(req))
                    throw new RuntimeException("Failed to attach requirement: " + req);
            }

            currentRequirement = currentRequirement.getNextRequirement();
        }


        return headRequirement.toRequirementList();
    }

    public static int compare(Requirement o1, Requirement o2) {
        return Double.compare(o1.getPosition().getDistance(), o2.getPosition().getDistance());
    }
}
