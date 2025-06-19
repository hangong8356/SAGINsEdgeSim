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

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;
import com.mechalikh.pureedgesim.taskgenerator.Task;
import com.mechalikh.pureedgesim.taskorchestrator.DefaultOrchestrator;

/**
 * 空天地任务编排器
 * 
 * 实现智能的任务调度策略，考虑不同类型计算节点的特性：
 * 1. 延迟敏感任务优先选择最近的计算节点
 * 2. 计算密集型任务选择处理能力强的节点
 * 3. 考虑节点的负载均衡和能耗效率
 * 4. 根据任务类型和节点类型进行匹配
 */
public class SkyGroundOrchestrator extends DefaultOrchestrator {
    
    public SkyGroundOrchestrator(SimulationManager simulationManager) {
        super(simulationManager);
    }
    
    @Override
    protected int findComputingNode(String[] architecture, Task task) {
        if ("ROUND_ROBIN".equals(algorithmName)) {
            return roundRobin(architecture, task);
        } else if ("TRADE_OFF".equals(algorithmName)) {
            return tradeOff(architecture, task);
        } else if ("SKY_GROUND_SMART".equals(algorithmName)) {
            return skyGroundSmartScheduling(architecture, task);
        } else {
            throw new IllegalArgumentException(getClass().getName() + " - Unknown orchestration algorithm '" 
                + algorithmName + "', please check the simulation parameters file...");
        }
    }
    
    /**
     * 空天地智能调度算法
     */
    private int skyGroundSmartScheduling(String[] architecture, Task task) {
        int selectedNodeIndex = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < nodeList.size(); i++) {
            ComputingNode node = nodeList.get(i);
            
            // 检查是否可以卸载到该节点
            if (!offloadingIsPossible(task, node, architecture)) {
                continue;
            }
            
            // 计算节点适应性得分
            double score = calculateNodeFitness(task, node);
            
            if (score > bestScore) {
                bestScore = score;
                selectedNodeIndex = i;
            }
        }
        
