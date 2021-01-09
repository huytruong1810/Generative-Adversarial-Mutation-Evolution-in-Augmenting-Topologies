# WumpusWorld - Independent Research Project
## Project's name: Explore the Development of A NEAT Multi-Agent Wumpus World
#### Author: Truong Nguyen Huy
#### At University of Illinois at Chicago


This research is based on the Wumpus World concept in Artificial Intelligence by Michael Genesereth, Wumpuslite model designed in Java by Professor James P. Biagioni in CS 511 – Artificial Intelligence II at UIC, and NeuroEvolution of Augmenting Topologies (NEAT - by Ken Stanley) algorithm. Under Professor Piotr Gmytrasiewicz’s guidance, I have added the factor of Multiagent to the model's environment simulation by giving the Wumpus a baseline agent paradigm.

Wumpus World Background and research expectation:

The utility for the human agent is to maximize its performance measure by reaching the gold while maintaining the highest possible score, which is calculated based on a reward/penalty system. The utility of the Wumpus agent is to maximize its performance measure by terminating the human agent while maintaining its highest possible score, which is also calculated based on its respective reward/penalty system. The agents competing in this environment are expected to perform logical reasoning in order to maximize their respective scores. At a minimum, the agents are expected to have the capabilities of learning, and knowledge-based agent models.


![Login screen](src/main/resources/images/loginUI.PNG)

##### Figure 1: This is the login screen of the program. User can specify the world' size, dimension of the 2D square grid world; number of trials, how many time the NEAT population is resetted and started anew; number of time steps, how long the environment will exist for; max population, the max number of individal neural networks in the NEAT population at any given time; and number of episodes, the number of lives each neural network gets to improve itself using Advantage Actor-Critic method. 


The first step of this research was to implement NeuroEvolution of Augmenting Topologies (NEAT) algorithm by Ken Stanley (to learn more, please read research paper at http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf). The population of neural networks are to be trained using policy gradient and state-value approximation and evaluated using a Wumpus enviroment simulator with a reward/penalty system. These networks are to be used to express the human's agent function alone (for now). The NEAT Driver and UI Controllers had been completed by Fall 2020.

![NEAT Lab scene](src/main/resources/images/labUI.PNG)

##### Figure 2: The NEAT Lab that the user can evolve, reset, view, etc. the NEAT population of neural networks for the human's agent function


The second step was to implement the Wumpus environment and Advantage Actor-Critic (A2C) algorithm, which is the basis for competition in the NEAT population. In brief, in each evolution step, each neural networks in the population will be trained on the Wumpus environment using Advantage Actor-Critic method for a number of episose. Successful NEAT species and networks will be selected for reproduction and offsrings will be added to the places where weak individuals were evicted from the population (maintaining a constant amount of individuals in the population). Networks with close genetic relations will be grouped in species and will only compete within their respective species to preserve newly developed architectual mutations. The Wumpus Simulation Driver and UI Controllers had been completed by Winter 2020 and have been merged successfully with the NEAT Driver and UI Controllers.

![Simulation scene](src/main/resources/images/simUI.PNG)

##### Figure 3: A randomly generated simulation scene. The human can be seen on the sand tile and Wumpus on the top-left tile of the environment grid. The circles on right panes represent the perception channels for each agent in the environment. The listviews below log each agent's action decisions, step-rewards, and Actor-Critic processes for debugging.


TODO: The third step is to run, test, and supervise to observe how the human agent behave and become "successful" in this single-agent environment. The fourth step will be to try implementing the NEAT classes so that a NEAT population can be developed for the Wumpus's agent function.
