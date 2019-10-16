package view;

import com.google.gson.Gson;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import messages.AgentContainerMessage;
import serializers.GsonInstance;
import messages.MQReceiver;
import messages.Message;
import map.MapPercept;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.*;
import map.Position;
import org.newdawn.slick.geom.Rectangle;

import java.util.*;

public class GridVisualizer extends BasicGame implements DeliverCallback {

    private static final int ROWS = 100;
    private static final int COLS = 100;
    private boolean showDebug = true;

    public CustomPanel[][] map;
    private MQReceiver mqReceiver;
    private Position currentAgentPosition;
    private long currentStep;
    private PerceptVisionOverlay overlay;
    private Map<String, Position> authenticatedAgents;
    private List<Position> currentPath;

    // The panel that is selected by the mouse
    private CustomPanel currentPanel;
    private AgentContainerMessage agentContainer;

    public GridVisualizer(String agentName) {
        super(agentName);
        this.authenticatedAgents = new HashMap<>();
        this.currentPath = new ArrayList<>();
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        container.setTargetFrameRate(60);
        container.setAlwaysRender(true);
        mqReceiver = new MQReceiver(this.getTitle(), this);
        container.setClearEachFrame(false);
        resetFrame();
    }

    @Override
    public void keyReleased(int key, char c) {
        // F12
        if(key == 88)
            showDebug = !showDebug;

        super.keyReleased(key, c);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        for (CustomPanel[] customPanels : map) {
            for (CustomPanel customPanel : customPanels) {
                if (customPanel != null)
                    customPanel.updatePanel();
            }
        }

        container.setClearEachFrame(false);
        container.setShowFPS(showDebug);

        overlay.update(translateAgentPositionToMap(currentAgentPosition));

        // Only update the panel if the mouse is grabbed
        currentPanel = getMouseHoverPanel(container);
    }
//
//    public boolean getAuthenticatedAgent(MapPercept percept) {
//        if (percept == null)
//            return false;
//
//        return authenticatedAgents.contains(percept.getLocation());
//    }

    private CustomPanel getMouseHoverPanel(GameContainer container) {
        if (!Mouse.isInsideWindow())
            return null;

        int mouseX = Mouse.getX();
        int mouseY = container.getHeight() - Mouse.getY();

        int panelX = Math.min(mouseX / CustomPanel.WIDTH, COLS - 1);
        int panelY = Math.min(mouseY / CustomPanel.HEIGHT, ROWS - 1);
        return map[panelX][panelY];
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        for (CustomPanel[] customPanels : map) {
            for (CustomPanel currentPanel : customPanels) {
                if (currentPanel != null)
                    currentPanel.draw(g);
            }
        }

        g.setColor(Color.red);

        // Draw Percept overlay
        if (currentStep > 0)
            overlay.draw(g);

        if(!currentPath.isEmpty())
        {
            for(Position pos : currentPath)
            {
                Position actualLoc = translateAgentPositionToPanelLocation(pos);
                Color c = new Color(Color.blue);
                c.a = 0.2f;
                g.setColor(c);
                g.fill(new Rectangle(actualLoc.getX(), actualLoc.getY(), CustomPanel.WIDTH, CustomPanel.HEIGHT));
            }
        }

        resetDebugStringPosition();


        // Draw Info Overlay
        g.setColor(Color.white);
        writeDebugString(g, "Current Step: " + getCurrentStep());
        if(agentContainer != null)
            writeDebugString(g, "Score: " + agentContainer.getScore());

        if (currentPanel != null) {
            writeDebugString(g, "----------");
            writeDebugString(g, "Current Cell Info:");

            if (currentPanel.getPercept() == null) {
                writeDebugString(g, "No Percept Available.");
            } else {
                writeDebugString(g, "Absolute Location: " + currentPanel.getPercept().getLocation());
                writeDebugString(g, "Perceived By: " + currentPanel.getPercept().getAgentSource());
                writeDebugString(g, "Last Step Perceived: " + currentPanel.getPercept().getLastStepPerceived());

                writeDebugString(g, "Terrain Info: " + currentPanel.getPercept().getTerrain().toString());
                writeDebugString(g, "Thing Info: " + currentPanel.getPercept().getThingList());
            }
        }

        if (!authenticatedAgents.isEmpty())
            writeDebugString(g, "----------");

        for (var e : authenticatedAgents.entrySet()) {
            writeDebugString(g, e.getKey() + ": " + e.getValue().toString());
            Position translated = translateAgentPositionToPanelLocation(e.getValue());
            g.setColor(Color.black); g.setAntiAlias(true);
            g.drawString(e.getKey(), translated.getX(), translated.getY());

        }

    }

