package localization.models;

import jason.environment.grid.Location;

import java.util.List;

public class LocalizationMap {
    private Integer width;
    private Integer height;
    private Location agentStart;
    private List<MapMarker> markers;

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Location getAgentStart() {
        return agentStart;
    }

    public List<MapMarker> getMarkers() {
        return markers;
    }
}
