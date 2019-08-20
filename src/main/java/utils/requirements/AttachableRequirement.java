package utils.requirements;

import utils.Direction;
import utils.Position;

import java.util.ArrayList;

public class AttachableRequirement extends Requirement {

    private AttachableRequirement previousRequirement;
    private AttachableRequirement nextRequirement;

    private Direction nextRequirementDirection = Direction.NONE;
    private Direction previousRequirementDirection = Direction.NONE;


    public AttachableRequirement(Position position, String blockType) {
        super(position, blockType);
    }

    public AttachableRequirement(int x, int y, String blockType) {
        super(x, y, blockType);
    }

    public AttachableRequirement(Requirement requirement) {
        super(requirement.getPosition(), requirement.getBlockType());
    }

    public boolean attachRequirement(Requirement requirement) {
        return attachRequirement(new AttachableRequirement(requirement));
    }

    public boolean attachRequirement(AttachableRequirement requirement) {
        if(requirement == null)
            return false;

        if(nextRequirement != null)
        {
            System.out.println("A Requirement has already been attached.");
            return false;
        }

        Direction transition = getTransition(requirement);

        // If there is no valid transition or if the next requirement is already connected.
        if(transition.equals(Direction.NONE) || !requirement.setPreviousRequirement(this))
            return false;

        this.nextRequirement = requirement;
        this.nextRequirementDirection = transition;
        return true;
    }

    public boolean isHeadRequirement()
    {
        return previousRequirement == null;
    }

    public int size()
    {
        if(isTailRequirement())
            return 1;

        return 1 + nextRequirement.size();
    }

    public boolean isTailRequirement()
    {
        return previousRequirement != null && nextRequirement == null;
    }

    private boolean setPreviousRequirement(AttachableRequirement requirement) {
        if(requirement == null)
            return false;

        if(nextRequirement != null)
        {
            System.out.println("A Requirement has already been attached.");
            return false;
        }

        this.previousRequirement = requirement;
        this.previousRequirementDirection = getTransition(requirement);
        return true;
    }

    private Direction getTransition(AttachableRequirement requirement)
    {
        Position transition = requirement.getPosition().subtract(this.getPosition());
        return Direction.GetDirection(transition);
    }

    public AttachableRequirement getNextRequirement() {
        return nextRequirement;
    }

    public AttachableRequirement getPreviousRequirement() {
        return previousRequirement;
    }

    public Direction getNextRequirementDirection() {
        return nextRequirementDirection;
    }

    public Direction getPreviousRequirementDirection() {
        return previousRequirementDirection;
    }

    public static void main(String[] args) {

        AttachableRequirement headRequirement = new AttachableRequirement(0, 1, "b0");
        headRequirement.attachRequirement(new AttachableRequirement(0, 3, "b1"));
        printAttachedChain(headRequirement);
    }

    private static void printAttachedChain(AttachableRequirement head)
    {
        AttachableRequirement cur = head;
        while(cur != null)
        {
            System.out.println(cur);
            System.out.println(cur.getNextRequirementDirection());
            cur = cur.getNextRequirement();
        }
    }
}
