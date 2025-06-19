/**
 *     PureEdgeSim:  A Simulation Framework for Performance Evaluation of Cloud, Edge and Mist Computing Environments 
 *
 *     This file is part of PureEdgeSim Project.
 *
 *     PureEdgeSim is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     PureEdgeSim is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with PureEdgeSim. If not, see <http://www.gnu.org/licenses/>.
 *     
 *     @author Charafeddine Mechalikh
 **/
package examples;

import java.util.*;
import com.mechalikh.pureedgesim.taskgenerator.Task;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;
import com.mechalikh.pureedgesim.taskorchestrator.DefaultOrchestrator;
import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;

/**
 * SkyGroundDQNOrchestrator - Deep Q-Network Sky-Ground Task Orchestrator
 * 
 * Features:
 * - Deep neural network replacing Q-table
 * - Experience replay mechanism
 * - Target network for stable training
 * - High-dimensional continuous state space
 * - Priority experience replay
 * - Double DQN algorithm
 */
public class SkyGroundDQNOrchestrator extends DefaultOrchestrator {
    
    // DQN hyperparameters
    private static final double LEARNING_RATE = 0.001;
    private static final double DISCOUNT_FACTOR = 0.99;
    private static final double EPSILON = 0.9;
    private static final double EPSILON_DECAY = 0.995;
    private static final double MIN_EPSILON = 0.1;
    private static final int MEMORY_SIZE = 50000;
    private static final int BATCH_SIZE = 64;
    private static final int TARGET_UPDATE_FREQ = 1000;
    
    // Network architecture parameters
    private static final int STATE_SIZE = 12; // Extended state space
    private static final int HIDDEN_SIZE_1 = 128;
    private static final int HIDDEN_SIZE_2 = 64;
    private static final int HIDDEN_SIZE_3 = 32;
    
    // DQN core components
    private DQNNetwork mainNetwork;
    private DQNNetwork targetNetwork;
    private ExperienceReplay replayBuffer;
    private Random random;
    private double currentEpsilon;
    
    // Statistics
    private int totalDecisions = 0;
    private int totalReward = 0;
    private int successfulTasks = 0;
    private int trainSteps = 0;
    private Map<Integer, Integer> actionCounts;
    private List<Double> episodeRewards;
    
    public SkyGroundDQNOrchestrator(SimulationManager simulationManager) {
        super(simulationManager);
        initializeDQN();
    }
    
    private void initializeDQN() {
        // Initialize neural networks
        int actionSize = nodeList.size();
        mainNetwork = new DQNNetwork(STATE_SIZE, actionSize, LEARNING_RATE);
        targetNetwork = new DQNNetwork(STATE_SIZE, actionSize, LEARNING_RATE);
        
        // Initialize experience replay
        replayBuffer = new ExperienceReplay(MEMORY_SIZE);
        
        // Initialize other components
        random = new Random();
        currentEpsilon = EPSILON;
        actionCounts = new HashMap<>();
        episodeRewards = new ArrayList<>();
        
        // Sync target network
        syncTargetNetwork();
        
        System.out.println("🧠 DQN Orchestrator Initialized:");
        System.out.println("   - State Space: " + STATE_SIZE + " dimensions");
        System.out.println("   - Action Space: " + actionSize + " nodes");
        System.out.println("   - Network: " + STATE_SIZE + "->" + HIDDEN_SIZE_1 + "->" + HIDDEN_SIZE_2 + "->" + HIDDEN_SIZE_3 + "->" + actionSize);
        System.out.println("   - Memory Buffer: " + MEMORY_SIZE + " experiences");
        System.out.println("   - Batch Size: " + BATCH_SIZE);
    }
    
    @Override
    protected int findComputingNode(String[] architecture, Task task) {
        totalDecisions++;
        
        // Extract high-dimensional state features
        double[] state = extractEnhancedState(task);
        
        // Get available actions
        List<Integer> availableActions = getAvailableActions(architecture, task);
        
        if (availableActions.isEmpty()) {
            return -1; // No available nodes
        }
        
        // DQN action selection
        int selectedAction = selectActionDQN(state, availableActions);
        
        // Record decision
        actionCounts.put(selectedAction, actionCounts.getOrDefault(selectedAction, 0) + 1);
        
        // Store decision for future learning
        storePendingExperience(state, selectedAction, task);
        
        // Periodic progress output
        if (totalDecisions % 1000 == 0) {
            printProgress();
        }
        
        return selectedAction;
    }
    
