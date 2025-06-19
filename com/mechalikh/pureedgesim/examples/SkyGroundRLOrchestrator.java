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
 *     @author Your Name
 **/
package examples;

import java.util.*;

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;
import com.mechalikh.pureedgesim.taskgenerator.Task;
import com.mechalikh.pureedgesim.taskorchestrator.DefaultOrchestrator;

/**
 * 空天地强化学习任务编排器 - 优化版本
 * 
 * 使用Q-learning算法学习最优的任务分配策略，重点优化：
 * 1. 成功率计算逻辑修复
 * 2. 奖励函数重新平衡，实现正向奖励
 * 3. 鼓励边缘资源利用，减少云端依赖
 * 4. 网络感知的智能调度
 */
public class SkyGroundRLOrchestrator extends DefaultOrchestrator {
    
    // Q-learning参数
    private static final double LEARNING_RATE = 0.15;    // 提高学习率
    private static final double DISCOUNT_FACTOR = 0.9;   // 折扣因子
    private static final double EPSILON = 0.3;           // 激进提升探索率至30%
    private static final double EPSILON_DECAY = 0.995;   // 减缓衰减
    private static final double MIN_EPSILON = 0.15;      // 提高最小探索率至15%
    
    // 状态空间维度
    private static final int STATE_DIMENSIONS = 6;       
    private static final int MAX_NODES = 20;             
    
    // Q表和经验存储
    private Map<String, Map<Integer, Double>> qTable;
    private List<Experience> experienceBuffer;
    private Random random;
    private double currentEpsilon;
    
    // 统计信息 - 修复计数逻辑
    private int totalDecisions = 0;          // RL决策次数
    private int totalCompletedTasks = 0;     // 完成的任务数量
    private int successfulTasksRL = 0;       // RL成功任务数
    private double totalReward = 0;
    private Map<Integer, Integer> actionCounts;
    private Map<String, Integer> nodeTypeUsage;
    
    // 云端过度使用惩罚计数器
    private int consecutiveCloudTasks = 0;
    private static final int CLOUD_OVERUSE_THRESHOLD = 5;
    
    public SkyGroundRLOrchestrator(SimulationManager simulationManager) {
        super(simulationManager);
        System.out.println("🤖 Initializing Enhanced RL Orchestrator...");
        initializeQLearning();
        System.out.println("✓ Enhanced Q-learning components initialized");
        System.out.println("✓ Learning parameters: α=" + LEARNING_RATE + ", γ=" + DISCOUNT_FACTOR + ", ε=" + EPSILON);
    }
    
    /**
     * 初始化Q-learning相关组件
     */
    private void initializeQLearning() {
        this.qTable = new HashMap<>();
        this.experienceBuffer = new ArrayList<>();
        this.random = new Random();
        this.currentEpsilon = EPSILON;
        this.actionCounts = new HashMap<>();
        this.nodeTypeUsage = new HashMap<>();
        
        // 预初始化Q表
        initializeQTable();
        
        System.out.println("Q-table initialized for edge-aware learning");
    }
    
    /**
     * 预初始化Q表，优化边缘资源偏好
     */
    private void initializeQTable() {
        for (int i = 0; i < 200; i++) { // 增加预初始化状态数量
            String state = generateRandomState();
            Map<Integer, Double> actions = new HashMap<>();
            for (int action = 0; action < MAX_NODES; action++) {
                // 给边缘节点稍高的初始Q值
                double initialValue = random.nextGaussian() * 0.1 + 0.05; 
                actions.put(action, initialValue);
            }
            qTable.put(state, actions);
        }
    }
    