    private int startingY;

    private void resetDebugStringPosition() {
        startingY = 50;
    }


    private void writeDebugString(Graphics g, String debugInfo) {
        if(!showDebug)
            return;

        g.drawString(debugInfo, 10, startingY);
        startingY += 20;
    }

    @Override
    public boolean closeRequested() {
        mqReceiver.close();
        return super.closeRequested();
    }

    private void resetFrame() {
        currentAgentPosition = new Position();
        overlay = new PerceptVisionOverlay(5);
        authenticatedAgents.clear();
        currentPath.clear();

        map = new CustomPanel[ROWS][COLS];

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                CustomPanel newPanel = new CustomPanel(this, new Position(i * CustomPanel.WIDTH, j * CustomPanel.HEIGHT));
                map[i][j] = newPanel;
            }
        }
    }

    public boolean isAgentCell(MapPercept percept) {
        return percept != null && percept.getLocation().equals(currentAgentPosition);
    }

    public Position translateAgentPositionToMap(Position pos) {
        return pos.add(new Position(ROWS / 2, COLS / 2));
    }

    public Position translateAgentPositionToPanelLocation(Position pos) {
        return pos.add(new Position(ROWS / 2, COLS / 2)).multiply(CustomPanel.HEIGHT);
    }

    public void updateGridLocation(MapPercept percept) {
        if (percept == null) {
            return;
        }

        Position translated = translateAgentPositionToMap(percept.getLocation());
        updatePanel(translated, percept);
    }

    private void updatePanel(Position panelPosition, MapPercept percept) {
        if (panelPosition.getX() >= map.length || panelPosition.getY() >= map.length || panelPosition.getX() < 0 || panelPosition.getY() < 0)
            return;

        try {
            CustomPanel panel = map[panelPosition.getX()][panelPosition.getY()];
            panel.setPercept(percept);
        } catch (NullPointerException npe) {
            System.out.println("Test!!!!");
            throw npe;
        }
    }

    @Override
    public void handle(String consumerTag, Delivery message) {
        Gson gson = GsonInstance.getInstance();
        String msgBodyString = new String(message.getBody());
        if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_RESET)) {
            resetFrame();
        } else if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_PATH)) {
            currentPath = gson.fromJson(msgBodyString, Message.POSITION_LIST_TYPE);
            System.out.println(currentPath);
        } else if (message.getProperties().getContentType().equals(Message.CONTENT_TYPE_AGENT_CONTAINER)){
            this.setAgentContainer(gson.fromJson(msgBodyString, AgentContainerMessage.class));

        }

    }

    private void setAgentContainer(AgentContainerMessage agentContainerMessage)
    {
        this.agentContainer = agentContainerMessage;
        setAgentPosition(agentContainer.getCurrentLocation());
        agentContainer.getCurrentStepChunks().forEach(this::updateGridLocation);
        setCurrentStep(agentContainer.getCurrentStep());

        authenticatedAgents = agentContainer.getAuthenticatedTeammatePositions();

    }

    private void setAgentPosition(Position fromJson) {
        this.currentAgentPosition = fromJson;
    }

    private void setCurrentStep(long stepInt) {
        this.currentStep = stepInt;
    }

    public long getCurrentStep() {
        return this.currentStep;
    }

    public static void main(String[] args) throws SlickException {
        if (args.length != 1) {
            System.out.println("Invalid Arguments. Missing Agent name.");
            System.out.println("Usage: GridVisualizer [agent-name]+");
            return;
        }

        String agentName = args[0];

        AppGameContainer appGameContainer = new AppGameContainer(new GridVisualizer(agentName));
        appGameContainer.setDisplayMode(GridVisualizer.COLS * CustomPanel.WIDTH, GridVisualizer.ROWS * CustomPanel.HEIGHT, false);
        appGameContainer.start();


//        new GridVisualizer(agentName);
    }
}
