package localization.view;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import localization.models.LocalizationMapModel;

import java.awt.*;

public class LocalizationMapView extends GridWorldView {

    private final LocalizationMapModel model;
    private SettingsPanel settingsPanel;


    private LocalizationMapView(LocalizationMapModel model) {
        super(model,
                "Localization Map",
                500);

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.model = model;
        this.getCanvas().addKeyListener(model);
        this.getModel().addMapListener(settingsPanel);
    }

    @Override
    public void initComponents(int width) {
        super.initComponents(width);

        settingsPanel = new SettingsPanel(this);

        // Initialize settings bar
        super.getContentPane().add(BorderLayout.SOUTH, settingsPanel);
    }


    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if ((object & LocalizationMapModel.GOAL) != 0) {
            drawGoal(g, x, y);
        }

        if ((object & LocalizationMapModel.POSSIBLE) != 0) {
//            drawEmpty(g,x,y);
            drawPossible(g, x, y);
        }
    }

    private void drawGoal(Graphics g, int x, int y) {
        g.setColor(Color.ORANGE);
        g.fillRoundRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4, 5, 5);

        g.setColor(Color.BLACK);
        g.drawString("GOAL", x * cellSizeW + (cellSizeW / 2) - 16, y * cellSizeH + (cellSizeH / 2));
    }

    private void drawPossible(Graphics g, int x, int y) {
        if (!settingsPanel.showPossible())
            return;

        g.setColor(Color.RED);
        if(model.getAgAtPos(x, y) == -1)
            g.drawOval(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2);
        else
        {
            g.fillOval(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
    }

    public LocalizationMapView() {
        this(LocalizationMapModel.loadFromFile());
    }

    @Override
    public LocalizationMapModel getModel() {
        return model;
    }

    public SettingsPanel getSettingsPanel()
    {
        return this.settingsPanel;
    }


}

