# Epistemic Agents 
[![CircleCI](https://circleci.com/gh/MikeVezina/epistemic-agents/tree/master.svg?style=svg&circle-token=d7ce6dbdee725382aab008ae3406668de1e409d7)](https://circleci.com/gh/MikeVezina/epistemic-agents/tree/master)

- Tests are currently out-of-date but implementation is working (prioritizing implementation completion for deadline)

- **Requirement**: The epistemic reasoner used by these agents can be found at: https://github.com/MikeVezina/epistemic-reasoner
- **Example**: A demo of how this framework can be used is shown for agent localization at: https://github.com/MikeVezina/localization-demo


## Install Gradle Local Dependency
In order to use the epistemic agent framework with other projects, you must install it into your local gradle repository (it is currently not being published to maven central). 

To do this, make sure gradle is installed, clone the repository, and run:
`gradle install`

- If the tests are not passing, run: `gradle install -x test`
