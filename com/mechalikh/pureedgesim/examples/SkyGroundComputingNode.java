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

import com.mechalikh.pureedgesim.datacentersmanager.DefaultComputingNode;
import com.mechalikh.pureedgesim.locationmanager.Location;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationengine.Event;
import com.mechalikh.pureedgesim.simulationengine.OnSimulationStartListener;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;

/**
 * 空天地计算节点类
 * 
 * 支持不同类型的计算节点：
 * - UAV (无人机): 移动性强，计算能力中等，能耗敏感
 * - BASE_STATION (基站): 固定位置，计算能力强，稳定电源
 * - LEO_SATELLITE (低轨卫星): 高空移动，覆盖范围大，计算能力中等
 * - GROUND_SENSOR (地面传感器): 资源受限，主要用于任务生成
 * - CLOUD_SERVER (云服务器): 计算能力最强，延迟较高
 */
public class SkyGroundComputingNode extends DefaultComputingNode implements OnSimulationStartListener {
    
    // 节点类型枚举
    public enum SkyGroundNodeType {
        UAV,            // 无人机
        BASE_STATION,   // 基站
        LEO_SATELLITE,  // 低轨卫星
        GROUND_SENSOR,  // 地面传感器
        CLOUD_SERVER    // 云服务器
    }
    
    // 自定义事件标识
    private static final int UPDATE_LOCATION = 13000; // 位置更新事件标识
    private static final int ENERGY_UPDATE = 13001;   // 能耗更新事件标识
    
    private SkyGroundNodeType nodeType;
    private double altitude;           // 高度 (米)
    private double coverageRadius;     // 覆盖半径 (米)
    private boolean isMobile;          // 是否移动
    private double currentSpeed;       // 当前移动速度 (m/s)
    private double maxSpeed;           // 最大移动速度 (m/s)
    private boolean initialized = false; // 是否已初始化
    
    public SkyGroundComputingNode(SimulationManager simulationManager, double mipsCapacity, 
                                 int numberOfPes, double storage, double ram) {
        super(simulationManager, mipsCapacity, numberOfPes, storage, ram);
        
        // 初始化为默认类型，稍后在 onSimulationStart 中确定真实类型
        this.nodeType = SkyGroundNodeType.CLOUD_SERVER;
        initializeDefaultCharacteristics();
    }
    
    /**
     * 初始化默认特征参数（在节点类型确定前）
     */
    private void initializeDefaultCharacteristics() {
        this.altitude = 0;
        this.coverageRadius = 1000;
        this.isMobile = false;
        this.maxSpeed = 0;
        this.currentSpeed = 0;
    }
    
    /**
     * 根据节点特征确定节点类型（只在名称可用后调用）
     */
    private SkyGroundNodeType determineNodeType() {
        // 如果名称为空，返回默认类型
        if (getName() == null) {
            return SkyGroundNodeType.CLOUD_SERVER;
        }
        
        String name = getName().toLowerCase();
        if (name.contains("uav") || name.contains("drone")) {
            return SkyGroundNodeType.UAV;
        } else if (name.contains("satellite") || name.contains("leo")) {
            return SkyGroundNodeType.LEO_SATELLITE;
        } else if (name.contains("base") || name.contains("station")) {
            return SkyGroundNodeType.BASE_STATION;
        } else if (name.contains("sensor") || name.contains("iot")) {
            return SkyGroundNodeType.GROUND_SENSOR;
        } else {
            // 根据节点类型进一步判断
            if (getType() == SimulationParameters.TYPES.EDGE_DEVICE) {
                return SkyGroundNodeType.GROUND_SENSOR;
            } else if (getType() == SimulationParameters.TYPES.EDGE_DATACENTER) {
                return SkyGroundNodeType.BASE_STATION;
            } else {
                return SkyGroundNodeType.CLOUD_SERVER;
            }
        }
    }
    
