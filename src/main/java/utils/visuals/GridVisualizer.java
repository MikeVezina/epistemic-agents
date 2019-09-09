package utils.visuals;

import eis.EISAdapter;
import eis.percepts.agent.AgentMap;
import eis.percepts.MapPercept;
import jason.asSyntax.Atom;
import jason.asSyntax.Structure;
import utils.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GridVisualizer extends JFrame{

    private static final int ROWS = 80;
    private static final int COLS = 80;

    public static void main(String[] args)
    {
        new GridVisualizer(null);
    }

    public CustomPanel[][] map;
    private AgentMap agentMap;

    public GridVisualizer(AgentMap agentMap)
    {
        this.agentMap = agentMap;
//        this.addKeyListener(this);
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

//    @Override
//    public void keyTyped(KeyEvent e) {
//
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        Structure eventStructure = null;
//        if(e.getKeyCode() == KeyEvent.VK_W)
//        {
//            eventStructure = new Structure("move");
//            eventStructure.addTerm(new Atom("n"));
//
//        }
//
//        if(e.getKeyCode() == KeyEvent.VK_A)
//        {
//            eventStructure = new Structure("move");
//            eventStructure.addTerm(new Atom("w"));
//
//        }
//
//        if(e.getKeyCode() == KeyEvent.VK_S)
//        {
//            eventStructure = new Structure("move");
//            eventStructure.addTerm(new Atom("s"));
//
//        }
//
//        if(e.getKeyCode() == KeyEvent.VK_D)
//        {
//            eventStructure = new Structure("move");
//            eventStructure.addTerm(new Atom("e"));
//
//        }
//
//        if(eventStructure != null)
//            EISAdapter.getSingleton().executeAction(agentMap.getAgentName(), eventStructure);
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//
//    }
}
