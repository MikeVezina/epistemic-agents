package eis.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import eis.percepts.terrain.*;
import eis.percepts.things.*;

public class GsonInstance {
    private static Gson gson;

    private static void setInstance()
    {
        RuntimeTypeAdapterFactory<Terrain> terrainAdapter =
                RuntimeTypeAdapterFactory
                        .of(Terrain.class)
                        .registerSubtype(ForbiddenCell.class)
                        .registerSubtype(FreeSpace.class)
                        .registerSubtype(Obstacle.class)
                        .registerSubtype(Goal.class);

        RuntimeTypeAdapterFactory<Thing> thingAdapter =
                RuntimeTypeAdapterFactory
                        .of(Thing.class)
                        .registerSubtype(Entity.class)
                        .registerSubtype(Block.class)
                        .registerSubtype(Dispenser.class)
                        .registerSubtype(Marker.class);

        gson = (new GsonBuilder()).setPrettyPrinting().registerTypeAdapterFactory(terrainAdapter).registerTypeAdapterFactory(thingAdapter).create();
    }

    public static Gson getInstance()
    {
        if(gson == null)
            setInstance();

        return gson;
    }
}