    /**
     * 初始化不同类型节点的特征参数
     */
    private void initializeNodeCharacteristics() {
        switch (nodeType) {
            case UAV:
                this.altitude = 100;  // 100米高度
                this.coverageRadius = 500;  // 500米覆盖半径
                this.isMobile = true;
                this.maxSpeed = 20;  // 20 m/s
                this.currentSpeed = 10;
                break;
                
            case LEO_SATELLITE:
                this.altitude = 550000;  // 550公里高度
                this.coverageRadius = 1000000;  // 1000公里覆盖半径
                this.isMobile = true;
                this.maxSpeed = 7800;  // 7.8 km/s (轨道速度)
                this.currentSpeed = 7800;
                break;
                
            case BASE_STATION:
                this.altitude = 20;  // 20米高度
                this.coverageRadius = 2000;  // 2公里覆盖半径
                this.isMobile = false;
                this.maxSpeed = 0;
                this.currentSpeed = 0;
                break;
                
            case GROUND_SENSOR:
                this.altitude = 2;  // 2米高度
                this.coverageRadius = 100;  // 100米覆盖半径
                this.isMobile = false;
                this.maxSpeed = 0;
                this.currentSpeed = 0;
                break;
                
            case CLOUD_SERVER:
                this.altitude = 0;  // 地面
                this.coverageRadius = Double.MAX_VALUE;  // 全球覆盖
                this.isMobile = false;
                this.maxSpeed = 0;
                this.currentSpeed = 0;
                break;
        }
        initialized = true;
    }
    
    @Override
    public void onSimulationStart() {
        super.onSimulationStart();
        
        // 现在名称应该已经设置，可以确定节点类型
        if (!initialized) {
            this.nodeType = determineNodeType();
            initializeNodeCharacteristics();
        }
        
        // 调度位置更新事件（对于移动节点）
        if (isMobile) {
            scheduleNow(this, UPDATE_LOCATION);
        }
        
        // 调度能耗更新事件
        scheduleNow(this, ENERGY_UPDATE);
    }
    
    @Override
    public void processEvent(final Event ev) {
        switch (ev.getTag()) {
            case UPDATE_LOCATION:
                updateLocation();
                // 调度下一次位置更新
                schedule(this, (double)SimulationParameters.updateInterval, UPDATE_LOCATION);
                break;
                
            case ENERGY_UPDATE:
                updateEnergyConsumption();
                // 调度下一次能耗更新
                schedule(this, (double)SimulationParameters.updateInterval, ENERGY_UPDATE);
                break;
                
            default:
                super.processEvent(ev);
                break;
        }
    }
    
    /**
     * 更新移动节点的位置
     */
    private void updateLocation() {
        if (!isMobile) return;
        
        Location currentLocation = getMobilityModel().getCurrentLocation();
        double updateInterval = SimulationParameters.updateInterval;
        
        switch (nodeType) {
            case UAV:
                // 无人机随机移动模式
                updateUAVLocation(currentLocation, updateInterval);
                break;
                
            case LEO_SATELLITE:
                // 卫星轨道移动模式
                updateSatelliteLocation(currentLocation, updateInterval);
                break;
        }
    }
    
    /**
     * 更新无人机位置
     */
    private void updateUAVLocation(Location currentLocation, double updateInterval) {
        // 简单的随机移动模型
        double deltaX = (Math.random() - 0.5) * currentSpeed * updateInterval * 2;
        double deltaY = (Math.random() - 0.5) * currentSpeed * updateInterval * 2;
        
        double newX = Math.max(0, Math.min(SimulationParameters.simulationMapLength,
                currentLocation.getXPos() + deltaX));
        double newY = Math.max(0, Math.min(SimulationParameters.simulationMapWidth,
                currentLocation.getYPos() + deltaY));
        
        // 通过自定义移动模型更新位置
        if (getMobilityModel() instanceof SkyGroundMobilityModel) {
            ((SkyGroundMobilityModel) getMobilityModel()).setCurrentLocation(new Location(newX, newY));
        }
    }
    