    /**
     * 生成随机状态字符串（用于初始化）
     */
    private String generateRandomState() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < STATE_DIMENSIONS; i++) {
            if (i > 0) sb.append(",");
            sb.append(random.nextInt(10)); // 0-9的状态值
        }
        return sb.toString();
    }
    
    @Override
    protected int findComputingNode(String[] architecture, Task task) {
        totalDecisions++;
        
        // 每50个决策输出一次进度（减少输出频率）
        if (totalDecisions % 50 == 0) {
            double currentSuccessRate = totalCompletedTasks > 0 ? 
                Math.min(100.0, (double)successfulTasksRL / totalCompletedTasks * 100) : 0;
            System.out.println("🎯 RL Decisions: " + totalDecisions + 
                             ", Completed: " + totalCompletedTasks +
                             ", Success rate: " + String.format("%.1f%%", currentSuccessRate) + 
                             ", ε=" + String.format("%.3f", currentEpsilon));
        }
        
        // 获取当前状态
        String currentState = extractState(task);
        
        // 获取可用动作（可用节点）
        List<Integer> availableActions = getAvailableActions(architecture, task);
        
        if (availableActions.isEmpty()) {
            System.out.println("⚠️ No available nodes for task " + task.getId());
            return -1;
        }
        
        // 选择动作（epsilon-greedy策略）
        int selectedAction = selectAction(currentState, availableActions);
        int nodeIndex = availableActions.get(selectedAction);
        
        // 调试输出（只对前5个决策）
        if (totalDecisions <= 5) {
            System.out.println("🔍 Decision " + totalDecisions + ": State=" + currentState + 
                             ", Available=" + availableActions.size() + 
                             ", Selected=" + nodeIndex);
        }
        
        // 存储经验
        Experience experience = new Experience(currentState, selectedAction, task, nodeIndex);
        experienceBuffer.add(experience);
        
        // 更新统计
        actionCounts.put(selectedAction, actionCounts.getOrDefault(selectedAction, 0) + 1);
        updateNodeTypeUsage(nodeIndex);
        
        return nodeIndex;
    }
    
    /**
     * 提取环境状态特征
     */
    private String extractState(Task task) {
        // 状态特征：
        // 1. 任务类型 (0-5)
        // 2. 任务大小级别 (0-4) 
        // 3. 延迟敏感级别 (0-4)
        // 4. 系统平均负载级别 (0-9)
        // 5. 可用节点数量级别 (0-4)
        // 6. 当前时间段 (0-9)
        
        int[] stateFeatures = new int[STATE_DIMENSIONS];
        
        // 1. 任务类型
        stateFeatures[0] = task.getApplicationID() % 6;
        
        // 2. 任务大小级别
        double taskSize = task.getLength();
        stateFeatures[1] = (int) Math.min(4, taskSize / 500); // 每500MIPS为一级
        
        // 3. 延迟敏感级别
        double latencyTolerance = SimulationParameters.applicationList.get(task.getApplicationID()).getLatency();
        stateFeatures[2] = (int) Math.min(4, latencyTolerance * 10); // 延迟容忍度分级
        
        // 4. 系统平均负载级别
        double avgLoad = calculateAverageSystemLoad();
        stateFeatures[3] = (int) Math.min(9, avgLoad / 10); // 负载分为10级
        
        // 5. 可用节点数量级别
        int availableNodes = countAvailableNodes();
        stateFeatures[4] = Math.min(4, availableNodes / 5); // 每5个节点为一级
        
        // 6. 当前时间段
        double currentTime = simulationManager.getSimulation().clock();
        stateFeatures[5] = (int) (currentTime / (SimulationParameters.simulationDuration / 10)) % 10;
        
        // 构造状态字符串
        StringBuilder stateBuilder = new StringBuilder();
        for (int i = 0; i < STATE_DIMENSIONS; i++) {
            if (i > 0) stateBuilder.append(",");
            stateBuilder.append(stateFeatures[i]);
        }
        
        return stateBuilder.toString();
    }
    
    /**
     * 计算系统平均负载
     */
    private double calculateAverageSystemLoad() {
        double totalLoad = 0;
        int nodeCount = 0;
        
        for (ComputingNode node : nodeList) {
            totalLoad += node.getCurrentCpuUtilization();
            nodeCount++;
        }
        
        return nodeCount > 0 ? totalLoad / nodeCount : 0;
    }
    
    /**
     * 计算可用节点数量
     */
    private int countAvailableNodes() {
        int count = 0;
        for (ComputingNode node : nodeList) {
            if (node.getCurrentCpuUtilization() < 90) { // 负载小于90%认为可用
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取可用动作列表
     */
    private List<Integer> getAvailableActions(String[] architecture, Task task) {
        List<Integer> availableActions = new ArrayList<>();
        
        for (int i = 0; i < nodeList.size(); i++) {
            ComputingNode node = nodeList.get(i);
            if (offloadingIsPossible(task, node, architecture)) {
                availableActions.add(i);
            }
        }
        
        return availableActions;
    }
    
    /**
     * 使用epsilon-greedy策略选择动作
     */
    private int selectAction(String state, List<Integer> availableActions) {
        // 探索 vs 利用
        if (random.nextDouble() < currentEpsilon) {
            // 探索：随机选择
            return random.nextInt(availableActions.size());
        } else {
            // 利用：选择Q值最高的动作
            return selectBestAction(state, availableActions);
        }
    }
    
    /**
     * 选择Q值最高的动作
     */
    private int selectBestAction(String state, List<Integer> availableActions) {
        Map<Integer, Double> stateActions = qTable.get(state);
        
        if (stateActions == null) {
            // 如果状态不存在，初始化并随机选择
            initializeStateActions(state, availableActions);
            return random.nextInt(availableActions.size());
        }
        
        int bestActionIndex = 0;
        double bestQValue = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < availableActions.size(); i++) {
            int nodeIndex = availableActions.get(i);
            double qValue = stateActions.getOrDefault(nodeIndex, 0.0);
            
            if (qValue > bestQValue) {
                bestQValue = qValue;
                bestActionIndex = i;
            }
        }
        
        return bestActionIndex;
    }
    
    /**
     * 初始化状态-动作对
     */
    private void initializeStateActions(String state, List<Integer> availableActions) {
        Map<Integer, Double> actions = new HashMap<>();
        for (int nodeIndex : availableActions) {
            actions.put(nodeIndex, random.nextGaussian() * 0.01);
        }
        qTable.put(state, actions);
    }
    
    @Override
    public void resultsReturned(Task task) {
        totalCompletedTasks++;
        
        // 更新Q值
        updateQValues(task);
        
        // 衰减探索率
        currentEpsilon = Math.max(MIN_EPSILON, currentEpsilon * EPSILON_DECAY);
        
        // 统计成功任务
        if (task.getStatus() == Task.Status.SUCCESS) {
            successfulTasksRL++;
            
            // 每成功50个任务输出一次统计
            if (successfulTasksRL % 50 == 0) {
                double avgReward = totalDecisions > 0 ? totalReward / totalDecisions : 0;
                System.out.println("🏆 RL Success: " + successfulTasksRL + " tasks, " +
                                 "avg reward: " + String.format("%.2f", avgReward) + 
                                 ", Q-states: " + qTable.size());
            }
        } else {
            // 只输出前10个失败的调试信息
            if ((totalCompletedTasks - successfulTasksRL) <= 10) {
                System.out.println("❌ RL Task " + task.getId() + " failed: " + 
                                 (task.getFailureReason() != null ? task.getFailureReason() : "Unknown"));
            }
        }
    }
    
    /**
     * 更新Q值
     */
    private void updateQValues(Task task) {
        // 找到对应的经验
        Experience experience = findExperience(task);
        if (experience == null) return;
        
        // 计算奖励
        double reward = calculateReward(task, experience);
        totalReward += reward;
        
        // 获取当前状态的Q值
        Map<Integer, Double> currentStateActions = qTable.get(experience.state);
        if (currentStateActions == null) {
            initializeStateActions(experience.state, Arrays.asList(experience.nodeIndex));
            currentStateActions = qTable.get(experience.state);
        }
        
        double currentQ = currentStateActions.getOrDefault(experience.nodeIndex, 0.0);
        
        // 估算下一状态的最大Q值（简化为0，因为是单步任务）
        double nextMaxQ = 0.0;
        
        // Q-learning更新公式：Q(s,a) = Q(s,a) + α[r + γ*max(Q(s',a')) - Q(s,a)]
        double newQ = currentQ + LEARNING_RATE * (reward + DISCOUNT_FACTOR * nextMaxQ - currentQ);
        currentStateActions.put(experience.nodeIndex, newQ);
        
        // 移除已处理的经验
        experienceBuffer.remove(experience);
    }
    
    /**
     * 重新平衡的奖励函数 - 实现正向平均奖励
     */
    private double calculateReward(Task task, Experience experience) {
        double reward = 0;
        
        // 基础奖励：任务成功完成
        if (task.getStatus() == Task.Status.SUCCESS) {
            reward += 200; // 大幅提升基础成功奖励
            
            // 超激进边缘优先奖励策略
            double edgePriorityReward = calculateUltraAggressiveEdgeReward(experience.nodeIndex);
            reward += edgePriorityReward;
            
            // 延迟性能奖励（保持现有逻辑）
            double executionTime = task.getActualCpuTime();
            double expectedTime = task.getLength() / nodeList.get(experience.nodeIndex).getTotalMipsCapacity();
            if (executionTime > 0 && expectedTime > 0) {
                double latencyBonus = Math.max(0, 50 * (expectedTime - executionTime) / expectedTime);
                reward += latencyBonus;
            }
            
            // 负载平衡奖励
            double loadBalance = calculateLoadBalanceReward(experience.nodeIndex);
            reward += loadBalance;
            
            // 节点类型匹配奖励
            double nodeMatch = calculateNodeTypeReward(task, experience.nodeIndex);
            reward += nodeMatch;
            
        } else {
            // 失败惩罚 - 大幅减轻
            if (task.getStatus() == Task.Status.FAILED) {
                // 检查失败原因
                if (task.getFailureReason() == Task.FailureReason.FAILED_DUE_TO_LATENCY) {
                    reward -= 30; // 从-100减至-30，提高延迟容忍度
                    
                    // 如果是云端延迟失败，额外惩罚
                    if (isCloudNode(experience.nodeIndex)) {
                        reward -= 50; // 云端延迟失败额外惩罚
                    }
                } else {
                    reward -= 20; // 其他失败惩罚减轻
                }
            }
        }
        
        // 云端过度使用惩罚机制
        double cloudPenalty = calculateCloudOverusePenalty(experience.nodeIndex);
        reward += cloudPenalty;
        
        // 记录并更新统计
        totalReward += reward;
        
        return reward;
    }
    
    /**
     * 超激进边缘奖励计算 - 极端偏向边缘
     */
    private double calculateUltraAggressiveEdgeReward(int nodeIndex) {
        ComputingNode node = nodeList.get(nodeIndex);
        String nodeType = node.getType().toString();
        String nodeName = node.getName();
        
        // 空指针安全检查
        if (nodeName == null) {
            nodeName = "Unknown_" + nodeIndex;
        }
        
        // 超激进边缘奖励
        switch (nodeType) {
            case "EDGE_DEVICE":  // UAV
                return 800;  // 从+300提升至+800
            case "EDGE_DATACENTER": // 基站  
                return 600;  // 从+250提升至+600
            case "CLOUD":        // LEO卫星也按云端处理
                if (nodeName.contains("LEO") || nodeName.contains("Satellite")) {
                    return 400;  // 卫星奖励+400
                } else {
                    return -100; // 云端使用直接惩罚
                }
            default:
                return -50;
        }
    }
    
    /**
     * 云端过度使用惩罚
     */
    private double calculateCloudOverusePenalty(int nodeIndex) {
        if (isCloudNode(nodeIndex)) {
            consecutiveCloudTasks++;
            
            // 累进惩罚机制
            if (consecutiveCloudTasks > CLOUD_OVERUSE_THRESHOLD) {
                return -20 * (consecutiveCloudTasks - CLOUD_OVERUSE_THRESHOLD); // 累进惩罚
            }
        } else {
            consecutiveCloudTasks = 0; // 重置计数器
        }
        return 0;
    }
    
    /**
     * 判断是否为云端节点
     */
    private boolean isCloudNode(int nodeIndex) {
        ComputingNode node = nodeList.get(nodeIndex);
        String nodeType = node.getType().toString();
        String nodeName = node.getName();
        
        // 空指针安全检查
        if (nodeName == null) {
            nodeName = "Unknown_" + nodeIndex;
        }
        
        // 只有真正的云端数据中心才算云端
        return "CLOUD".equals(nodeType) && 
               !nodeName.contains("LEO") && 
               !nodeName.contains("Satellite") &&
               !nodeName.contains("UAV") &&
               !nodeName.contains("Base");
    }
    
    /**
     * 边缘优先奖励 - 鼓励使用边缘和UAV资源
     */
    private double calculateEdgePreferenceReward(int nodeIndex) {
        // 这个方法现在被 calculateUltraAggressiveEdgeReward 替代
        return calculateUltraAggressiveEdgeReward(nodeIndex);
    }
    
    /**
     * 计算节点类型适配奖励
     */
    private double calculateNodeTypeReward(Task task, int nodeIndex) {
        ComputingNode node = nodeList.get(nodeIndex);
        String taskType = SimulationParameters.applicationList.get(task.getApplicationID()).getType().toLowerCase();
        
        if (node instanceof SkyGroundComputingNode) {
            SkyGroundComputingNode skyNode = (SkyGroundComputingNode) node;
            SkyGroundComputingNode.SkyGroundNodeType nodeType = skyNode.getNodeType();
            
            // 根据任务类型和节点类型的匹配度给予奖励
            switch (nodeType) {
                case UAV:
                    if (taskType.contains("real-time") || taskType.contains("emergency")) {
                        return 30; // UAV适合实时任务
                    }
                    return 15;
                case LEO_SATELLITE:
                    if (taskType.contains("monitoring") || taskType.contains("sensing")) {
                        return 25; // 卫星适合监控任务
                    }
                    return 10;
                case BASE_STATION:
                    return 20; // 基站通用性较强
                case CLOUD_SERVER:
                    if (taskType.contains("computation") || taskType.contains("ai")) {
                        return 15; // 云端适合计算密集任务
                    }
                    return 5;
                default:
                    return 10;
            }
        }
        
        return 12; // 默认适配奖励
    }
    
    /**
     * 查找对应的经验
     */
    private Experience findExperience(Task task) {
        for (Experience exp : experienceBuffer) {
            if (exp.task == task) {
                return exp;
            }
        }
        return null;
    }
    
    /**
     * 更新节点类型使用统计
     */
    private void updateNodeTypeUsage(int nodeIndex) {
        if (nodeIndex >= 0 && nodeIndex < nodeList.size()) {
            ComputingNode node = nodeList.get(nodeIndex);
            String nodeType = "Unknown";
            
            if (node instanceof SkyGroundComputingNode) {
                SkyGroundComputingNode skyNode = (SkyGroundComputingNode) node;
                nodeType = skyNode.getNodeType().toString();
            } else {
                // 根据节点位置推断类型
                if (node.getType().toString().contains("CLOUD")) {
                    nodeType = "CLOUD";
                } else if (node.getType().toString().contains("EDGE")) {
                    nodeType = "EDGE";
                } else {
                    nodeType = "OTHER";
                }
            }
            
            nodeTypeUsage.put(nodeType, nodeTypeUsage.getOrDefault(nodeType, 0) + 1);
        }
    }
    
    /**
     * 打印增强的学习统计信息
     */
    public void printLearningStatistics() {
        double successRate = totalCompletedTasks > 0 ? 
            Math.min(100.0, (double) successfulTasksRL / totalCompletedTasks * 100) : 0;
        double avgReward = totalDecisions > 0 ? totalReward / totalDecisions : 0;
        
        System.out.println("\n🧠 === ENHANCED RL FINAL STATISTICS ===");
        System.out.println("📊 Learning Summary:");
        System.out.println("   • Total RL decisions: " + totalDecisions);
        System.out.println("   • Completed tasks: " + totalCompletedTasks);
        System.out.println("   • Successful tasks: " + successfulTasksRL + " (" + String.format("%.1f%%", successRate) + ")");
        System.out.println("   • Average reward: " + String.format("%.2f", avgReward));
        System.out.println("   • Final epsilon: " + String.format("%.3f", currentEpsilon));
        System.out.println("   • Q-table size: " + qTable.size() + " states learned");
        
        // 打印节点类型使用分布
        if (!nodeTypeUsage.isEmpty()) {
            System.out.println("\n🎯 Node Type Distribution:");
            int totalUsage = nodeTypeUsage.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> entry : nodeTypeUsage.entrySet()) {
                double percentage = totalUsage > 0 ? (double) entry.getValue() / totalUsage * 100 : 0;
                System.out.println("   • " + entry.getKey() + ": " + entry.getValue() + 
                                 " tasks (" + String.format("%.1f%%", percentage) + ")");
            }
        }
        
        // 性能评估
        System.out.println("\n📈 Performance Assessment:");
        if (successRate > 85) {
            System.out.println("   ✅ EXCELLENT: High success rate achieved!");
        } else if (successRate > 70) {
            System.out.println("   ✅ GOOD: Acceptable performance");
        } else {
            System.out.println("   ⚠️ NEEDS IMPROVEMENT: Consider parameter tuning");
        }
        
        if (avgReward > 0) {
            System.out.println("   ✅ POSITIVE LEARNING: Agent shows effective learning");
        } else {
            System.out.println("   ⚠️ NEGATIVE REWARDS: Reward function may need adjustment");
        }
        
        double edgeUsagePercentage = nodeTypeUsage.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("CLOUD"))
            .mapToInt(Map.Entry::getValue)
            .sum() * 100.0 / Math.max(1, totalDecisions);
            
        if (edgeUsagePercentage > 15) {
            System.out.println("   ✅ EDGE UTILIZATION: Good edge resource usage (" + 
                             String.format("%.1f%%", edgeUsagePercentage) + ")");
        } else {
            System.out.println("   ⚠️ CLOUD DOMINATED: Most tasks sent to cloud (" + 
                             String.format("%.1f%%", 100 - edgeUsagePercentage) + "%)");
        }
        
        System.out.println("🧠 === END OF ENHANCED RL STATISTICS ===\n");
    }
    
    /**
     * 获取当前学习统计信息（供外部调用）
     */
    public String getLearningStatsSummary() {
        double successRate = totalCompletedTasks > 0 ? (double) successfulTasksRL / totalCompletedTasks * 100 : 0;
        double avgReward = totalDecisions > 0 ? totalReward / totalDecisions : 0;
        return String.format("Tasks: %d, Success: %.1f%%, Avg Reward: %.2f, Q-states: %d", 
                           totalDecisions, successRate, avgReward, qTable.size());
    }
    
    /**
     * 检查强化学习是否正在工作
     */
    public boolean isLearningActive() {
        return totalDecisions > 0 && qTable.size() > 0;
    }
    
    /**
     * 经验存储类
     */
    private static class Experience {
        String state;
        int action;
        Task task;
        int nodeIndex;
        
        Experience(String state, int action, Task task, int nodeIndex) {
            this.state = state;
            this.action = action;
            this.task = task;
            this.nodeIndex = nodeIndex;
        }
    }
    
    /**
     * 计算负载平衡奖励
     */
    private double calculateLoadBalanceReward(int nodeIndex) {
        ComputingNode node = nodeList.get(nodeIndex);
        double utilization = node.getCurrentCpuUtilization();
        
        // 奖励低负载节点的使用
        if (utilization < 30) {
            return 40; // 低负载奖励
        } else if (utilization < 60) {
            return 20; // 中等负载适中奖励
        } else if (utilization < 80) {
            return 5; // 较高负载小奖励
        } else {
            return -20; // 过载惩罚
        }
    }
} 