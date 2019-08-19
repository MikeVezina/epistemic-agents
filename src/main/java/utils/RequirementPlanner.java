package utils;

import java.util.*;

public class RequirementPlanner implements Comparator<Requirement> {

    // Requirements can only be east, west, or south relative to the previous requirement.
    private static final Position EAST_POSITION = new Position(1, 0);
    private static final Position WEST_POSITION = new Position(-1, 0);
    private static final Position SOUTH_POSITION = new Position(0, 1);

    private List<Requirement> requirementList;

    public RequirementPlanner(List<Requirement> requirementList) {
        if (requirementList == null)
            throw new NullPointerException("Requirements List can not be null.");

        if (requirementList.isEmpty())
            throw new NullPointerException("Requirements List can not be empty.");

        this.requirementList = requirementList;
    }

    public List<Requirement> getSortedRequirementsList() {

        // If there is only one requirement, there is no need to sort them.
        if (this.requirementList.size() == 1)
            return requirementList;

        this.requirementList.sort(this);

        LinkedList<Requirement> requirementsSequence = new LinkedList<>();

        // Sorting does not necessarily give us the correct sequence of requirements,
        // so we have to process the list manually.
        while(requirementsSequence.size() < requirementList.size()) {
            for(Requirement req : requirementList)
            {
                Requirement previousRequirement = requirementsSequence.peekLast();

                // If the current requirement position is south, then it is the first requirement
                if(previousRequirement == null && !req.getPosition().equals(SOUTH_POSITION))
                    continue;

                if(previousRequirement == null)
                {
                    requirementsSequence.add(req);
                }
                else {
                    Position transition = previousRequirement.getPosition().subtract(req.getPosition());
                    if(transition.equals(EAST_POSITION) || transition.equals(WEST_POSITION) || transition.equals(SOUTH_POSITION))
                    {

                    }
                }

            }

        }
        return requirementsSequence;
    }

    @Override
    public int compare(Requirement o1, Requirement o2) {
        return Double.compare(o1.getPosition().getDistance(), o2.getPosition().getDistance());
    }

    private static void testReqs() {
        List<Requirement> requirements = new ArrayList<>();
        requirements.add(new Requirement(new Position(0, 1), "b0"));
        requirements.add(new Requirement(new Position(0, 2), "b0"));
        requirements.add(new Requirement(new Position(-1, 2), "b0"));
        requirements.add(new Requirement(new Position(-1, 3), "b0"));
        requirements.add(new Requirement(new Position(0, 3), "b0"));
        requirements.add(new Requirement(new Position(1, 3), "b0"));


        RequirementPlanner r = new RequirementPlanner(requirements);
        r.getSortedRequirementsList();
        System.out.println(requirements);
    }

    public static void main(String[] args) {
        testReqs();
    }
}