    /**
     * 更新卫星位置
     */
    private void updateSatelliteLocation(Location currentLocation, double updateInterval) {
        // 简化的轨道移动模型
        double angle = (simulationManager.getSimulation().clock() * currentSpeed) / (altitude * Math.PI);
        double centerX = SimulationParameters.simulationMapLength / 2.0;
        double centerY = SimulationParameters.simulationMapWidth / 2.0;
        double radius = Math.max(centerX, centerY);
        
        double newX = centerX + radius * Math.cos(angle);
        double newY = centerY + radius * Math.sin(angle);
        
        // 通过自定义移动模型更新位置
        if (getMobilityModel() instanceof SkyGroundMobilityModel) {
            ((SkyGroundMobilityModel) getMobilityModel()).setCurrentLocation(new Location(newX, newY));
        }
    }
    
    /**
     * 自定义能耗更新
     */
    private void updateEnergyConsumption() {
        // 根据节点类型和当前状态更新能耗
        double additionalEnergyConsumption = 0;
        
        switch (nodeType) {
            case UAV:
                // 无人机飞行和计算的额外能耗
                additionalEnergyConsumption = getCurrentCpuUtilization() * 10 + 
                                            (isMobile ? currentSpeed * 0.5 : 0);
                break;
                
            case LEO_SATELLITE:
                // 卫星的通信和计算能耗
                additionalEnergyConsumption = getCurrentCpuUtilization() * 15 + 5; // 基础通信能耗
                break;
                
            case BASE_STATION:
                // 基站的计算和通信能耗
                additionalEnergyConsumption = getCurrentCpuUtilization() * 8;
                break;
                
            case GROUND_SENSOR:
                // 传感器的低功耗设计
                additionalEnergyConsumption = getCurrentCpuUtilization() * 2 + 0.1;
                break;
        }
        
        // 应用额外的能耗 - 简化处理，使用现有的能耗更新方法
        if (getEnergyModel().isBatteryPowered() && additionalEnergyConsumption > 0) {
            // 通过动态能耗更新来模拟不同类型节点的能耗特性
            getEnergyModel().updateDynamicEnergyConsumption(additionalEnergyConsumption, 1.0);
        }
    }
    
    /**
     * 计算到目标位置的通信延迟
     */
    public double getCommLatencyTo(SkyGroundComputingNode targetNode) {
        double distance = getDistanceTo(targetNode);
        double baseLatency = 0;
        
        // 根据节点类型确定基础延迟
        if (this.nodeType == SkyGroundNodeType.LEO_SATELLITE || 
            targetNode.nodeType == SkyGroundNodeType.LEO_SATELLITE) {
            baseLatency = 0.02; // 卫星通信延迟
        } else if (this.nodeType == SkyGroundNodeType.UAV || 
                   targetNode.nodeType == SkyGroundNodeType.UAV) {
            baseLatency = 0.01; // 无人机通信延迟
        } else {
            baseLatency = 0.005; // 地面网络延迟
        }
        
        // 距离对延迟的影响
        return baseLatency + (distance / 299792458000.0); // 光速传播延迟
    }
    
    /**
     * 计算到目标节点的距离（考虑高度）
     */
    public double getDistanceTo(SkyGroundComputingNode targetNode) {
        Location thisLocation = getMobilityModel().getCurrentLocation();
        Location targetLocation = targetNode.getMobilityModel().getCurrentLocation();
        
        double horizontalDistance = Math.sqrt(
            Math.pow(thisLocation.getXPos() - targetLocation.getXPos(), 2) +
            Math.pow(thisLocation.getYPos() - targetLocation.getYPos(), 2)
        );
        
        double heightDifference = Math.abs(this.altitude - targetNode.altitude);
        
        return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(heightDifference, 2));
    }
    
    // Getter 方法
    public SkyGroundNodeType getNodeType() { return nodeType; }
    public double getAltitude() { return altitude; }
    public double getCoverageRadius() { return coverageRadius; }
    public boolean isMobile() { return isMobile; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getMaxSpeed() { return maxSpeed; }
    
    // Setter 方法
    public void setCurrentSpeed(double speed) { 
        this.currentSpeed = Math.min(speed, maxSpeed); 
    }
} 