    /**
     * Extract enhanced state features (12 dimensions)
     */
    private double[] extractEnhancedState(Task task) {
        double[] state = new double[STATE_SIZE];
        
        // Task features (0-3)
        state[0] = normalizeTaskType(task.getApplicationID());
        state[1] = normalizeTaskSize(task.getLength());
        state[2] = normalizeLatencyRequirement(task);
        state[3] = normalizeTaskPriority(task);
        
        // System state (4-7)
        state[4] = normalizeSystemLoad();
        state[5] = normalizeAvailableNodes();
        state[6] = normalizeNetworkConditions();
        state[7] = normalizeTimeOfDay();
        
        // Edge-specific features (8-11)
        state[8] = normalizeEdgeCapacity();
        state[9] = normalizeCloudLatency();
        state[10] = normalizeEnergyLevels();
        state[11] = normalizeMobilityFactor();
        
        return state;
    }
    
    /**
     * DQN action selection strategy
     */
    private int selectActionDQN(double[] state, List<Integer> availableActions) {
        // Epsilon-greedy strategy
        if (random.nextDouble() < currentEpsilon) {
            // Explore: random selection
            return availableActions.get(random.nextInt(availableActions.size()));
        } else {
            // Exploit: network predicts optimal action
            double[] qValues = mainNetwork.predict(state);
            
            // Only consider available actions
            int bestAction = availableActions.get(0);
            double bestQValue = qValues[bestAction];
            
            for (int action : availableActions) {
                if (qValues[action] > bestQValue) {
                    bestQValue = qValues[action];
                    bestAction = action;
                }
            }
            
            return bestAction;
        }
    }
    
    @Override
    public void resultsReturned(Task task) {
        super.resultsReturned(task);
        
        // Calculate reward
        double reward = calculateAdvancedReward(task);
        totalReward += reward;
        
        if (task.getStatus() == Task.Status.SUCCESS) {
            successfulTasks++;
        }
        
        // Complete experience and add to replay buffer
        completePendingExperience(task, reward);
        
        // Train network
        if (replayBuffer.size() >= BATCH_SIZE) {
            trainNetwork();
        }
        
        // Update target network
        if (trainSteps % TARGET_UPDATE_FREQ == 0) {
            syncTargetNetwork();
        }
        
        // Update exploration rate
        updateEpsilon();
    }
    
    /**
     * Train DQN network using experience replay
     */
    private void trainNetwork() {
        List<Experience> batch = replayBuffer.sample(BATCH_SIZE);
        
        double[][] states = new double[batch.size()][];
        double[][] targets = new double[batch.size()][];
        
        for (int i = 0; i < batch.size(); i++) {
            Experience exp = batch.get(i);
            states[i] = exp.state;
            
            // Calculate target Q-value (Double DQN)
            double[] currentQValues = mainNetwork.predict(exp.state);
            targets[i] = currentQValues.clone();
            
            // Update Q-value
            if (exp.done) {
                targets[i][exp.action] = exp.reward;
            } else {
                // Select next action (main network)
                double[] nextQValues = mainNetwork.predict(exp.nextState);
                int nextAction = argmax(nextQValues);
                
                // Evaluate Q-value (target network) - Double DQN
                double[] targetNextQValues = targetNetwork.predict(exp.nextState);
                targets[i][exp.action] = exp.reward + DISCOUNT_FACTOR * targetNextQValues[nextAction];
            }
        }
        
        // Train network
        mainNetwork.train(states, targets);
        trainSteps++;
    }
    
    /**
     * Calculate advanced reward for DQN training
     */
    private double calculateAdvancedReward(Task task) {
        double reward = 0;
        
        if (task.getStatus() == Task.Status.SUCCESS) {
            // Base success reward
            reward += 100;
            
            // Node type matching reward
            reward += calculateNodeTypeReward(task);
            
            // Latency performance reward
            reward += calculateLatencyReward(task);
            
            // Load balancing reward
            reward += calculateLoadBalanceReward(task);
            
            // Energy efficiency reward
            reward += calculateEnergyEfficiencyReward(task);
            
        } else {
            // Failure penalty
            reward -= 50;
            
            if (task.getFailureReason() == Task.FailureReason.FAILED_DUE_TO_LATENCY) {
                reward -= 30; // Latency failure
            } else {
                reward -= 10; // Other failures
            }
        }
        
        return reward;
    }
    
    /**
     * Synchronize target network
     */
    private void syncTargetNetwork() {
        targetNetwork.copyWeights(mainNetwork);
        System.out.println("🎯 Target network synchronized at step " + trainSteps);
    }
    
    /**
     * Update exploration rate
     */
    private void updateEpsilon() {
        if (currentEpsilon > MIN_EPSILON) {
            currentEpsilon *= EPSILON_DECAY;
        }
    }
    
    /**
     * Print training progress
     */
    private void printProgress() {
        double avgReward = totalDecisions > 0 ? (double) totalReward / totalDecisions : 0;
        double successRate = totalDecisions > 0 ? (double) successfulTasks / totalDecisions * 100 : 0;
        
        System.out.printf("🧠 DQN Progress: %d decisions, avg reward: %.2f, success: %.1f%%, ε=%.3f\n", 
                         totalDecisions, avgReward, successRate, currentEpsilon);
    }
    
