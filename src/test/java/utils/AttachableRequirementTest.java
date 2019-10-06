package utils;

import map.Direction;
import map.Position;
import org.junit.Before;
import org.junit.Test;
import eis.percepts.requirements.AttachableRequirement;

import static org.junit.Assert.*;

public class AttachableRequirementTest {

    private static final String BLOCK = "b0";
    private AttachableRequirement headRequirement;
    private AttachableRequirement priorityAttachment;
    private AttachableRequirement nonPriorityAttachment;


    @Before
    public void setUp() throws Exception {
        headRequirement = new AttachableRequirement(0, 1, BLOCK);
        priorityAttachment = new AttachableRequirement(-1, 1, BLOCK);
        nonPriorityAttachment = new AttachableRequirement(0, 2, BLOCK);
    }

    private void testAttachDirection(Direction result, Direction original) {
        Position nextBlock = headRequirement.getPosition().add(original.getPosition());
        assertEquals(result, headRequirement.getAttachableDirection(new AttachableRequirement(nextBlock, BLOCK)));
    }

    private void testAttachDirection(Direction dir) {
        testAttachDirection(dir, dir);
    }

    @Test
    public void testAttachableEastDirection() {
        testAttachDirection(Direction.EAST);
    }

    @Test
    public void testAttachableWestDirection() {
        testAttachDirection(Direction.WEST);
    }

    @Test
    public void testAttachableSouthDirection() {
        testAttachDirection(Direction.SOUTH);
    }
    @Test
    public void testAttachableNorthDirection() {
        testAttachDirection(Direction.NONE, Direction.NORTH);
    }

    @Test
    public void testAttachExistingNoOverwrite() {
        assertTrue(headRequirement.attachRequirement(priorityAttachment));
        assertFalse(headRequirement.attachRequirement(nonPriorityAttachment));

        // Check that the priority attachment is kept, and that the non-priority attachment is updated
        assertEquals(priorityAttachment, headRequirement.getNextRequirement());
        assertNull(nonPriorityAttachment.getPreviousRequirement());
        assertEquals(Direction.NONE, nonPriorityAttachment.getPreviousRequirementDirection());
    }

    @Test
    public void testAttachExistingOverwrite() {
        assertTrue(headRequirement.attachRequirement(nonPriorityAttachment));
        assertTrue(headRequirement.attachRequirement(priorityAttachment));

        // Check that the priority attachment overwrites the non-priority, and that the non-priority attachment is updated
        assertEquals(priorityAttachment, headRequirement.getNextRequirement());
        assertNull(nonPriorityAttachment.getPreviousRequirement());
        assertEquals(Direction.NONE, nonPriorityAttachment.getPreviousRequirementDirection());
    }

    @Test
    public void testSize() {
        assertEquals(1, headRequirement.size());

        headRequirement.attachRequirement(new AttachableRequirement(1, 1, BLOCK));
        assertEquals(2, headRequirement.size());

        headRequirement.getNextRequirement().attachRequirement(new AttachableRequirement(2, 1, BLOCK));
        assertEquals(3, headRequirement.size());
    }

    @Test
    public void testAttachNotValidDirection() {
        assertFalse(headRequirement.attachRequirement(new AttachableRequirement(100, 100, BLOCK)));
        assertFalse(headRequirement.attachRequirement(new AttachableRequirement(0, -1, BLOCK)));
        assertFalse(headRequirement.attachRequirement(new AttachableRequirement(2, 1, BLOCK)));
        assertFalse(headRequirement.attachRequirement(new AttachableRequirement(-2, 1, BLOCK)));

    }


}