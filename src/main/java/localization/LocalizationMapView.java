package localization;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.*;
import java.awt.geom.Path2D;

public class LocalizationMapView extends GridWorldView {

    private final LocalizationMapModel model;


    private LocalizationMapView(LocalizationMapModel model) {
        super(model,
                "Localization Map",
                500);

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.model = model;
        this.getCanvas().addKeyListener(model);
    }

    public void populateModel(Location agentLocation) {
        this.model.populateModel(agentLocation);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if((object & LocalizationMapModel.GOAL) != 0) {
            drawGoal(g, x, y);
        }

        if((object & LocalizationMapModel.POSSIBLE) != 0) {
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
        g.setColor(Color.RED);
        g.drawOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
    }

    public LocalizationMapView() {
        this(new LocalizationMapModel(5, 5, 1));
    }

    @Override
    public LocalizationMapModel getModel() {
        return model;
    }


}

