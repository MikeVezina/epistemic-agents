# MASSim 2019
## Current Development Iteration: Iteration 0.5
Contest Details and Simulator can be found at https://multiagentcontest.org/2019/.
The following Gradle dependencies must be obtained and installed manually:
- eismassim 4.0 (with dependencies). Found in the simulator release: https://github.com/agentcontest/massim_2019/releases

# Progress
#### Iteration 0 Progress
##### Requirements:
1. (DONE) Choose a task and parse requirements
2. (DONE) Choose a requirement
3. (DONE) Find block dispenser for requirement and align itself to the dispenser
4. (DONE) Find Goal location and navigate to it
5. (DONE) Submit Task

##### Issues & Further Comments:
- We need multiple agents to create task patterns with more than one block.
- Found issue with contest simulator and submitted GitHub issue & fix. (Resolved)
- Code base is getting large, 80% of features still need to be implemented, bringing us to the following points:
  - There needs to be testing. Too many moving components are making it difficult to determine what's causing issues.
  - The code base needs to be refactored a bit more to make it more modular. Testing needs to be in place first to make sure nothing gets broken.

#### Iteration 0.5 Progress
- Agent handles random (probablistic) action failures by repeating last attempted action (slippery vaccuum world)

# Development Iterations
The following sections describe the various milestones for the Agent.

## Iteration 0: MVP
This milestone represents the minimal deliverables required to make a functional agent. The Agent should be able to:

1. Choose a task and parse requirements
2. Choose a requirement
3. Find block dispenser for requirement and align itself to the dispenser
4. Find Goal location and navigate to it
5. Submit Task

In this milestone, we make the following simplifications using the server config file provided by the simulation server:

1. No obstacles (no need to perform complicated pathfinding)
2. No Action failures
3. Agent has unlimited visibility (no exploring/searching required)
4. Only one Agent (no competing teams, no agent communication required)
5. Agent ignores task deadlines
6. Task requirements only require 1 block attached


## Iteration 0.5: Better MVP
Building off of Iteration 0, we remove some of the simplifications. The Agent is required to perform with Action failures, additional agent teammates, task deadlines, and block patterns with 1 or 2 block requirements.

The agent is expected to perform under the following circumstances (requirements modified from Iteration 0 are in **bold**):
1. No Obstacles
2. **Action Failures Occur** (Make sure all action failures are handled accordingly)
3. Agent has unlimited visibility.
4. **Two (or more) agents on the same team.**
5. **Task deadlines are considered in task selection.**
6. **Task requirements require 1-2 blocks.**


## Iteration 1: Pathfinding & Complex Block Pattern Agent
The agent behaviour should be able to perform in the competition on a very basic level. This iteration introduces obstacles and complex block patterns.

Building off of Iteration 0.5, we remove more of the simplifications. The agent is expected to perform under the following circumstances (requirements modified from Iteration 0.5 are in **bold**):
1. **Obstacles & Pathfinding** (assume no attached blocks)
2. Action Failures Occur (Make sure all action failures are handled accordingly)
3. Agent has unlimited visibility.
4. Two (or more) agents on the same team.
5. Task deadlines are considered in task selection.
6. **Task requirements require 1 to many blocks.**


## Iteration 2: Limited Visibility Agent
This iteration reduces the visibility of the agent. This forces the agent to build a mental model of the map and to keep it updated. This mental model of the map should be shared across agents.

**Note**: Instead of the Agent handling the map model building, maybe this can be done by the environment. This might be too much responsibility for the agent. In this case, the environment would receive the percepts from the contest server and build the mental model, which would then be parsed and passed to the agent so that the agent can perform the higher level behaviour such as navigation, etc. Sharing the (parsed) mental model could be done by the agent.

The agent is expected to perform under the following circumstances (requirements modified from Iteration 1 are in **bold**):
1. Obstacles & Pathfinding (assume no attached blocks)
2. Action Failures Occur (Make sure all action failures are handled accordingly)
3. **Agent has limited visibility.** (Build and share mental model of map)
4. One team only.
5. Task deadlines are considered in task selection.
6. Task requirements require 1 to many blocks.


## Iteration 3: Advanced Behaiour
At this stage, the agent will be mostly feature complete. We look to enhance the behaviour developed by the previous iterations. We look to introduce the following additions/modifications:
1. Competing team.
2. Advanced pathfinding (includes attached blocks)
3. Advanced block gathering (agent can gather more than one block at a time on each side if it saves time, and can look for free blocks outside of the dispenser)
4. Consideration for clear events.


## Iteration 4: Temporal Agent
The goal of this iteration is to perform more advanced temporal reasoning about task deadlines. For example, can we estimate how long it will take to complete a task. There can be various strategies for this, including: chronicles and execution profiling. It is important to have a fully functioning agent before this stage and to establish a baseline for performance metrics. Any performance improvements (or performance hindrances) can be noted as a result of temporal reasoning.
