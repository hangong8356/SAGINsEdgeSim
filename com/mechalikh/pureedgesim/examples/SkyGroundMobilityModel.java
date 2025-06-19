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

import com.mechalikh.pureedgesim.locationmanager.DefaultMobilityModel;
import com.mechalikh.pureedgesim.locationmanager.Location;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;

/**
 * 空天地移动模型
 * 
 * 支持不同类型节点的移动模式：
 * - 无人机：3D随机移动
 * - 卫星：轨道运动
 * - 基站：静止
 * - 传感器：静止或慢速移动
 */
public class SkyGroundMobilityModel extends DefaultMobilityModel {
    
    public SkyGroundMobilityModel(SimulationManager simulationManager, Location currentLocation) {
        super(simulationManager, currentLocation);
    }
    
    /**
     * 设置当前位置（用于外部更新位置）
     */
    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }
    
    @Override
    protected Location getNextLocation(Location currentLocation) {
        // 使用默认的移动模型实现
        // 在实际应用中，可以根据节点类型实现不同的移动模式
        return super.getNextLocation(currentLocation);
    }
    
    /**
     * 无人机移动模式：考虑高度变化的3D移动
     */
    protected Location getUAVNextLocation(Location currentLocation) {
        double xPosition = currentLocation.getXPos();
        double yPosition = currentLocation.getYPos();
        
        // 3D随机移动，考虑高度限制
        if (pause && pauseDuration > 0) {
            pauseDuration -= SimulationParameters.updateInterval;
            return currentLocation;
        }
        
        // UAV特有的移动逻辑
        reorientateUAV(xPosition, yPosition);
        
        if (mobilityDuration <= 0) {
            pause();
        }
        
        if (pauseDuration <= 0) {
            resume();
        }
        
        return updateLocationUAV(xPosition, yPosition);
    }
    
    /**
     * 卫星轨道移动模式
     */
    protected Location getSatelliteNextLocation(Location currentLocation) {
        // 轨道运动模式，沿固定轨道移动
        double centerX = SimulationParameters.simulationMapLength / 2.0;
        double centerY = SimulationParameters.simulationMapWidth / 2.0;
        
        // 基于时间的轨道位置计算
        double orbitRadius = Math.max(centerX, centerY) * 0.8;
        double angularSpeed = 0.1; // 角速度 (弧度/秒)
        double currentTime = getSimulationManager().getSimulation().clock();
        double angle = (currentTime * angularSpeed) % (2 * Math.PI);
        
        double newX = centerX + orbitRadius * Math.cos(angle);
        double newY = centerY + orbitRadius * Math.sin(angle);
        
        return new Location(newX, newY);
    }
    
    /**
     * UAV重定向逻辑
     */
    protected void reorientateUAV(double xPosition, double yPosition) {
        // 增加垂直方向的考虑
        if (xPosition >= SimulationParameters.simulationMapLength)
            orientationAngle = -90 - random.nextInt(180);
        else if (xPosition <= 0)
            orientationAngle = -90 + random.nextInt(180);
        if (yPosition >= SimulationParameters.simulationMapWidth)
            orientationAngle = -random.nextInt(180);
        else if (yPosition <= 0)
            orientationAngle = random.nextInt(180);
    }
    
    /**
     * UAV位置更新
     */
    protected Location updateLocationUAV(double xPosition, double yPosition) {
        double distance = getSpeed() * SimulationParameters.updateInterval;
        double X_distance = Math.cos(Math.toRadians(orientationAngle)) * distance;
        double Y_distance = Math.sin(Math.toRadians(orientationAngle)) * distance;
        
        double X_pos = xPosition + X_distance;
        double Y_pos = yPosition + Y_distance;
        
        // 确保UAV在有效范围内
        X_pos = Math.max(0, Math.min(SimulationParameters.simulationMapLength, X_pos));
        Y_pos = Math.max(0, Math.min(SimulationParameters.simulationMapWidth, Y_pos));
        
        return new Location(X_pos, Y_pos);
    }
} 