        return selectedNodeIndex;
    }
    
    /**
     * 计算节点对任务的适应性得分
     */
    private double calculateNodeFitness(Task task, ComputingNode node) {
        double score = 0;
        
        // 如果是自定义的空天地节点，获取其类型信息
        SkyGroundComputingNode.SkyGroundNodeType nodeType = getNodeType(node);
        
        // 1. 延迟权重 (40%)
        double latencyScore = calculateLatencyScore(task, node);
        score += latencyScore * 0.4;
        
        // 2. 计算能力权重 (30%)
        double computingScore = calculateComputingScore(task, node);
        score += computingScore * 0.3;
        
        // 3. 负载均衡权重 (20%)
        double loadScore = calculateLoadScore(node);
        score += loadScore * 0.2;
        
        // 4. 节点类型适应性权重 (10%)
        double typeScore = calculateTypeAdaptationScore(task, nodeType);
        score += typeScore * 0.1;
        
        return score;
    }
    
    /**
     * 获取节点类型
     */
    private SkyGroundComputingNode.SkyGroundNodeType getNodeType(ComputingNode node) {
        if (node instanceof SkyGroundComputingNode) {
            return ((SkyGroundComputingNode) node).getNodeType();
        }
        
        // 对于非自定义节点，根据类型推断
        if (node.getType() == SimulationParameters.TYPES.CLOUD) {
            return SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER;
        } else if (node.getType() == SimulationParameters.TYPES.EDGE_DATACENTER) {
            return SkyGroundComputingNode.SkyGroundNodeType.BASE_STATION;
        } else {
            return SkyGroundComputingNode.SkyGroundNodeType.GROUND_SENSOR;
        }
    }
    
    /**
     * 计算延迟得分 (分数越高越好)
     */
    private double calculateLatencyScore(Task task, ComputingNode node) {
        // 估算网络延迟
        double networkLatency = estimateNetworkLatency(task, node);
        
        // 任务的延迟容忍度 - 从应用程序列表中获取
        double taskLatencyTolerance = SimulationParameters.applicationList.get(task.getApplicationID()).getLatency();
        
        // 如果网络延迟超过任务要求，得分为0
        if (networkLatency > taskLatencyTolerance) {
            return 0;
        }
        
        // 延迟越小，得分越高
        return Math.max(0, (taskLatencyTolerance - networkLatency) / taskLatencyTolerance * 100);
    }
    
    /**
     * 计算计算能力得分
     */
    private double calculateComputingScore(Task task, ComputingNode node) {
        if (node.getTotalMipsCapacity() == 0) {
            return 0;
        }
        
        // 考虑当前CPU利用率
        double availableCapacity = node.getTotalMipsCapacity() * (1 - node.getCurrentCpuUtilization() / 100.0);
        
        // 如果可用容量不足以处理任务，得分为0
        if (availableCapacity < task.getLength()) {
            return 0;
        }
        
        // 可用容量越大，得分越高
        return Math.min(100, availableCapacity / task.getLength() * 10);
    }
    
    /**
     * 计算负载均衡得分
     */
    private double calculateLoadScore(ComputingNode node) {
        double currentUtilization = node.getCurrentCpuUtilization();
        
        // 利用率越低，得分越高
        return Math.max(0, 100 - currentUtilization);
    }
    
    /**
     * 计算节点类型适应性得分
     */
    private double calculateTypeAdaptationScore(Task task, SkyGroundComputingNode.SkyGroundNodeType nodeType) {
        // 从任务的应用程序ID获取应用类型
        String taskType = SimulationParameters.applicationList.get(task.getApplicationID()).getType().toLowerCase();
        
        switch (nodeType) {
            case UAV:
                // 无人机适合处理移动性和实时性要求高的任务
                if (taskType.contains("real-time") || taskType.contains("mobile") || taskType.contains("emergency")) {
                    return 90;
                } else if (taskType.contains("soft real-time")) {
                    return 70;
                } else {
                    return 30;
                }
                
            case LEO_SATELLITE:
                // 卫星适合处理大范围覆盖的任务
                if (taskType.contains("global") || taskType.contains("monitoring") || taskType.contains("communication")) {
                    return 95;
                } else if (taskType.contains("computation")) {
                    return 60;
                } else {
                    return 40;
                }
                
            case BASE_STATION:
                // 基站适合处理各种类型的任务，平衡性好
                return 80;
                
            case GROUND_SENSOR:
                // 传感器节点主要用于数据收集，处理能力有限
                if (taskType.contains("sensing") || taskType.contains("data collection")) {
                    return 85;
                } else {
                    return 10;
                }
                
            case CLOUD_SERVER:
                // 云服务器适合处理计算密集型任务
                if (taskType.contains("heavy") || taskType.contains("computation") || taskType.contains("batch")) {
                    return 100;
                } else if (taskType.contains("non real-time")) {
                    return 90;
                } else {
                    return 50;
                }
                
            default:
                return 50;
        }
    }
    
    /**
     * 估算网络延迟
     */
    private double estimateNetworkLatency(Task task, ComputingNode node) {
        // 基础网络延迟
        double baseLatency = 0;
        
        if (node.getType() == SimulationParameters.TYPES.CLOUD) {
            baseLatency = 0.05; // 云服务器延迟较高
        } else if (node.getType() == SimulationParameters.TYPES.EDGE_DATACENTER) {
            baseLatency = 0.01; // 边缘数据中心延迟中等
        } else {
            baseLatency = 0.005; // 边缘设备延迟较低
        }
        
        // 如果是自定义节点，使用更精确的延迟计算
        if (node instanceof SkyGroundComputingNode && task.getEdgeDevice() instanceof SkyGroundComputingNode) {
            SkyGroundComputingNode skyGroundNode = (SkyGroundComputingNode) node;
            SkyGroundComputingNode sourceNode = (SkyGroundComputingNode) task.getEdgeDevice();
            return skyGroundNode.getCommLatencyTo(sourceNode);
        }
        
        return baseLatency;
    }
    
    @Override
    public void resultsReturned(Task task) {
        // 可以在这里添加结果处理逻辑
        // 例如：统计不同类型节点的任务完成情况
    }
} 