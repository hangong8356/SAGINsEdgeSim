 /**
 * 真实SARL (Realistic Simulated Annealing Reinforcement Learning) 天空-地面任务编排器
 * 增强版本，包含真实世界的不确定性、网络变化、设备故障和资源约束
 */
package examples;

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;
import com.mechalikh.pureedgesim.taskgenerator.Task;
import com.mechalikh.pureedgesim.taskorchestrator.DefaultOrchestrator;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class SkyGroundRealisticSARLOrchestrator extends DefaultOrchestrator {
    
    // SARL算法参数 - 更保守的设置
    private static final double INITIAL_TEMPERATURE = 5.0;   // 降低初始温度
    private static final double TEMPERATURE_DECAY = 0.998;   // 更慢的衰减
    private static final double MIN_TEMPERATURE = 0.1;       // 更高的最小温度
    
    // 真实世界约束参数
    private static final double REAL_FAILURE_RATE = 0.15;    // 15%真实故障率
    private static final double NETWORK_CONGESTION_FACTOR = 0.2; // 网络拥塞影响
    private static final double DEVICE_OVERLOAD_THRESHOLD = 0.8; // 设备过载阈值
    private static final double ENERGY_DEPLETION_RATE = 0.1;     // 能量消耗率
    
    // 设备类型定义
    private static final int DEVICE_TYPE_SENSOR = 0;
    private static final int DEVICE_TYPE_UAV = 1;
    private static final int DEVICE_TYPE_BS = 2;
    private static final int DEVICE_TYPE_LEO = 3;
    private static final int DEVICE_TYPE_CLOUD = 4;
    
    // SARL状态变量
    private double currentTemperature;
    private Random random;
    private int totalDecisions = 0;
    private int totalCompletedTasks = 0;
    private int successfulTasks = 0;
    private int failedTasks = 0;
    private double totalReward = 0;
    private Map<String, Integer> nodeTypeUsage;
    private Map<String, List<Double>> performanceHistory;
    
    // 真实性能追踪
    private List<Double> recentLatencies = new ArrayList<>();
    private List<Double> recentEnergyUsage = new ArrayList<>();
    private List<Double> networkCongestionHistory = new ArrayList<>();
    private Map<Integer, Double> nodeLoadHistory = new HashMap<>();
    private Map<Integer, Double> nodeEnergyHistory = new HashMap<>();
    
    // 动态环境状态
    private double currentNetworkCongestion = 0.0;
    private Map<Integer, Boolean> nodeAvailability = new HashMap<>();
    private Map<Integer, Double> nodeReliability = new HashMap<>();
    
    // 文件输出
    private PrintWriter logWriter;
    private PrintWriter performanceWriter;
    private String logFileName;
    private long simulationStartTime;
    
    public SkyGroundRealisticSARLOrchestrator(SimulationManager simulationManager) {
        super(simulationManager);
        System.out.println("🌍 初始化真实SARL天空-地面架构...");
        System.out.println("📡 Realistic ISAC Architecture: Enhanced uncertainty modeling");
        
        this.currentTemperature = INITIAL_TEMPERATURE;
        this.random = new Random();
        this.nodeTypeUsage = new HashMap<>();
        this.performanceHistory = new HashMap<>();
        this.simulationStartTime = System.currentTimeMillis();
        
        // 初始化性能历史记录
        performanceHistory.put("latency", new ArrayList<>());
        performanceHistory.put("energy", new ArrayList<>());
        performanceHistory.put("success_rate", new ArrayList<>());
        performanceHistory.put("network_congestion", new ArrayList<>());
        
        initializeFileLogging();
        initializeNodeReliability();
        
        System.out.println("✓ 真实SARL组件初始化完成 - 初始温度: " + INITIAL_TEMPERATURE);
        logToFile("✓ Realistic SARL components initialized - Temperature: " + INITIAL_TEMPERATURE);
        logToFile("🌍 Enhanced with: Network variation, Device failures, Resource constraints");
    }
    
    private void initializeFileLogging() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(new Date());
            logFileName = "Realistic_SARL_Progress_" + timestamp + ".txt";
            
            File outputDir = new File("PureEdgeSim/examples/SkyGround_output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 主日志文件
            File logFile = new File(outputDir, logFileName);
            logWriter = new PrintWriter(new FileWriter(logFile));
            
            // 性能分析文件
            File perfFile = new File(outputDir, "Realistic_Performance_" + timestamp + ".csv");
            performanceWriter = new PrintWriter(new FileWriter(perfFile));
            performanceWriter.println("Time,Temperature,Success_Rate,Avg_Latency,Network_Congestion,Failed_Tasks,Reward");
            
            logWriter.println("============================================");
            logWriter.println("真实SARL算法学习过程记录 (Realistic SARL Learning)");
            logWriter.println("开始时间: " + new Date());
            logWriter.println("算法版本: Realistic Enhanced SARL with Uncertainty");
            logWriter.println("真实性增强: 网络变化、设备故障、资源约束");
            logWriter.println("============================================");
            logWriter.println();
            logWriter.flush();
            
            System.out.println("📄 详细学习过程保存到: " + logFile.getAbsolutePath());
            System.out.println("📊 性能数据保存到: " + perfFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("⚠️ 无法创建日志文件: " + e.getMessage());
            logWriter = null;
            performanceWriter = null;
        }
    }
    
    private void initializeNodeReliability() {
        for (int i = 0; i < 100; i++) {
            // 初始化节点可靠性 (60%-95%范围)
            double reliability = 0.6 + random.nextDouble() * 0.35;
            nodeReliability.put(i, reliability);
            nodeAvailability.put(i, true);
            nodeLoadHistory.put(i, 0.0);
            nodeEnergyHistory.put(i, 100.0); // 100%能量
        }
    }
    
    private void logToFile(String message) {
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
        }
    }
    
    private void logLearningProgress(String progressInfo) {
        System.out.println(progressInfo);
        if (logWriter != null) {
            long elapsedTime = System.currentTimeMillis() - simulationStartTime;
            logWriter.println("[" + String.format("%8.3f", elapsedTime / 1000.0) + "s] " + progressInfo);
            logWriter.flush();
        }
    }
    
    @Override
    protected int findComputingNode(String[] architecture, Task task) {
        totalDecisions++;
        
        // 更新动态环境状态
        updateEnvironmentState();
        
        // 定期打印进度 (更频繁的早期反馈)
        if (shouldPrintProgress()) {
            printProgressUpdate();
        }
        
        // 获取可用节点 (考虑真实约束)
        List<Integer> availableNodes = getRealisticallyAvailableNodes(architecture, task);
        if (availableNodes.isEmpty()) {
            logFailure("No available nodes due to realistic constraints");
            return -1;
        }
        
        // 真实SARL动作选择
        int selectedNode = selectNodeWithRealisticSARL(availableNodes, task);
        
        // 记录节点使用情况
        updateNodeTypeUsage(selectedNode);
        updateNodeLoad(selectedNode);
        
        return selectedNode;
    }
    
    private void updateEnvironmentState() {
        // 模拟网络拥塞变化
        currentNetworkCongestion = Math.max(0, Math.min(1, 
            currentNetworkCongestion + (random.nextGaussian() * 0.1)));
        networkCongestionHistory.add(currentNetworkCongestion);
        
        // 模拟设备可用性变化
        for (Integer nodeId : nodeAvailability.keySet()) {
            if (random.nextDouble() < 0.001) { // 0.1%概率状态变化
                nodeAvailability.put(nodeId, !nodeAvailability.get(nodeId));
            }
            
            // 能量消耗模拟
            double currentEnergy = nodeEnergyHistory.getOrDefault(nodeId, 100.0);
            double energyConsumption = random.nextDouble() * ENERGY_DEPLETION_RATE;
            nodeEnergyHistory.put(nodeId, Math.max(0, currentEnergy - energyConsumption));
        }
    }
    
    private boolean shouldPrintProgress() {
        if (totalDecisions <= 100) return totalDecisions % 10 == 0;
        if (totalDecisions <= 1000) return totalDecisions % 50 == 0;
        if (totalDecisions <= 10000) return totalDecisions % 200 == 0;
        return totalDecisions % 500 == 0;
    }
    
    private void printProgressUpdate() {
        double successRate = totalCompletedTasks > 0 ? 
            (double) successfulTasks / totalCompletedTasks * 100 : 0;
        double failureRate = totalCompletedTasks > 0 ? 
            (double) failedTasks / totalCompletedTasks * 100 : 0;
        double avgReward = totalCompletedTasks > 0 ? totalReward / totalCompletedTasks : 0;
        
        String progressInfo = String.format(
            "🌍 Realistic SARL Progress: %d decisions, %d/%d tasks (✅%.1f%% ❌%.1f%%), " +
            "avg reward: %.2f, temp: %.4f, congestion: %.2f",
            totalDecisions, successfulTasks, totalCompletedTasks, 
            successRate, failureRate, avgReward, currentTemperature, currentNetworkCongestion);
        
        logLearningProgress(progressInfo);
        
        // 记录到CSV
        if (performanceWriter != null) {
            double avgLatency = recentLatencies.isEmpty() ? 0 : 
                recentLatencies.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            performanceWriter.printf("%.3f,%.4f,%.2f,%.4f,%.3f,%d,%.2f\n",
                (System.currentTimeMillis() - simulationStartTime) / 1000.0,
                currentTemperature, successRate, avgLatency, 
                currentNetworkCongestion, failedTasks, avgReward);
            performanceWriter.flush();
        }
    }
    
    private List<Integer> getRealisticallyAvailableNodes(String[] architecture, Task task) {
        List<Integer> availableNodes = new ArrayList<>();
        
        for (int i = 0; i < nodeList.size(); i++) {
            ComputingNode node = nodeList.get(i);
            
            // 基本可用性检查
            if (!offloadingIsPossible(task, node, architecture)) {
                continue;
            }
            
            // 真实约束检查
            if (!isNodeReallyAvailable(i, node)) {
                continue;
            }
            
            availableNodes.add(i);
        }
        
        return availableNodes;
    }
    
    private boolean isNodeReallyAvailable(int nodeIndex, ComputingNode node) {
        // 检查节点是否在线
        if (!nodeAvailability.getOrDefault(nodeIndex, true)) {
            return false;
        }
        
        // 检查能量水平
        double energy = nodeEnergyHistory.getOrDefault(nodeIndex, 100.0);
        if (energy < 5.0) { // 5%以下能量不可用
            return false;
        }
        
        // 检查负载
        double load = nodeLoadHistory.getOrDefault(nodeIndex, 0.0);
        if (load > DEVICE_OVERLOAD_THRESHOLD) {
            return false;
        }
        
        // 检查可靠性
        double reliability = nodeReliability.getOrDefault(nodeIndex, 1.0);
        if (random.nextDouble() > reliability) {
            return false;
        }
        
        return true;
    }
    
    private int selectNodeWithRealisticSARL(List<Integer> availableNodes, Task task) {
        double[] actionValues = new double[availableNodes.size()];
        
        for (int i = 0; i < availableNodes.size(); i++) {
            int nodeIndex = availableNodes.get(i);
            actionValues[i] = calculateRealisticActionValue(nodeIndex, task);
            actionValues[i] = applySimulatedAnnealingWithNoise(actionValues[i]);
        }
        
        // ε-贪婪策略，epsilon随温度和性能动态调整
        double epsilon = Math.min(0.3, 0.1 + currentTemperature / INITIAL_TEMPERATURE);
        
        // 根据最近性能调整探索率
        double recentSuccessRate = getRecentSuccessRate();
        if (recentSuccessRate < 0.7) {
            epsilon *= 1.5; // 性能差时增加探索
        }
        
        if (random.nextDouble() < epsilon) {
            return selectExplorationNode(availableNodes);
        } else {
            int bestIndex = getBestActionIndex(actionValues);
            return availableNodes.get(bestIndex);
        }
    }
    
    private double calculateRealisticActionValue(int nodeIndex, Task task) {
        if (nodeIndex < 0 || nodeIndex >= nodeList.size()) {
            return -1000;
        }
        
        ComputingNode node = nodeList.get(nodeIndex);
        double value = 0;
        
        // 基础设备类型奖励 (降低以增加真实性)
        int deviceType = classifyDeviceType(node);
        switch (deviceType) {
            case DEVICE_TYPE_UAV:
                value += 300; // 降低了奖励值
                break;
            case DEVICE_TYPE_BS:
                value += 250;
                break;
            case DEVICE_TYPE_LEO:
                value += 200;
                break;
            case DEVICE_TYPE_CLOUD:
                value += 100; // 云不再是负分
                break;
            case DEVICE_TYPE_SENSOR:
                value = -1000;
                break;
            default:
                value += 50;
                break;
        }
        
        // 真实约束惩罚
        // 1. 负载惩罚
        double load = nodeLoadHistory.getOrDefault(nodeIndex, 0.0);
        value -= load * 100; // 负载越高惩罚越大
        
        // 2. 能量水平
        double energy = nodeEnergyHistory.getOrDefault(nodeIndex, 100.0);
        value += energy * 2; // 能量高有奖励
        
        // 3. 可靠性
        double reliability = nodeReliability.getOrDefault(nodeIndex, 1.0);
        value += reliability * 50;
        
        // 4. 网络拥塞影响
        if (isCloudNode(nodeIndex)) {
            value -= currentNetworkCongestion * 100;
        }
        
        // 5. 任务特性匹配
        value += calculateTaskNodeCompatibility(task, node);
        
        // 6. 历史性能
        value += getNodeHistoricalPerformance(nodeIndex);
        
        return value;
    }
    
    private double calculateTaskNodeCompatibility(Task task, ComputingNode node) {
        // 基于任务需求和节点能力的匹配度
        double compatibility = 0;
        
        // CPU需求匹配
        double cpuRequirement = task.getLength();
        double cpuCapability = node.getTotalMipsCapacity();
        if (cpuCapability > cpuRequirement * 1.5) {
            compatibility += 20;
        } else if (cpuCapability < cpuRequirement) {
            compatibility -= 30;
        }
        
        // 延迟需求匹配
        double latencyRequirement = task.getMaxLatency();
        if (latencyRequirement < 1.0 && isEdgeNode(node)) {
            compatibility += 30; // 低延迟任务偏向边缘
        } else if (latencyRequirement > 10.0 && isCloudNode(node)) {
            compatibility += 20; // 高延迟容忍任务可以用云
        }
        
        return compatibility;
    }
    
    private boolean isEdgeNode(ComputingNode node) {
        int deviceType = classifyDeviceType(node);
        return deviceType == DEVICE_TYPE_UAV || 
               deviceType == DEVICE_TYPE_BS || 
               deviceType == DEVICE_TYPE_LEO;
    }
    
    private boolean isCloudNode(ComputingNode node) {
        return classifyDeviceType(node) == DEVICE_TYPE_CLOUD;
    }
    
    private boolean isCloudNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= nodeList.size()) return false;
        return isCloudNode(nodeList.get(nodeIndex));
    }
    
    private double getNodeHistoricalPerformance(int nodeIndex) {
        // 基于历史表现给出奖励/惩罚
        // 这里可以根据该节点的历史成功率、平均延迟等计算
        return random.nextGaussian() * 10; // 简化实现
    }
    
    private double applySimulatedAnnealingWithNoise(double currentValue) {
        // 应用模拟退火，增加更多噪声
        double perturbation = random.nextGaussian() * currentTemperature * 0.5; // 增加噪声
        double newValue = currentValue + perturbation;
        
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
    
    private int selectExplorationNode(List<Integer> availableNodes) {
        // 探索策略：倾向于选择历史使用较少的节点
        Map<String, Integer> usageCounts = new HashMap<>();
        for (int nodeIndex : availableNodes) {
            ComputingNode node = nodeList.get(nodeIndex);
            String nodeType = getDeviceTypeName(classifyDeviceType(node));
            usageCounts.put(nodeType, nodeTypeUsage.getOrDefault(nodeType, 0));
        }
        
        // 找到使用最少的节点类型
        String leastUsedType = usageCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
        
        // 从该类型中随机选择
        List<Integer> leastUsedNodes = new ArrayList<>();
        for (int nodeIndex : availableNodes) {
            ComputingNode node = nodeList.get(nodeIndex);
            if (leastUsedType.equals(getDeviceTypeName(classifyDeviceType(node)))) {
                leastUsedNodes.add(nodeIndex);
            }
        }
        
        if (!leastUsedNodes.isEmpty()) {
            return leastUsedNodes.get(random.nextInt(leastUsedNodes.size()));
        } else {
            return availableNodes.get(random.nextInt(availableNodes.size()));
        }
    }
    
    private int getBestActionIndex(double[] actionValues) {
        int bestIndex = 0;
        for (int i = 1; i < actionValues.length; i++) {
            if (actionValues[i] > actionValues[bestIndex]) {
                bestIndex = i;
            }
        }
        return bestIndex;
    }
    
    private double getRecentSuccessRate() {
        if (totalCompletedTasks < 10) return 1.0;
        int recentTasks = Math.min(50, totalCompletedTasks);
        return (double) (recentTasks - Math.min(recentTasks, failedTasks)) / recentTasks;
    }
    
    @Override
    public void resultsReturned(Task task) {
        super.resultsReturned(task);
        totalCompletedTasks++;
        
        // 计算真实奖励
        double reward = calculateRealisticReward(task);
        totalReward += reward;
        
        // 更新成功/失败统计
        boolean isSuccess = determineTaskSuccess(task, reward);
        if (isSuccess) {
            successfulTasks++;
        } else {
            failedTasks++;
            logFailure("Task failed: " + task.getId() + ", reward: " + reward);
        }
        
        // 更新温度
        updateTemperatureRealistic();
        
        // 更新性能指标
        updatePerformanceMetrics(task, reward);
        
        // 定期结果报告
        if (totalCompletedTasks % 100 == 0) {
            printDetailedResults();
        }
    }
    
    private double calculateRealisticReward(Task task) {
        double reward = 0;
        
        // 引入更高的真实故障率
        if (random.nextDouble() < REAL_FAILURE_RATE) {
            return -100 + random.nextGaussian() * 20; // 大幅负奖励
        }
        
        // 基础成功奖励 (降低)
        if (task.getStatus() == Task.Status.SUCCESS) {
            reward += 20 + random.nextGaussian() * 10; // 降低基础奖励
        } else {
            reward -= 50 + random.nextGaussian() * 15;
            return reward;
        }
        
        // 延迟性能 (增加变化)
        double actualLatency = task.getActualNetworkTime() + task.getWatingTime() + task.getActualCpuTime();
        double expectedLatency = task.getMaxLatency();
        
        if (expectedLatency > 0) {
            double latencyRatio = actualLatency / expectedLatency;
            // 网络拥塞影响
            latencyRatio += currentNetworkCongestion * 0.5;
            // 随机网络变化
            latencyRatio += random.nextGaussian() * 0.3;
            
            if (latencyRatio <= 0.5) {
                reward += 15 + random.nextGaussian() * 5;
            } else if (latencyRatio <= 0.8) {
                reward += 8 + random.nextGaussian() * 4;
            } else if (latencyRatio <= 1.2) {
                reward += 2 + random.nextGaussian() * 3;
            } else {
                reward -= 20 + Math.abs(random.nextGaussian() * 8);
            }
        }
        
        // 能耗考虑
        ComputingNode executionNode = task.getOffloadingDestination();
        if (executionNode != null && executionNode != ComputingNode.NULL) {
            double nodeUtilization = executionNode.getCurrentCpuUtilization();
            nodeUtilization += random.nextGaussian() * 0.1; // 利用率变化
            
            if (nodeUtilization < 0.2) {
                reward -= 8; // 资源浪费
            } else if (nodeUtilization < 0.8) {
                reward += 10; // 良好利用
            } else {
                reward -= 15; // 过载
            }
        }
        
        // 系统负载影响
        double systemLoadPenalty = currentNetworkCongestion * 20;
        reward -= systemLoadPenalty;
        
        // 增大温度相关噪声
        double temperatureNoise = random.nextGaussian() * currentTemperature * 5;
        reward += temperatureNoise;
        
        // 时间相关变化
        double timeBasedVariation = Math.sin(totalDecisions * 0.01) * 8;
        reward += timeBasedVariation;
        
        return Math.max(-150, Math.min(100, reward)); // 扩大奖励范围
    }
    
    private boolean determineTaskSuccess(Task task, double reward) {
        // 更复杂的成功判定
        if (reward < -50) return false;
        if (task.getStatus() != Task.Status.SUCCESS) return false;
        
        // 额外的现实约束检查
        double actualLatency = task.getActualNetworkTime() + task.getWatingTime() + task.getActualCpuTime();
        if (actualLatency > task.getMaxLatency() * 1.5) return false;
        
        return true;
    }
    
    private void updateTemperatureRealistic() {
        double oldTemperature = currentTemperature;
        currentTemperature = Math.max(MIN_TEMPERATURE, currentTemperature * TEMPERATURE_DECAY);
        
        // 自适应温度调整
        if (totalCompletedTasks > 100 && totalCompletedTasks % 100 == 0) {
            double recentSuccessRate = getRecentSuccessRate();
            
            if (recentSuccessRate < 0.6) {
                // 性能很差，增加探索
                currentTemperature = Math.min(INITIAL_TEMPERATURE * 0.3, currentTemperature * 2.0);
                logLearningProgress("🔥 Temperature increased due to poor performance: " + 
                                 String.format("%.4f", currentTemperature));
            } else if (recentSuccessRate > 0.9 && currentTemperature > MIN_TEMPERATURE * 2) {
                // 性能很好，可以减少探索
                currentTemperature *= 0.8;
            }
        }
        
        // 周期性温度波动（模拟环境变化）
        if (totalDecisions % 1000 == 0 && random.nextDouble() < 0.4) {
            currentTemperature = Math.min(INITIAL_TEMPERATURE * 0.2, currentTemperature * 1.8);
            logLearningProgress("🌡️ Environmental change temperature spike: " + 
                             String.format("%.4f", currentTemperature));
        }
    }
    
    private void updatePerformanceMetrics(Task task, double reward) {
        // 更新延迟历史
        double latency = task.getActualNetworkTime() + task.getWatingTime() + task.getActualCpuTime();
        recentLatencies.add(latency);
        if (recentLatencies.size() > 200) { // 增加历史记录长度
            recentLatencies.remove(0);
        }
        
        // 更新能耗历史
        double energyUsage = task.getActualCpuTime() * 15; // 真实能耗模型
        recentEnergyUsage.add(energyUsage);
        if (recentEnergyUsage.size() > 200) {
            recentEnergyUsage.remove(0);
        }
        
        // 记录到性能历史
        performanceHistory.get("latency").add(latency);
        performanceHistory.get("energy").add(energyUsage);
        performanceHistory.get("success_rate").add(getRecentSuccessRate());
        performanceHistory.get("network_congestion").add(currentNetworkCongestion);
    }
    
    private void updateNodeLoad(int nodeIndex) {
        if (nodeIndex >= 0 && nodeIndex < nodeList.size()) {
            ComputingNode node = nodeList.get(nodeIndex);
            double currentLoad = node.getCurrentCpuUtilization();
            nodeLoadHistory.put(nodeIndex, currentLoad);
        }
    }
    
    private void printDetailedResults() {
        double successRate = totalCompletedTasks > 0 ? 
            (double) successfulTasks / totalCompletedTasks * 100 : 0;
        double failureRate = totalCompletedTasks > 0 ? 
            (double) failedTasks / totalCompletedTasks * 100 : 0;
        double avgReward = totalCompletedTasks > 0 ? totalReward / totalCompletedTasks : 0;
        
        String detailedInfo = String.format(
            "📊 详细结果: 成功率: %.1f%% (✅%d ❌%d), 平均奖励: %.2f, " +
            "网络拥塞: %.2f, 温度: %.4f",
            successRate, successfulTasks, failedTasks, avgReward,
            currentNetworkCongestion, currentTemperature);
        
        logLearningProgress(detailedInfo);
    }
    
    private void logFailure(String reason) {
        if (logWriter != null) {
            logWriter.println("[FAILURE] " + reason);
            logWriter.flush();
        }
    }
    
    // 其他辅助方法 (从原SARL复制并修改)
    private int classifyDeviceType(ComputingNode node) {
        String nodeType = node.getType().toString();
        String nodeName = node.getName();
        
        if (nodeName == null) nodeName = "";
        
        if ("EDGE_DEVICE".equals(nodeType)) {
            if (nodeName.contains("UAV") || nodeName.contains("Drone")) {
                return DEVICE_TYPE_UAV;
            } else {
                return DEVICE_TYPE_SENSOR;
            }
        } else if ("EDGE_DATACENTER".equals(nodeType) || nodeName.contains("Base")) {
            return DEVICE_TYPE_BS;
        } else if (nodeName.contains("LEO") || nodeName.contains("Satellite")) {
            return DEVICE_TYPE_LEO;
        } else if ("CLOUD".equals(nodeType)) {
            return DEVICE_TYPE_CLOUD;
        } else {
            return DEVICE_TYPE_SENSOR;
        }
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
    
    public void printRealisticSARLStatistics() {
        double successRate = totalCompletedTasks > 0 ? 
            (double) successfulTasks / totalCompletedTasks * 100 : 0;
        double failureRate = totalCompletedTasks > 0 ? 
            (double) failedTasks / totalCompletedTasks * 100 : 0;
        double avgReward = totalCompletedTasks > 0 ? totalReward / totalCompletedTasks : 0;
        
        System.out.println("\n🌍 === 真实SARL最终统计 (Realistic SARL Final Statistics) ===");
        System.out.println("📡 Realistic ISAC Architecture Analysis");
        System.out.println("🚀 Total decisions: " + totalDecisions);
        System.out.println("✅ Success rate: " + String.format("%.1f%%", successRate) + " (" + successfulTasks + " tasks)");
        System.out.println("❌ Failure rate: " + String.format("%.1f%%", failureRate) + " (" + failedTasks + " tasks)");
        System.out.println("🎯 Average reward: " + String.format("%.2f", avgReward));
        System.out.println("🌡️ Final temperature: " + String.format("%.4f", currentTemperature));
        System.out.println("🌐 Final network congestion: " + String.format("%.2f", currentNetworkCongestion));
        
        if (!nodeTypeUsage.isEmpty()) {
            System.out.println("\n📊 真实计算节点使用分布:");
            int totalUsage = nodeTypeUsage.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String, Integer> entry : nodeTypeUsage.entrySet()) {
                double percentage = totalUsage > 0 ? (double) entry.getValue() / totalUsage * 100 : 0;
                String nodeInfo = getNodeInfoChinese(entry.getKey());
                System.out.println("   " + nodeInfo + ": " + 
                                 String.format("%.1f%%", percentage) + " (" + entry.getValue() + " tasks)");
            }
        }
        
        // 性能分析
        if (!recentLatencies.isEmpty()) {
            double avgLatency = recentLatencies.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double avgEnergy = recentEnergyUsage.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.println("\n📈 性能指标:");
            System.out.println("   ⏱️ 平均延迟: " + String.format("%.3f", avgLatency) + "s");
            System.out.println("   🔋 平均能耗: " + String.format("%.2f", avgEnergy) + "Wh");
            System.out.println("   📡 网络拥塞变化: " + networkCongestionHistory.size() + " samples");
        }
        
        System.out.println("\n🎯 真实性增强特性:");
        System.out.println("   ✅ 网络拥塞模拟: Enabled");
        System.out.println("   ✅ 设备故障模拟: " + String.format("%.1f%%", REAL_FAILURE_RATE * 100) + " failure rate");
        System.out.println("   ✅ 资源约束模拟: Load threshold " + String.format("%.1f%%", DEVICE_OVERLOAD_THRESHOLD * 100));
        System.out.println("   ✅ 能量消耗模拟: " + String.format("%.1f%%", ENERGY_DEPLETION_RATE * 100) + " depletion rate");
    }
    
    private String getNodeInfoChinese(String nodeType) {
        switch (nodeType) {
            case "Sensor": return "📱 传感器(仅感知)";
            case "UAV": return "🚁 无人机(移动边缘)";
            case "BaseStation": return "📡 基站(固定边缘)";
            case "LEO_Satellite": return "🛰️ 卫星(空间计算)";
            case "Cloud": return "☁️ 云(中心计算)";
            default: return "🔧 " + nodeType;
        }
    }
}