package eis.percepts.parsers;

public final class PerceptMapperFactory {
    private static TaskMapper taskMapper;
    private static TerrainPerceptMapper terrainPerceptMapper;
    private static ThingPerceptMapper thingPerceptMapper;

    private PerceptMapperFactory()
    {
        // Private constructor
    }

    public static TaskMapper getTaskMapper()
    {
        if(taskMapper == null)
            taskMapper = new TaskMapper();

        return taskMapper;
    }

    public static TerrainPerceptMapper getTerrainPerceptMapper()
    {
        if(terrainPerceptMapper == null)
            terrainPerceptMapper = new TerrainPerceptMapper();

        return terrainPerceptMapper;
    }

    public static ThingPerceptMapper getThingPerceptMapper()
    {
        if(thingPerceptMapper == null)
            thingPerceptMapper = new ThingPerceptMapper();

        return thingPerceptMapper;
    }
}
