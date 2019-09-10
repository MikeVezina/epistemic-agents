package eis.percepts.parsers;

public final class PerceptHandlerFactory {
    private static TaskHandler taskHandler;
    private static TerrainPerceptHandler terrainPerceptHandler;
    private static ThingPerceptHandler thingPerceptHandler;

    private PerceptHandlerFactory()
    {
        // Private constructor
    }

    public static TaskHandler getTaskHandler()
    {
        if(taskHandler == null)
            taskHandler = new TaskHandler();

        return taskHandler;
    }

    public static TerrainPerceptHandler getTerrainPerceptHandler()
    {
        if(terrainPerceptHandler == null)
            terrainPerceptHandler = new TerrainPerceptHandler();

        return terrainPerceptHandler;
    }

    public static ThingPerceptHandler getThingPerceptHandler()
    {
        if(thingPerceptHandler == null)
            thingPerceptHandler = new ThingPerceptHandler();

        return thingPerceptHandler;
    }
}
