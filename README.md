# Independent Research Project: Explore A NEAT-ly Wumpus Reinforcement Model
### Author: Truong Nguyen Huy
### Major: Computer Science - Software Engineering
#### @ University of Illinois at Chicago


This research is based on the Wumpus World concept in Artificial Intelligence by Michael Genesereth, Wumpuslite model designed in Java by Professor James P. Biagioni in CS 511 – Artificial Intelligence II at UIC, and NeuroEvolution of Augmenting Topologies (NEAT - by Ken Stanley) algorithm. Under Professor Piotr Gmytrasiewicz’s guidance, I have added the factor of Multiagent to the model's environment simulation by giving the Wumpus a baseline agent paradigm. Furthermore, an expected final product of this research is a software that can assist AI researchers in "culturing" their own space of strategies/solutions to a problem. In this description, I will use neural network and solution interchangeably.

Wumpus World Background and research expectation:

The utility for the human agent is to maximize its performance measure by reaching the gold while maintaining the highest possible score, which is calculated based on a reward/penalty system. The utility of the Wumpus agent is to maximize its performance measure by terminating the human agent while maintaining its highest possible score, which is also calculated based on its respective reward/penalty system. The agents competing in this environment are expected to perform logical "reasoning" and strategic "planning" in order to nagivite its task without termination. The agents in this environment have the capabilities of learning, and knowledge-based agent models.

###### Notice: These files do not contain all of the coded work (for example, NEAT drivers, A2C drivers have been removed) due to the research's security.


![Login screen](src/main/resources/images/loginUI.PNG)

##### Figure 1: This is the login screen of the program. User can specify the Wumpus world' size (dimension of the 2D square grid world); number of trials (how many time the NEAT population is resetted and started anew); number of time steps (each time step is the time unit of a solution training session); max population (the max number of individal solution architectures in the NEAT population at any given training session); and number of episodes (the number of chances each solution gets to improve itself in a training session). 


I implemented the NeuroEvolution of Augmenting Topologies (NEAT) algorithm proposed by Kenneth O. Stanley (to learn more, please read research paper at http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf) as a control for non-deterministically producing agent functions/solution. My version of NEAT implementation involves initializing a population of primitively defined solution architectures/parameters and evolving them until a "well-performing" solution is found. At a high level, my implementation for evolution includes the following steps: speciating the population of solution architectures using a genomic distance function, having individuals in a species perform in the Wumpus environment while training them using the actor-critic method so high-performing individuals can be yielded while low-performing ones are evicted, terminating species that exceed the minimum threshold of individual counts, and finally, having successful species reproduce using a genetic algorithm to fill in the spot of evicted individuals. This implementation ensures that the inflow of newborn individual solutions and outflow of evicted solutions are maintained at consistent rates sustaining a stable population count. Moreover, speciation ensures the retention of a diverse population of solution architectures, thus, maintaining and parallelly developing a promising hypothesis space.

The NEAT Driver and NEAT UI Controllers had been completely implemented by Fall 2020.

![NEAT Lab scene](src/main/resources/images/labUI.PNG)

##### Figure 2: The NEAT Lab that the user can evolve, reset, view, etc. The neural network being spotlighted on screen is the fittest network after 183 evolution steps. The densely connected area is the Memory Retention Unit (MRU) while the parser counterpart is the Actor-Critic Unit (ACU). As a testing architecture for the Wumpus World task, the input layer to MRU consists of 6 observation nodes and 8 hidden nodes. At the output layer of the ACU, there are  6 nodes: 5 of them output the action probability distribution using softmax activation and 1 outputs the Critic's state-value opinion. Noted that the architectural information is shown for a node because I was hovering over it.

At the design level of the solution/network architectures, a Long Short-Term Memory (LSTM) is attached to an Actor-Critic architecture. The LSTM's role is to tune a useful sequential memory processor that maps the space of environment observation sequence to elevated feature space for the Actor-Critic architecture. Therefore, I named this area the "Memory Retention Unit" (MRU) of the network architecture. The "Actor-Critic Unit" (ACU) would map this feature space to an action-distribution and state-value space respectively. In a training session, after each full forward pass from percept history to action decision/state-value opinion, the environment returns rewards which are inputs to policy gradient calculation for actor training and loss minimization for critic training. The back gradients of these two ACU subunits are, then, passed into the Memory Retention Unit to "service" the LSTM gates.

Design decision: In the MRU, I decide that while each gate has its own set of parameters, their architectures should stay the same. Similarly, in the ACU, while Actor and Critic have their own respective set of parameters, Actor shares the same architecture with the Critic with some minor differences in output arrangements (as Actor is performing Logistic Classification and Critic is performing Regression).

The Wumpus Simulation Driver, Baseline Solution Architecture setup, and Simulation UI Controllers had been completely implemented by Winter 2020 and have been merged successfully with the NEAT Driver and Main UI Controllers.

![Simulation scene](src/main/resources/images/simUI.PNG)

##### Figure 3: A randomly generated simulation scene. The human can be seen on the sand tile and Wumpus on the top-left tile of the environment grid. The circles on right panes represent the perception channels for each agent in the environment. The listviews below log each agent's action decisions, step-rewards, and Actor-Critic processes for debugging. It is observed that the probability distribution of action decision at each time step is uniform and state-value opinion is far off from the Temporal Difference target, this shows that the solution architecure is still too simple. Further evolution steps are required. 

I want to examine if this baseline architectural design (LSTM - ActorCritic) of the agent function can lead to the development of functions that exhibit logical "reasoning" and strategic "planning". At the current state of the research, the model has been completely implemented and is to be optimized and supervised to check on how the human agent displays "rationality". So far, an observed shortcoming that inhibits effective evolution trajectories is the slow convergence-to-optima of the Memory Retention Unit. Coupled with the computational expensiveness of LSTMs, this area requires further optimization or even being replaced by a self-attention architecture.

TODO: An idea worth implementing is to a make a separate MRU that services Critic and a separate MRU that services the Actor. This could help decrease the variance that inhibit the convergence of the MRUs.
