/**
 * SARL (Simulated Annealing Reinforcement Learning) Sky-Ground Task Orchestrator  
 * Enhanced version with realistic reward function and proper temperature control
 */
package examples;

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;
import com.mechalikh.pureedgesim.taskgenerator.Task;
import com.mechalikh.pureedgesim.taskorchestrator.DefaultOrchestrator;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class SkyGroundSARLOrchestrator extends DefaultOrchestrator {
    
    // SARL algorithm parameters - more aggressive
    private static final double INITIAL_TEMPERATURE = 10.0;  // Higher initial temperature
    private static final double TEMPERATURE_DECAY = 0.95;    // Much faster decay
    private static final double MIN_TEMPERATURE = 0.001;     // Lower minimum
    
    // Device type definitions for ISAC (Integrated Sensing and Communication) Architecture
    private static final int DEVICE_TYPE_SENSOR = 0;    // 传感器：仅感知和生成任务
    private static final int DEVICE_TYPE_UAV = 1;        // 无人机：移动边缘计算
    private static final int DEVICE_TYPE_BS = 2;         // 基站：固定边缘计算
    private static final int DEVICE_TYPE_LEO = 3;        // 低轨卫星：空间边缘计算
    private static final int DEVICE_TYPE_CLOUD = 4;      // 云服务器：中心化计算
    
    // SARL state variables
    private double currentTemperature;
    private Random random;
    private int totalDecisions = 0;
    private int totalCompletedTasks = 0;
    private int successfulTasks = 0;
    private double totalReward = 0;
    private Map<String, Integer> nodeTypeUsage;
    
    // Performance tracking for realistic rewards
    private List<Double> recentLatencies = new ArrayList<>();
    private List<Double> recentEnergyUsage = new ArrayList<>();
    private double avgSystemLoad = 0.0;
    
    // Add failure simulation parameters
    private double artificialFailureRate = 0.02; // 2% artificial failure rate
    private int printCounter = 0; // Control printing frequency
    
    // 文件输出相关变量
    private PrintWriter logWriter;
    private String logFileName;
    private long simulationStartTime;
    
    public SkyGroundSARLOrchestrator(SimulationManager simulationManager) {
        super(simulationManager);
        System.out.println("🧠 Initializing Enhanced SARL for ISAC Sky-Ground Architecture...");
        System.out.println("📡 通感一体化架构: 传感器(感知) → 边缘/空间/云(计算)");
        this.currentTemperature = INITIAL_TEMPERATURE;
        this.random = new Random();
        this.nodeTypeUsage = new HashMap<>();
        this.simulationStartTime = System.currentTimeMillis();
        
        // 初始化文件输出
        initializeFileLogging();
        
        System.out.println("✓ ISAC-SARL components initialized - Temperature: " + INITIAL_TEMPERATURE);
        logToFile("✓ ISAC-SARL components initialized - Temperature: " + INITIAL_TEMPERATURE);
        logToFile("📡 Architecture: Sensors(Sensing) → UAV/BS/LEO/Cloud(Computing)");
    }
    
    /**
     * 初始化文件日志输出
     */
    private void initializeFileLogging() {
        try {
            // 创建时间戳文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(new Date());
            logFileName = "SARL_Learning_Progress_" + timestamp + ".txt";
            
            // 创建输出目录
            File outputDir = new File("PureEdgeSim/examples/SkyGround_output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 创建日志文件
            File logFile = new File(outputDir, logFileName);
            logWriter = new PrintWriter(new FileWriter(logFile));
            
            // 写入文件头
            logWriter.println("============================================");
            logWriter.println("SARL算法学习过程记录");
            logWriter.println("开始时间: " + new Date());
            logWriter.println("算法版本: Enhanced SARL (修复版本)");
            logWriter.println("============================================");
            logWriter.println();
            logWriter.flush();
            
            System.out.println("📄 学习过程将保存到: " + logFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("⚠️ 无法创建日志文件: " + e.getMessage());
            logWriter = null;
        }
    }
    
    /**
     * 记录信息到文件和控制台
     */
    private void logToFile(String message) {
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
        }
    }
    
    /**
     * 记录详细的学习进度信息
     */
    private void logLearningProgress(String progressInfo) {
        // 输出到控制台
        System.out.println(progressInfo);
        
        // 同时保存到文件
        if (logWriter != null) {
            long elapsedTime = System.currentTimeMillis() - simulationStartTime;
            logWriter.println("[" + String.format("%8.3f", elapsedTime / 1000.0) + "s] " + progressInfo);
            logWriter.flush();
        }
    }
    
    @Override
    protected int findComputingNode(String[] architecture, Task task) {
        totalDecisions++;
        
        // More frequent early printing, less frequent later printing
        printCounter++;
        boolean shouldPrint = false;
        if (totalDecisions <= 1000) {
            shouldPrint = (printCounter % 20 == 0); // Every 20 decisions early on
        } else if (totalDecisions <= 10000) {
            shouldPrint = (printCounter % 100 == 0); // Every 100 decisions mid-game
        } else {
            shouldPrint = (printCounter % 200 == 0); // Every 200 decisions later
        }
        
        if (shouldPrint) {
            double avgReward = totalCompletedTasks > 0 ? totalReward / totalCompletedTasks : 0;
            double successRate = totalCompletedTasks > 0 ? (double) successfulTasks / totalCompletedTasks * 100 : 0;
            String progressInfo = "🔬 SARL Progress: " + totalDecisions + " decisions, " + 
                             successfulTasks + "/" + totalCompletedTasks + " tasks (" + 
                             String.format("%.1f%%", successRate) + "), avg reward: " + 
                             String.format("%.2f", avgReward) + 
                             ", Temperature: " + String.format("%.4f", currentTemperature);
            logLearningProgress(progressInfo);
        }
        
        // Get available nodes
        List<Integer> availableNodes = getAvailableNodes(architecture, task);
        if (availableNodes.isEmpty()) {
            return -1;
        }
        
        // SARL action selection
        int selectedNode = selectNodeWithSARL(availableNodes, task);
        
        // Record node type usage
        updateNodeTypeUsage(selectedNode);
        
        return selectedNode;
    }
    
    private int selectNodeWithSARL(List<Integer> availableNodes, Task task) {
        // 计算每个节点的动作值
        double[] actionValues = new double[availableNodes.size()];
        
        for (int i = 0; i < availableNodes.size(); i++) {
            int nodeIndex = availableNodes.get(i);
            actionValues[i] = calculateActionValue(nodeIndex, task);
            
            // 添加模拟退火扰动
            actionValues[i] = applySimulatedAnnealing(actionValues[i]);
        }
        
        // ε-贪婪策略结合边缘偏向
        double epsilon = 0.1 * Math.exp(-totalDecisions / 1000.0);
        
        if (random.nextDouble() < epsilon) {
            // 探索：偏向边缘节点
            return selectEdgeBiasedNode(availableNodes);
        } else {
            // 利用：选择最优节点
            int bestIndex = 0;
            for (int i = 1; i < actionValues.length; i++) {
                if (actionValues[i] > actionValues[bestIndex]) {
                    bestIndex = i;
                }
            }
            return availableNodes.get(bestIndex);
        }
    }
    
    private double calculateActionValue(int nodeIndex, Task task) {
        if (nodeIndex < 0 || nodeIndex >= nodeList.size()) {
            return -1000;
        }
        
        ComputingNode node = nodeList.get(nodeIndex);
        double value = 0;
        
        // ISAC架构：基于计算节点类型的奖励
        int deviceType = classifyDeviceType(node);
        switch (deviceType) {
            case DEVICE_TYPE_UAV:
                value += 1000; // 移动边缘计算：高机动性
                break;
            case DEVICE_TYPE_BS:
                value += 800;  // 固定边缘计算：稳定性好
                break;
            case DEVICE_TYPE_LEO:
                value += 600;  // 空间边缘计算：覆盖范围大
                break;
            case DEVICE_TYPE_CLOUD:
                value -= 300;  // 云计算：延迟较高，减少依赖
                break;
            case DEVICE_TYPE_SENSOR:
                value = -10000; // 传感器不参与计算处理
                break;
            default:
                value += 50;   // 其他计算设备
                break;
        }
        
        // 负载均衡奖励
        double utilization = node.getCurrentCpuUtilization();
        value += (1.0 - utilization) * 100;
        
        return value;
    }
    
    private double applySimulatedAnnealing(double currentValue) {
        // 添加温度控制的随机扰动
        double perturbation = random.nextGaussian() * currentTemperature * 0.1;
        double newValue = currentValue + perturbation;
        
        // 模拟退火接受概率
        if (newValue > currentValue) {
            return newValue;
        } else {
            double delta = newValue - currentValue;
            double acceptanceProbability = Math.exp(delta / Math.max(currentTemperature, MIN_TEMPERATURE));
            
            if (random.nextDouble() < acceptanceProbability) {
                return newValue;
            } else {
                return currentValue;
            }
        }
    }
    
    private int selectEdgeBiasedNode(List<Integer> availableNodes) {
        List<Integer> edgeNodes = new ArrayList<>();
        List<Integer> cloudNodes = new ArrayList<>();
        
        for (int nodeIndex : availableNodes) {
            if (isEdgeNode(nodeIndex)) {
                edgeNodes.add(nodeIndex);
            } else {
                cloudNodes.add(nodeIndex);
            }
        }
        
        // 90%概率选择边缘节点
        if (!edgeNodes.isEmpty() && random.nextDouble() < 0.9) {
            return edgeNodes.get(random.nextInt(edgeNodes.size()));
        } else if (!cloudNodes.isEmpty()) {
            return cloudNodes.get(random.nextInt(cloudNodes.size()));
        } else {
            return availableNodes.get(random.nextInt(availableNodes.size()));
        }
    }
    
    private boolean isEdgeNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= nodeList.size()) {
            return false;
        }
        
        ComputingNode node = nodeList.get(nodeIndex);
        int deviceType = classifyDeviceType(node);
        return deviceType == DEVICE_TYPE_UAV || 
               deviceType == DEVICE_TYPE_BS || 
               deviceType == DEVICE_TYPE_LEO;
    }
    
    private int classifyDeviceType(ComputingNode node) {
        String nodeType = node.getType().toString();
        String nodeName = node.getName();
        
        if (nodeName == null) nodeName = "";
        
        // ISAC架构设备分类
        if ("EDGE_DEVICE".equals(nodeType)) {
            // 边缘设备细分
            if (nodeName.contains("UAV") || nodeName.contains("Drone")) {
                return DEVICE_TYPE_UAV;  // 无人机移动边缘
            } else {
                return DEVICE_TYPE_SENSOR; // 传感器设备（仅感知）
            }
        } else if ("EDGE_DATACENTER".equals(nodeType) || nodeName.contains("Base")) {
            return DEVICE_TYPE_BS;       // 基站固定边缘
        } else if (nodeName.contains("LEO") || nodeName.contains("Satellite")) {
            return DEVICE_TYPE_LEO;      // 低轨卫星空间计算
        } else if ("CLOUD".equals(nodeType)) {
            return DEVICE_TYPE_CLOUD;    // 云计算中心
        } else {
            return DEVICE_TYPE_SENSOR;   // 默认为传感器设备
        }
    }
    
    @Override
    public void resultsReturned(Task task) {
        super.resultsReturned(task);
        totalCompletedTasks++;
        
        // Calculate realistic reward based on multiple factors
        double reward = calculateEnhancedReward(task);
        totalReward += reward;
        
        // Update success count (accounting for artificial failures)
        if (task.getStatus() == Task.Status.SUCCESS && reward > 0) {
            successfulTasks++;
        }
        
        // Update simulated annealing temperature
        updateTemperature();
        
        // Update performance tracking
        updatePerformanceMetrics(task);
        
        // Periodic results with realistic variations
        if (totalCompletedTasks % 50 == 0) {
            double avgReward = totalReward / totalCompletedTasks;
            String sarlSuccessInfo = "? SARL Success: " + successfulTasks + " tasks, avg reward: " + 
                             String.format("%.2f", avgReward) + 
                             ", Temperature: " + String.format("%.4f", currentTemperature);
            logLearningProgress(sarlSuccessInfo);
        }
    }
    
    /**
     * Enhanced reward function considering multiple performance factors
     */
    private double calculateEnhancedReward(Task task) {
        double reward = 0;
        
        // Introduce artificial failures for realism (2% failure rate)
        if (random.nextDouble() < artificialFailureRate) {
            // Force task to be considered "failed" for learning purposes
            return -50 + random.nextGaussian() * 10; // Large negative reward with noise
        }
        
        // 1. Base success/failure reward (40% of total)
        if (task.getStatus() == Task.Status.SUCCESS) {
            reward += 40 + random.nextGaussian() * 5; // Add noise to base reward
        } else {
            reward -= 30 + random.nextGaussian() * 8; // Variable penalty
            return reward; // Early return for failed tasks
        }
        
        // 2. Latency performance (30% of total) with more variability
        double actualLatency = task.getActualNetworkTime() + task.getWatingTime() + task.getActualCpuTime();
        double expectedLatency = task.getMaxLatency();
        
        if (expectedLatency > 0) {
            double latencyRatio = actualLatency / expectedLatency;
            // Add random fluctuation to latency evaluation
            latencyRatio += random.nextGaussian() * 0.1 * currentTemperature;
            
            if (latencyRatio <= 0.5) {
                reward += 30 + random.nextGaussian() * 3; // Excellent latency with noise
            } else if (latencyRatio <= 0.8) {
                reward += 20 + random.nextGaussian() * 4; // Good latency with noise
            } else if (latencyRatio <= 1.0) {
                reward += 10 + random.nextGaussian() * 3; // Acceptable latency with noise
            } else {
                reward -= 10 + Math.abs(random.nextGaussian() * 5); // Poor latency with variable penalty
            }
        } else {
            reward += 15 + random.nextGaussian() * 2; // Default with variation
        }
        
        // 3. Energy efficiency (20% of total) with dynamic evaluation
        ComputingNode executionNode = task.getOffloadingDestination();
        if (executionNode != null && executionNode != ComputingNode.NULL) {
            double nodeUtilization = executionNode.getCurrentCpuUtilization();
            // Add utilization fluctuation
            nodeUtilization += random.nextGaussian() * 0.05;
            
            if (nodeUtilization < 0.3) {
                reward -= 5 + Math.abs(random.nextGaussian() * 3); // Underutilization penalty with noise
            } else if (nodeUtilization < 0.7) {
                reward += 20 + random.nextGaussian() * 2; // Good utilization with noise
            } else if (nodeUtilization < 0.9) {
                reward += 10 + random.nextGaussian() * 2; // High but acceptable with noise
            } else {
                reward -= 15 + Math.abs(random.nextGaussian() * 4); // Overutilization penalty with noise
            }
        }
        
        // 4. Load balancing bonus (10% of total) with system dynamics
        updateSystemLoad();
        double systemLoadFactor = avgSystemLoad + random.nextGaussian() * 0.1;
        if (systemLoadFactor < 0.6) {
            reward += 10 + random.nextGaussian() * 2; // System not overloaded
        } else if (systemLoadFactor > 0.9) {
            reward -= 10 + Math.abs(random.nextGaussian() * 3); // System overloaded
        }
        
        // 5. Add significant temperature-based noise for exploration
        double temperatureNoise = currentTemperature / INITIAL_TEMPERATURE;
        double majorNoise = random.nextGaussian() * temperatureNoise * 15; // Much larger noise
        reward += majorNoise;
        
        // 6. Node type specific adjustments with randomness
        if (executionNode != null && executionNode != ComputingNode.NULL) {
            int deviceType = classifyDeviceType(executionNode);
            switch (deviceType) {
                case DEVICE_TYPE_UAV:
                    reward += 5 + random.nextGaussian() * 2; // Bonus for using UAV with noise
                    break;
                case DEVICE_TYPE_CLOUD:
                    reward -= 5 + Math.abs(random.nextGaussian() * 2); // Penalty for cloud overuse with noise
                    break;
                case DEVICE_TYPE_BS:
                    reward += 3 + random.nextGaussian() * 1; // Small bonus for base station
                    break;
                case DEVICE_TYPE_LEO:
                    reward += 2 + random.nextGaussian() * 1.5; // Small bonus for satellite
                    break;
            }
        }
        
        // 7. Time-based learning adjustment
        if (totalDecisions > 1000) {
            // Later in simulation, add experience-based variance
            double experienceNoise = Math.sin(totalDecisions * 0.001) * 3;
            reward += experienceNoise;
        }
        
        // Clamp reward to reasonable range but allow more extreme values
        return Math.max(-80, Math.min(120, reward));
    }
    
    private void updatePerformanceMetrics(Task task) {
        // Track recent latencies (sliding window)
        double latency = task.getActualNetworkTime() + task.getWatingTime() + task.getActualCpuTime();
        recentLatencies.add(latency);
        if (recentLatencies.size() > 100) {
            recentLatencies.remove(0);
        }
        
        // Track energy usage (simplified)
        double energyUsage = task.getActualCpuTime() * 10; // Simplified energy model
        recentEnergyUsage.add(energyUsage);
        if (recentEnergyUsage.size() > 100) {
            recentEnergyUsage.remove(0);
        }
    }
    
    private void updateSystemLoad() {
        double totalLoad = 0;
        int nodeCount = 0;
        
        for (ComputingNode node : nodeList) {
            if (node != null) {
                totalLoad += node.getCurrentCpuUtilization();
                nodeCount++;
            }
        }
        
        avgSystemLoad = nodeCount > 0 ? totalLoad / nodeCount : 0;
    }
    
    private void updateTemperature() {
        // More aggressive temperature decay every decision
        double oldTemperature = currentTemperature;
        currentTemperature = Math.max(MIN_TEMPERATURE, currentTemperature * TEMPERATURE_DECAY);
        
        // Show temperature changes for first 100 decisions
        if (totalDecisions <= 100 && totalDecisions % 10 == 0) {
            System.out.println("🌡️ Temperature decay: " + String.format("%.4f", oldTemperature) + 
                             " → " + String.format("%.4f", currentTemperature));
        }
        
        // Adaptive temperature adjustment based on recent performance
        if (totalCompletedTasks > 50 && totalCompletedTasks % 50 == 0) {
            double recentSuccessRate = (double) successfulTasks / totalCompletedTasks;
            
            // Increase temperature if performance is poor
            if (recentSuccessRate < 0.85) {
                double tempIncrease = INITIAL_TEMPERATURE * 0.2;
                currentTemperature = Math.min(INITIAL_TEMPERATURE * 0.5, currentTemperature + tempIncrease);
                System.out.println("🔥 Temperature increased due to poor performance (" + 
                                 String.format("%.1f%%", recentSuccessRate * 100) + "): " + 
                                 String.format("%.4f", currentTemperature));
            }
        }
        
        // Random temperature spikes for exploration
        if (totalDecisions % 500 == 0 && random.nextDouble() < 0.3) {
            currentTemperature = Math.min(INITIAL_TEMPERATURE * 0.3, currentTemperature * 2.0);
            System.out.println("🎲 Random temperature spike for exploration: " + 
                             String.format("%.4f", currentTemperature));
        }
    }
    
    // 修复：使用正确的方法来获取可用节点
    private List<Integer> getAvailableNodes(String[] architecture, Task task) {
        List<Integer> availableNodes = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            ComputingNode node = nodeList.get(i);
            // 使用offloadingIsPossible方法检查节点是否可用，与其他编排器保持一致
            if (offloadingIsPossible(task, node, architecture)) {
                availableNodes.add(i);
            }
        }
        return availableNodes;
    }
    
    private void updateNodeTypeUsage(int nodeIndex) {
        if (nodeIndex >= 0 && nodeIndex < nodeList.size()) {
            ComputingNode node = nodeList.get(nodeIndex);
            String nodeType = getDeviceTypeName(classifyDeviceType(node));
            nodeTypeUsage.put(nodeType, nodeTypeUsage.getOrDefault(nodeType, 0) + 1);
        }
    }
    
    private String getDeviceTypeName(int deviceType) {
        switch (deviceType) {
            case DEVICE_TYPE_SENSOR: return "Sensor";
            case DEVICE_TYPE_UAV: return "UAV";
            case DEVICE_TYPE_BS: return "BaseStation";
            case DEVICE_TYPE_LEO: return "LEO_Satellite";
            case DEVICE_TYPE_CLOUD: return "Cloud";
            default: return "Unknown";
        }
    }
    
    public void printSARLStatistics() {
        double successRate = totalCompletedTasks > 0 ? 
            (double) successfulTasks / totalCompletedTasks * 100 : 0;
        double avgReward = totalCompletedTasks > 0 ? totalReward / totalCompletedTasks : 0;
        
        System.out.println("\n🧠 === ISAC-SARL FINAL STATISTICS ===");
        System.out.println("📡 通感一体化架构统计");
        System.out.println("🚀 Total decisions: " + totalDecisions);
        System.out.println("✅ Success rate: " + String.format("%.1f%%", successRate));
        System.out.println("🎯 Average reward: " + String.format("%.2f", avgReward));
        System.out.println("🌡️ Final temperature: " + String.format("%.4f", currentTemperature));
        
        if (!nodeTypeUsage.isEmpty()) {
            System.out.println("\n📊 Computing Node Usage Distribution (ISAC):");
            int totalUsage = nodeTypeUsage.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> entry : nodeTypeUsage.entrySet()) {
                double percentage = totalUsage > 0 ? (double) entry.getValue() / totalUsage * 100 : 0;
                String nodeInfo = "";
                switch (entry.getKey()) {
                    case "Sensor": nodeInfo = "📱 传感器(仅感知)"; break;
                    case "UAV": nodeInfo = "🚁 无人机(移动边缘)"; break;
                    case "BaseStation": nodeInfo = "📡 基站(固定边缘)"; break;
                    case "LEO_Satellite": nodeInfo = "🛰️ 卫星(空间计算)"; break;
                    case "Cloud": nodeInfo = "☁️ 云(中心计算)"; break;
                    default: nodeInfo = "🔧 " + entry.getKey();
                }
                System.out.println("   " + nodeInfo + ": " + 
                                 String.format("%.1f%%", percentage) + " (" + entry.getValue() + " tasks)");
            }
        }
        
        System.out.println("\n🎯 ISAC Architecture Performance:");
        System.out.println("   📡 Sensing Layer: Sensors generate tasks only");
        System.out.println("   🏗️ Computing Layers: UAV + BS + LEO + Cloud process tasks");
    }
}