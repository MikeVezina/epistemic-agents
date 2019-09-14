package utils.visuals;

import eis.percepts.MapPercept;
import eis.percepts.terrain.ForbiddenCell;
import eis.percepts.terrain.FreeSpace;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import utils.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

public class CustomPanel extends JPanel {
    private Logger logger = Logger.getLogger("CustomPanel");
    private MapPercept currentPercept;
    public static final int HEIGHT = 10;
    public static final int WIDTH = 10;
    private GridVisualizer gridVisualizer;

    public CustomPanel(GridVisualizer gridVisualizer)
    {
        this.gridVisualizer = gridVisualizer;
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(new CustomActionHandler());
    }

    private Color updateBackground()
    {
        if(currentPercept == null)
        {
            return Color.GRAY;
        }

        if(gridVisualizer.isAgentCell(currentPercept))
            return Color.YELLOW;

//        if(lastPercept.isExpired(agentMap.getLastUpdateStep()))
//            return Color.GRAY;


        if(currentPercept.hasTeamEntity() && currentPercept.hasEnemyEntity())
            return Color.MAGENTA;
        else if (currentPercept.hasTeamEntity())
            return Color.ORANGE;
        else if(currentPercept.hasEnemyEntity())
             return Color.RED;

        if(currentPercept.hasBlock())
            return Color.GREEN;


        if(currentPercept.hasDispenser())
            return Color.BLUE;
        if(currentPercept.getTerrain() instanceof Obstacle)
            return Color.BLACK;
        if(currentPercept.getTerrain() instanceof ForbiddenCell)
            return Color.CYAN;
        if(currentPercept.getTerrain() instanceof Goal)
            return Color.PINK;
        if(currentPercept.getTerrain() instanceof FreeSpace && gridVisualizer.getCurrentStep() == currentPercept.getLastStepPerceived())
            return Color.WHITE;
        if(currentPercept.getTerrain() instanceof FreeSpace)
            return Color.LIGHT_GRAY;

        return Color.LIGHT_GRAY;
    }

    @Override
    public void paint(Graphics g)
    {

    }


    public void updatePercept(MapPercept percept)
    {
        currentPercept = percept;
        setBackground(updateBackground());

        if(updateBorder() != null)
            setBorder(BorderFactory.createLineBorder(updateBorder()));
    }

    private Color updateBorder() {
        if(currentPercept != null && currentPercept.getLocation().equals(Position.ZERO))
            return Color.GREEN;
//        else
//            return Color.BLACK;
        return null;
    }

    class CustomActionHandler implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() % 2 == 0)
            {
                System.out.println(CustomPanel.this.currentPercept);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
