package utils.visuals;

import eis.percepts.agent.AgentMap;
import eis.percepts.MapPercept;
import utils.Position;

import javax.swing.*;
import java.awt.*;

public class GridVisualizer extends JFrame {

    private static final int ROWS = 80;
    private static final int COLS = 80;

    public static void main(String[] args)
    {
        new GridVisualizer(null);
    }

    public CustomPanel[][] map;

    public GridVisualizer(AgentMap agentMap)
    {
        setTitle(agentMap.getAgentName());
        this.setLayout(new GridLayout(ROWS, COLS));
        map = new CustomPanel[ROWS][COLS];

        for(int i = 0; i < map.length; i++)
        {
            for (int j = 0; j < map[i].length; j++) {
                CustomPanel newPanel = new CustomPanel(agentMap);
                map[i][j] = newPanel;
                add(newPanel);
            }
        }
        setMinimumSize(new Dimension(COLS * CustomPanel.WIDTH, ROWS * CustomPanel.HEIGHT ));
        setVisible(true);

    }

    public void updateGridLocation(AgentMap curAgent, Position p, MapPercept percept)
    {
        Position translated = percept.getLocation().add(new Position(ROWS/2, COLS/2));

        if(translated.getY() > 0 && translated.getY() < 5)
        {

        }
        updatePanel(translated, percept);


    }

    private void updatePanel(Position panelPosition, MapPercept percept)
    {
        if(panelPosition.getX() >= map.length || panelPosition.getY() >= map.length || panelPosition.getX() < 0 || panelPosition.getY() < 0)
            return;

        CustomPanel panel = map[panelPosition.getY()][panelPosition.getX()];
        panel.updatePercept(percept);
    }
}