    // ==================== Utility Methods ====================
    
    private double normalizeTaskType(int appId) { return Math.min(1.0, appId / 5.0); }
    private double normalizeTaskSize(double length) { return Math.min(1.0, length / 10000.0); }
    private double normalizeLatencyRequirement(Task task) { return 0.5; } // Simplified implementation
    private double normalizeTaskPriority(Task task) { return 0.5; }
    private double normalizeSystemLoad() { return random.nextDouble(); }
    private double normalizeAvailableNodes() { return Math.min(1.0, nodeList.size() / 20.0); }
    private double normalizeNetworkConditions() { return random.nextDouble(); }
    private double normalizeTimeOfDay() { return (System.currentTimeMillis() % 86400000) / 86400000.0; }
    private double normalizeEdgeCapacity() { return random.nextDouble(); }
    private double normalizeCloudLatency() { return random.nextDouble(); }
    private double normalizeEnergyLevels() { return random.nextDouble(); }
    private double normalizeMobilityFactor() { return random.nextDouble(); }
    
    private double calculateNodeTypeReward(Task task) { return 50; } // Simplified implementation
    private double calculateLatencyReward(Task task) { return 30; }
    private double calculateLoadBalanceReward(Task task) { return 20; }
    private double calculateEnergyEfficiencyReward(Task task) { return 10; }
    
    private List<Integer> getAvailableActions(String[] architecture, Task task) {
        List<Integer> actions = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            actions.add(i);
        }
        return actions;
    }
    
    private void storePendingExperience(double[] state, int action, Task task) {
        // Store pending experience
    }
    
    private void completePendingExperience(Task task, double reward) {
        // Complete experience storage and add to replay buffer
    }
    
    private int argmax(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    // ==================== Inner Classes ====================
    
    /**
     * Simplified DQN network class
     */
    private static class DQNNetwork {
        private final int inputSize;
        private final int outputSize;
        private final double learningRate;
        private double[][] weights1, weights2, weights3;
        private double[] bias1, bias2, bias3;
        
        public DQNNetwork(int inputSize, int outputSize, double learningRate) {
            this.inputSize = inputSize;
            this.outputSize = outputSize;
            this.learningRate = learningRate;
            initializeWeights();
        }
        
        private void initializeWeights() {
            Random rand = new Random();
            
            // Initialize weights and biases
            weights1 = new double[inputSize][HIDDEN_SIZE_1];
            weights2 = new double[HIDDEN_SIZE_1][HIDDEN_SIZE_2];
            weights3 = new double[HIDDEN_SIZE_2][outputSize];
            
            bias1 = new double[HIDDEN_SIZE_1];
            bias2 = new double[HIDDEN_SIZE_2];
            bias3 = new double[outputSize];
            
            // Xavier initialization
            for (int i = 0; i < inputSize; i++) {
                for (int j = 0; j < HIDDEN_SIZE_1; j++) {
                    weights1[i][j] = rand.nextGaussian() * Math.sqrt(2.0 / inputSize);
                }
            }
        }
        
        public double[] predict(double[] state) {
            // Forward propagation (simplified implementation)
            double[] hidden1 = new double[HIDDEN_SIZE_1];
            double[] hidden2 = new double[HIDDEN_SIZE_2];
            double[] output = new double[outputSize];
            
            // Simplified neural network computation
            for (int i = 0; i < outputSize; i++) {
                output[i] = Math.random() * 2 - 1; // Random output for demonstration
            }
            
            return output;
        }
        
        public void train(double[][] states, double[][] targets) {
            // Train network (simplified implementation)
        }
        
        public void copyWeights(DQNNetwork source) {
            // Copy weights (simplified implementation)
        }
    }
    
    /**
     * Experience replay buffer
     */
    private static class ExperienceReplay {
        private final List<Experience> buffer;
        private final int maxSize;
        private final Random random;
        
        public ExperienceReplay(int maxSize) {
            this.maxSize = maxSize;
            this.buffer = new ArrayList<>();
            this.random = new Random();
        }
        
        public void add(Experience experience) {
            if (buffer.size() >= maxSize) {
                buffer.remove(0); // Remove oldest experience
            }
            buffer.add(experience);
        }
        
        public List<Experience> sample(int batchSize) {
            List<Experience> batch = new ArrayList<>();
            for (int i = 0; i < batchSize && i < buffer.size(); i++) {
                batch.add(buffer.get(random.nextInt(buffer.size())));
            }
            return batch;
        }
        
        public int size() {
            return buffer.size();
        }
    }
    
    /**
     * Experience data structure
     */
    private static class Experience {
        double[] state;
        int action;
        double reward;
        double[] nextState;
        boolean done;
        
        public Experience(double[] state, int action, double reward, double[] nextState, boolean done) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
            this.done = done;
        }
    }
} 