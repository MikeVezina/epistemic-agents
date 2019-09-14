package utils.visuals;

import com.google.gson.Gson;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import eis.agent.AgentLocation;
import eis.messages.GsonInstance;
import eis.messages.MQReceiver;
import eis.messages.Message;
import eis.percepts.MapPercept;
import utils.Position;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GridVisualizer extends JFrame implements DeliverCallback {

    private static final int ROWS = 80;
    private static final int COLS = 80;


    public CustomPanel[][] map;
    private MQReceiver mqReceiver;
    private Position currentAgentPosition;
    private int currentStep;

    public GridVisualizer(String agentName) {
        setTitle(agentName);
        mqReceiver = new MQReceiver(agentName, this);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mqReceiver.close();
                super.windowClosing(e);
            }
        });

        resetFrame();
    }

    private synchronized void resetFrame() {
        currentAgentPosition = new Position();
        getContentPane().removeAll();

        this.setLayout(new GridLayout(ROWS, COLS));
        map = new CustomPanel[ROWS][COLS];

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                CustomPanel newPanel = new CustomPanel(this);
                map[i][j] = newPanel;
                add(newPanel);
            }
        }
        setMinimumSize(new Dimension(COLS * CustomPanel.WIDTH, ROWS * CustomPanel.HEIGHT));
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        repaint();
    }

    public synchronized boolean isAgentCell(MapPercept percept) {
        return percept != null && percept.getLocation().equals(currentAgentPosition);
    }

    public void updateGridLocation(MapPercept percept) {
        if (percept == null) {

            return;
        }
        Position translated = percept.getLocation().add(new Position(ROWS / 2, COLS / 2));
        updatePanel(translated, percept);
    }

    private void updatePanel(Position panelPosition, MapPercept percept) {
        if (panelPosition.getX() >= map.length || panelPosition.getY() >= map.length || panelPosition.getX() < 0 || panelPosition.getY() < 0)
            return;

        try {
            CustomPanel panel = map[panelPosition.getY()][panelPosition.getX()];
            panel.updatePercept(percept);
            repaint();
        }
        catch (NullPointerException npe)
        {
            System.out.println("Test!!!!");
            throw npe;
        }
    }

    @Override
    public void handle(String consumerTag, Delivery message) {
        Gson gson = GsonInstance.getInstance();
        String msgBodyString = new String(message.getBody());
        if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_LOCATION)) {
            this.setAgentPosition(gson.fromJson(msgBodyString, Position.class));
        } else if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_RESET)) {
            resetFrame();
        } else if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_PERCEPT)) {
            Collection<MapPercept> perceptChunk = gson.fromJson(msgBodyString, Message.MAP_PERCEPT_LIST_TYPE);
            perceptChunk.forEach(this::updateGridLocation);
            repaint();
        } else if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_NEW_STEP)) {
            String stepString = new String(message.getBody());
            int stepInt = Integer.parseInt(stepString);
                this.setCurrentStep(stepInt);
        } else {
            System.out.println("Unknown Message Content type. Content Type: " + message.getProperties().getContentType());
        }
    }

    private synchronized void setAgentPosition(Position fromJson) {
        this.currentAgentPosition = fromJson;
    }

    private synchronized void setCurrentStep(int stepInt) {
        this.currentStep = stepInt;
    }

    public synchronized int getCurrentStep() {
        return this.currentStep;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid Arguments. Missing Agent name.");
            System.out.println("Usage: GridVisualizer [agent-name]+");
            return;
        }

        String agentName = args[0];
        new GridVisualizer(agentName);
    }
}
