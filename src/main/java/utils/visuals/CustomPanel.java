package utils.visuals;

import eis.percepts.AgentMap;
import eis.percepts.MapPercept;
import eis.percepts.terrain.Obstacle;
import utils.Position;

import javax.swing.*;
import java.awt.*;

public class CustomPanel extends JPanel  {
    private AgentMap agentMap;
    private MapPercept lastPercept;

    public CustomPanel(AgentMap agentMap)
    {
        this.agentMap = agentMap;
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setBackground(Color.GRAY);
        setPreferredSize(new Dimension(10, 10));
    }

    private Color updateBackground()
    {
        if(lastPercept == null)
        {
            return Color.GRAY;
        }

        if(lastPercept.getLocation().equals(agentMap.getCurrentAgentPosition()))
            return Color.YELLOW;

        if(lastPercept.getThing() != null && lastPercept.getThing().getType().equals("entity"))
            return Color.ORANGE;
        else if(lastPercept.getThing() != null && lastPercept.getThing().getType().equals("dispenser"))
            return Color.BLUE;
        else if(lastPercept.getTerrain() != null && lastPercept.getTerrain() instanceof Obstacle)
            return Color.BLACK;
        else if(lastPercept.getAgentSource().equals(agentMap.getAgent()) && lastPercept.getLastStepPerceived() == agentMap.getLastUpdateStep())
            return Color.WHITE;
        else
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
}
