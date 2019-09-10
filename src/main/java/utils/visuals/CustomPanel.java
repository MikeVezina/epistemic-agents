package utils.visuals;

import eis.percepts.agent.AgentMap;
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
    private AgentMap agentMap;
    private MapPercept lastPercept;
    public static final int HEIGHT = 10;
    public static final int WIDTH = 10;

    public CustomPanel(AgentMap agentMap)
    {
        this.agentMap = agentMap;
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(new CustomActionHandler());
    }

    private Color updateBackground()
    {
        if(lastPercept == null)
        {
            return Color.GRAY;
        }



        if(lastPercept.getLocation().equals(agentMap.getCurrentAgentPosition()))
            return Color.YELLOW;

//        if(lastPercept.isExpired(agentMap.getLastUpdateStep()))
//            return Color.GRAY;


        if(lastPercept.hasTeamEntity(agentMap.getSelfEntity()) && lastPercept.hasEnemyEntity(agentMap.getSelfEntity()))
            return Color.MAGENTA;
        else if (lastPercept.hasTeamEntity(agentMap.getSelfEntity()))
            return Color.ORANGE;
        else if(lastPercept.hasEnemyEntity(agentMap.getSelfEntity()))
             return Color.RED;

        if(lastPercept.hasBlock())
            return Color.GREEN;


        if(lastPercept.hasDispenser())
            return Color.BLUE;
        if(lastPercept.getTerrain() instanceof Obstacle)
            return Color.BLACK;
        if(lastPercept.getTerrain() instanceof ForbiddenCell)
            return Color.CYAN;
        if(lastPercept.getTerrain() instanceof Goal)
            return Color.PINK;
        if(lastPercept.getAgentSource().equals(agentMap.getAgentName()) && lastPercept.getLastStepPerceived() == agentMap.getAgentContainer().getCurrentStep())
            return Color.WHITE;
        if(lastPercept.getTerrain() instanceof FreeSpace)
            return Color.LIGHT_GRAY;




        return Color.LIGHT_GRAY;
    }


    public void updatePercept(MapPercept percept)
    {
        lastPercept = percept;
    }



    @Override
    public void paint(Graphics g) {
        setBackground(updateBackground());

        if(updateBorder() != null)
            setBorder(BorderFactory.createLineBorder(updateBorder()));
        super.paint(g);
    }

    private Color updateBorder() {
        if(lastPercept != null && lastPercept.getLocation().equals(Position.ZERO))
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
                System.out.println(CustomPanel.this.lastPercept);
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
