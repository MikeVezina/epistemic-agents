package localization.models;

import jason.environment.grid.Location;

public class MapMarker {
    private Location location;
    private Integer type;
    public MapMarker(Location location, Integer type)
    {
        this.location = location;
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
