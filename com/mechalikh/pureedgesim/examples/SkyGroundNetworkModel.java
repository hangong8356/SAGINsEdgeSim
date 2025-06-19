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

import java.util.ArrayList;
import java.util.List;

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.network.DefaultNetworkModel;
import com.mechalikh.pureedgesim.network.TransferProgress;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;

/**
 * 空天地网络模型
 * 
 * 实现空天地一体化网络的特性：
 * 1. 不同链路类型的带宽和延迟特性
 * 2. 基于距离和高度的链路质量计算
 * 3. 动态网络拓扑变化处理
 * 4. 多层网络架构支持
 */
public class SkyGroundNetworkModel extends DefaultNetworkModel {
    
    // 不同链路类型的带宽配置 (Mbps)
    private static final double SATELLITE_TO_GROUND_BANDWIDTH = 50;   // 卫星-地面链路
    private static final double SATELLITE_TO_SATELLITE_BANDWIDTH = 100; // 星间链路
    private static final double UAV_TO_GROUND_BANDWIDTH = 200;       // 无人机-地面链路
    private static final double UAV_TO_UAV_BANDWIDTH = 150;         // 无人机间链路
    private static final double GROUND_TO_GROUND_BANDWIDTH = 1000;   // 地面网络
    private static final double GROUND_TO_CLOUD_BANDWIDTH = 500;     // 地面-云链路
    
    // 链路质量阈值
    private static final double MAX_COMMUNICATION_DISTANCE = 2000000; // 最大通信距离 (米)
    private static final double GOOD_LINK_THRESHOLD = 0.8;            // 良好链路质量阈值
    
    // 记录自定义的传输进度列表
    private List<TransferProgress> skyGroundTransferList = new ArrayList<>();
    
    public SkyGroundNetworkModel(SimulationManager simulationManager) {
        super(simulationManager);
    }
    
    /**
     * 重写传输完成处理方法，添加空天地网络特性
     */
    @Override
    protected void transferFinished(TransferProgress transfer) {
        // 移除已完成的传输
        skyGroundTransferList.remove(transfer);
        
        // 如果涉及空天地节点，更新统计信息
        if (transfer.getTask().getEdgeDevice() instanceof SkyGroundComputingNode ||
            transfer.getTask().getOffloadingDestination() instanceof SkyGroundComputingNode) {
            updateSkyGroundNetworkStatistics(transfer);
        }
        
        // 调用父类的默认处理
        super.transferFinished(transfer);
    }
    
    /**
     * 更新空天地网络统计信息
     */
    private void updateSkyGroundNetworkStatistics(TransferProgress transfer) {
        // 这里可以添加统计信息更新逻辑
        // 例如：记录不同类型节点间的传输性能
    }
    
    /**
     * 计算链路质量
     */
    private double calculateLinkQuality(SkyGroundComputingNode src, SkyGroundComputingNode dst, double distance) {
        // 基础质量计算
        double quality = 1.0;
        
        // 距离对质量的影响
        if (distance > MAX_COMMUNICATION_DISTANCE) {
            return 0; // 超出通信范围
        }
        
        double distanceFactor = 1.0 - (distance / MAX_COMMUNICATION_DISTANCE);
        quality *= distanceFactor;
        
        // 高度差对质量的影响
        double altitudeDiff = Math.abs(src.getAltitude() - dst.getAltitude());
        double altitudeFactor = 1.0 - Math.min(0.3, altitudeDiff / 1000000.0); // 高度差影响
        quality *= altitudeFactor;
        
        // 节点移动性对质量的影响
        if (src.isMobile() || dst.isMobile()) {
            quality *= 0.9; // 移动节点稍微降低链路稳定性
        }
        
        return Math.max(0, Math.min(1.0, quality));
    }
    
    /**
     * 获取空天地节点间的带宽
     */
    public double getSkyGroundBandwidth(SkyGroundComputingNode src, SkyGroundComputingNode dst) {
        SkyGroundComputingNode.SkyGroundNodeType srcType = src.getNodeType();
        SkyGroundComputingNode.SkyGroundNodeType dstType = dst.getNodeType();
        
        // 基于节点类型确定基础带宽
        double baseBandwidth = determineBaseBandwidth(srcType, dstType);
        
        // 考虑距离和质量的影响
        double distance = src.getDistanceTo(dst);
        double quality = calculateLinkQuality(src, dst, distance);
        
        return baseBandwidth * quality;
    }
    
