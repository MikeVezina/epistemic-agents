package eis.percepts;

import eis.iilang.Percept;
import eis.percepts.terrain.Goal;
import eis.percepts.terrain.Obstacle;
import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PerceptContainer {

    private List<Terrain> terrainList;
    private List<Task> taskList;
    private List<Thing> thingList;
    private SimulationInfo simulationInfo;
    private AgentInfo agentInfo;

    public PerceptContainer() {
    }

    private List<Goal> getGoalTerrain() {
        return terrainList.stream().filter(t -> t instanceof Goal).map(t -> (Goal) t).collect(Collectors.toList());
    }

    private List<Obstacle> getObstacleTerrain() {
        return terrainList.stream().filter(t -> t instanceof Obstacle).map(t -> (Obstacle) t).collect(Collectors.toList());
    }

    public List<Terrain> getTerrainList() {
        return terrainList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public static PerceptContainer parsePercepts(Collection<Percept> percepts) {
        PerceptContainer pAdapter = new PerceptContainer();

        // Parse Tasks and terrain lists.
        List<Task> tasks = percepts.stream().filter(Task::canParse).map(Task::parseTask).collect(Collectors.toList());
        List<Terrain> terrainList = percepts.parallelStream().filter(Terrain::canParse).map(Terrain::parseTerrain).collect(Collectors.toList());
        List<Thing> thingList = percepts.parallelStream().filter(Thing::canParse).map(Thing::ParseThing).collect(Collectors.toList());

        pAdapter.taskList = tasks;
        pAdapter.terrainList = terrainList;
        pAdapter.thingList = thingList;


        // Query Simulation info
        List<Percept> simulationInfoPercepts = percepts.parallelStream().filter(SimulationInfo::isSimPercept).collect(Collectors.toList());
        SimulationInfo simulationInfo = SimulationInfo.parseSimulationPercepts(simulationInfoPercepts);
        pAdapter.simulationInfo = simulationInfo;

        return pAdapter;


//        {
//            if(Task.canParse(p))
//                pAdapter.addTask(Task.parseTask(p));
//
//            if(Step.canParse(p))
//                throw new RuntimeException();
//
//            if(p.getFunctor().equalsIgnoreCase("score"))
//                throw new RuntimeException();
//        });

    }
}
