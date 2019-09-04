package utils.visuals;

import eis.percepts.AgentMap;
import eis.percepts.MapPercept;
import eis.percepts.terrain.Obstacle;
import utils.Position;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GridVisualizer extends JFrame {

    private static final int ROWS = 40;
    private static final int COLS = 40;

    public static void main(String[] args)
    {
        new GridVisualizer(null);
    }

    public CustomPanel[][] map;

    public GridVisualizer(AgentMap agentMap)
    {
        setTitle(agentMap.getAgent());
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
        setMinimumSize(new Dimension(500, 500));
        setVisible(true);

    }

    public void updateGridLocation(AgentMap curAgent, Position p, MapPercept percept)
    {
        Position curPosTrans = curAgent.getCurrentAgentPosition().add(new Position(20, 20));


        Position translated = percept.getLocation().add(new Position(20, 20));
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
