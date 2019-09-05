package utils.visuals;

import eis.EISAdapter;
import eis.percepts.AgentMap;
import eis.percepts.MapPercept;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import eis.percepts.things.Dispenser;
import eis.percepts.things.Entity;
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


        if(lastPercept.getThing() != null && lastPercept.getThing() instanceof Entity) {
         Entity ent = (Entity) lastPercept.getThing();
         if(ent.isTeammate())
            return Color.ORANGE;
         else
             return Color.RED;
        }

        if(lastPercept.getThing() != null && lastPercept.getThing() instanceof Dispenser)
            return Color.BLUE;
        if(lastPercept.getTerrain() != null && lastPercept.getTerrain() instanceof Obstacle)
            return Color.BLACK;
        if(lastPercept.getTerrain() != null && lastPercept.getTerrain() instanceof Goal)
            return Color.PINK;
        if(lastPercept.getAgentSource().equals(agentMap.getAgent()) && lastPercept.getLastStepPerceived() == agentMap.getLastUpdateStep())
            return Color.WHITE;

        return Color.LIGHT_GRAY;
    }


    public void updatePercept(MapPercept percept)
    {
        lastPercept = percept;
    }

    @Override
    public void paint(Graphics g) {
        setBackground(updateBackground());
        super.paint(g);
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
