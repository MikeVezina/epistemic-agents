package eis.view;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import utils.Position;

import java.util.List;

public class PerceptVisionOverlay extends Polygon {
    /**
     * Create a new bounding box
     *
     * @param x      The x position of the box
     * @param y      The y position of the box
     * @param width  The width of the box
     * @param height The hieght of the box
     */
    public PerceptVisionOverlay(int vision) {
        super();
        setVisionSize(vision);

    }

    private Shape curShape;
    private int vision;

    private void setPoints(float curX, float curY) {
        float midX = CustomPanel.WIDTH / 2.0f;
        float midY = CustomPanel.HEIGHT / 2.0f;


        // 4 points, each with a corresponding x & y coordinate
        float[] rotatedEnds = new float[8];

        // North point
        rotatedEnds[0] = curX + midX;
        rotatedEnds[1] = curY - calculateVisionHeight();

        // East point
        rotatedEnds[2] = curX + CustomPanel.WIDTH + calculateVisionWidth();
        rotatedEnds[3] = curY + midY;

        // South point
        rotatedEnds[4] = curX + midX;
        rotatedEnds[5] = curY + CustomPanel.HEIGHT + calculateVisionHeight();
        // West point
        rotatedEnds[6] = curX - calculateVisionWidth();
        rotatedEnds[7] = curY + midY;

        super.points = rotatedEnds;
        super.pointsDirty = true;
        super.checkPoints();
    }

    private float calculateVisionHeight() {
        return vision * CustomPanel.HEIGHT;
    }

    private float calculateVisionWidth() {
        return vision * CustomPanel.WIDTH;
    }

    public void setVisionSize(int visionSize) {
        vision = visionSize;
    }

    public void update(Position currentLocation) {
        this.setPoints(currentLocation.getX() * CustomPanel.WIDTH, currentLocation.getY() * CustomPanel.HEIGHT);


    }

    public void draw(Graphics g) {
        Color transRed = new Color(1.0f, 0.0f, 0.0f, 0.2f);
        g.setColor(transRed);
        g.fill(this);
    }
}
