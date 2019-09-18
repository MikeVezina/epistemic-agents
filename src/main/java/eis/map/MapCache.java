package eis.map;

import eis.percepts.terrain.FreeSpace;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MapCache extends ConcurrentHashMap<Position, MapPercept> {
    private Set<Position> terrainCache;
    private Set<Position> thingCache;

    public MapCache()
    {
        terrainCache = new HashSet<>();
        thingCache = new HashSet<>();
    }

    @Override
    public synchronized MapPercept put(Position key, MapPercept value) {
        // Remove any existing cache values
        terrainCache.remove(key);
        thingCache.remove(key);

        if(!(value.getTerrain() instanceof FreeSpace))
            terrainCache.add(key);

        if(!value.getThingList().isEmpty())
            thingCache.add(key);

        return super.put(key, value);
    }

    /**
     * @return Get the list of MapPercepts that have terrain (other than a free space)
     */
    public synchronized List<MapPercept> getCachedTerrain()
    {
        return terrainCache.parallelStream().map(this::get).collect(Collectors.toList());
    }

    /**
     * @return Get the list of MapPercepts that have things
     */
    public synchronized List<MapPercept> getCachedThingList()
    {
        return thingCache.stream().map(this::get).collect(Collectors.toList());
    }

    @Override
    public synchronized void putAll(Map<? extends Position, ? extends MapPercept> mapPercepts) {
        for(var p : mapPercepts.entrySet())
            this.put(p.getKey(), p.getValue());
    }
}