    /**
     * 根据节点类型确定基础带宽
     */
    private double determineBaseBandwidth(SkyGroundComputingNode.SkyGroundNodeType srcType, 
                                        SkyGroundComputingNode.SkyGroundNodeType dstType) {
        
        // 卫星相关链路
        if (srcType == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE || 
            dstType == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE) {
            
            if (srcType == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE && 
                dstType == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE) {
                return SATELLITE_TO_SATELLITE_BANDWIDTH; // 星间链路
            } else {
                return SATELLITE_TO_GROUND_BANDWIDTH; // 星地链路
            }
        }
        
        // 无人机相关链路
        if (srcType == SkyGroundComputingNode.SkyGroundNodeType.UAV || 
            dstType == SkyGroundComputingNode.SkyGroundNodeType.UAV) {
            
            if (srcType == SkyGroundComputingNode.SkyGroundNodeType.UAV && 
                dstType == SkyGroundComputingNode.SkyGroundNodeType.UAV) {
                return UAV_TO_UAV_BANDWIDTH; // 无人机间链路
            } else {
                return UAV_TO_GROUND_BANDWIDTH; // 无人机-地面链路
            }
        }
        
        // 云服务器链路
        if (srcType == SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER || 
            dstType == SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER) {
            return GROUND_TO_CLOUD_BANDWIDTH;
        }
        
        // 地面网络 (基站、传感器等)
        return GROUND_TO_GROUND_BANDWIDTH;
    }
    
    /**
     * 计算空天地节点间的延迟
     */
    public double getSkyGroundLatency(SkyGroundComputingNode src, SkyGroundComputingNode dst) {
        return src.getCommLatencyTo(dst);
    }
    
    /**
     * 检查两个节点间是否可以直接通信
     */
    public boolean isDirectCommunicationPossible(ComputingNode src, ComputingNode dst) {
        if (src instanceof SkyGroundComputingNode && dst instanceof SkyGroundComputingNode) {
            SkyGroundComputingNode srcNode = (SkyGroundComputingNode) src;
            SkyGroundComputingNode dstNode = (SkyGroundComputingNode) dst;
            
            double distance = srcNode.getDistanceTo(dstNode);
            return distance <= MAX_COMMUNICATION_DISTANCE;
        }
        
        return true; // 对于非自定义节点，假设可以通信
    }
    
    /**
     * 获取网络统计信息
     */
    public void printNetworkStatistics() {
        int totalLinks = skyGroundTransferList.size();
        int goodQualityLinks = 0;
        double avgBandwidth = 0;
        
        for (TransferProgress transfer : skyGroundTransferList) {
            ComputingNode src = transfer.getTask().getEdgeDevice();
            ComputingNode dst = transfer.getTask().getOffloadingDestination();
            
            if (src instanceof SkyGroundComputingNode && dst instanceof SkyGroundComputingNode) {
                SkyGroundComputingNode srcNode = (SkyGroundComputingNode) src;
                SkyGroundComputingNode dstNode = (SkyGroundComputingNode) dst;
                
                double bandwidth = getSkyGroundBandwidth(srcNode, dstNode);
                avgBandwidth += bandwidth;
                
                double distance = srcNode.getDistanceTo(dstNode);
                double quality = calculateLinkQuality(srcNode, dstNode, distance);
                if (quality >= GOOD_LINK_THRESHOLD) {
                    goodQualityLinks++;
                }
            }
        }
        
        if (totalLinks > 0) {
            avgBandwidth /= totalLinks;
            double qualityRatio = (double) goodQualityLinks / totalLinks;
            
            System.out.println("=== Sky-Ground Network Statistics ===");
            System.out.println("Total active links: " + totalLinks);
            System.out.println("Good quality links: " + goodQualityLinks + " (" + String.format("%.1f%%", qualityRatio * 100) + ")");
            System.out.println("Average bandwidth: " + String.format("%.2f Mbps", avgBandwidth));
        }
    }
    
    /**
     * 添加空天地传输进度跟踪
     */
    public void addSkyGroundTransfer(TransferProgress transfer) {
        if (!skyGroundTransferList.contains(transfer)) {
            skyGroundTransferList.add(transfer);
        }
    }
    
    /**
     * 获取两个计算节点间的推荐带宽 (供外部调用)
     */
    public double getRecommendedBandwidth(ComputingNode src, ComputingNode dst) {
        if (src instanceof SkyGroundComputingNode && dst instanceof SkyGroundComputingNode) {
            return getSkyGroundBandwidth((SkyGroundComputingNode) src, (SkyGroundComputingNode) dst);
        }
        
        // 对于非空天地节点，返回默认带宽
        return SimulationParameters.wanBandwidthBitsPerSecond / 1000000.0; // 转换为Mbps
    }
    
    /**
     * 获取两个计算节点间的推荐延迟 (供外部调用)
     */
    public double getRecommendedLatency(ComputingNode src, ComputingNode dst) {
        if (src instanceof SkyGroundComputingNode && dst instanceof SkyGroundComputingNode) {
            return getSkyGroundLatency((SkyGroundComputingNode) src, (SkyGroundComputingNode) dst);
        }
        
        // 对于非空天地节点，返回默认延迟
        return SimulationParameters.wanLatency;
    }
} 
 