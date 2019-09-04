package utils;

import eis.percepts.MapPercept;
import eis.percepts.terrain.Obstacle;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GridVisualizer extends JFrame {

    private static final int ROWS = 40;
    private static final int COLS = 40;

    public static void main(String[] args)
    {
        new GridVisualizer("");
    }

    private class Tile {

    }

    public JPanel[][] map;

    public GridVisualizer(String name)
    {
        setTitle(name);
        this.setLayout(new GridLayout(ROWS, COLS));
        map = new JPanel[ROWS][COLS];
        Random r = new Random();
        for(int i = 0; i < map.length; i++)
        {
            for (int j = 0; j < map[i].length; j++) {
                JPanel newPanel = new JPanel();
                newPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                newPanel.setBackground(Color.GRAY);
                newPanel.setPreferredSize(new Dimension(10, 10));

                map[i][j] = newPanel;
                add(newPanel);
            }
        }
        setMinimumSize(new Dimension(500, 500));
        setVisible(true);

    }

    public void updateGridLocation(Position curAgent, Position p, MapPercept percept)
    {
        Position curPosTrans = curAgent.add(new Position(20, 20));

        changePanelColor(curPosTrans, Color.YELLOW);
        Position translated = p.add(new Position(20, 20));

        if(percept.getThing() != null && percept.getThing().getType().equals("entity"))
            changePanelColor(translated, Color.ORANGE);
        else if(percept.getThing() != null && percept.getThing().getType().equals("dispenser"))
            changePanelColor(translated, Color.BLUE);
        else if(percept.getTerrain() != null && percept.getTerrain() instanceof Obstacle)
            changePanelColor(translated, Color.BLACK);
        else
            changePanelColor(translated, Color.LIGHT_GRAY);
    }

    private void changePanelColor(Position p, Color c)
    {
        if(p.getX() >= map.length || p.getY() >= map.length || p.getX() < 0 || p.getY() < 0)
            return;
        JPanel val = map[p.getY()][p.getX()];
        val.setBackground(c);
    }
}
