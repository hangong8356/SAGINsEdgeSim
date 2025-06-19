/**
 * Sky-Ground 3D Map Chart for visualizing multi-layered edge computing architecture
 * 
 * This class provides a 3D visualization of the sky-ground integrated edge computing environment:
 * - Space Layer: LEO Satellites (highest altitude ~550km)
 * - Aerial Layer: UAVs/Drones (medium altitude ~100m)  
 * - Terrestrial Layer: Base Stations (~20m) and Ground Sensors (ground level)
 * - Cloud Layer: Cloud Servers (centralized locations)
 * - Connection Links: Real-time data flow visualization
 * 
 * Features:
 * - Interactive 3D rotation and zoom
 * - Real-time node status updates (active/idle/dead)
 * - Connection link animation
 * - Layer-based node positioning
 * - Performance metrics overlay
 */
package examples;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.mechalikh.pureedgesim.datacentersmanager.ComputingNode;
import com.mechalikh.pureedgesim.datacentersmanager.ComputingNodesGenerator;
import com.mechalikh.pureedgesim.scenariomanager.SimulationParameters;
import com.mechalikh.pureedgesim.simulationmanager.SimulationManager;

public class SkyGround3DMapChart extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // 3D View Parameters
    private double viewAngleX = 25.0;  // X-axis rotation angle (pitch)
    private double viewAngleY = 45.0;  // Y-axis rotation angle (yaw)  
    private double zoom = 1.0;         // Zoom scale
    private Point lastMousePoint;      // Last mouse position
    
    // Realistic Sky-Ground Layer Heights for XYZ visualization
    private static final double SATELLITE_ALTITUDE = 400.0;   // Space layer - Satellites (highest)
    private static final double UAV_ALTITUDE = 150.0;         // Aerial layer - UAVs (medium)
    private static final double GROUND_ALTITUDE = 0.0;        // Ground layer - Sensors, Base Stations, Cloud (lowest)
    private static final double BASE_STATION_ALTITUDE = 0.0;  // Ground level - same as sensors
    private static final double CLOUD_ALTITUDE = 0.0;         // Ground level - data centers
    
    // Node Colors for different types
    private static final Color SENSOR_COLOR = new Color(34, 139, 34);      // Forest green - Ground sensors
    private static final Color BASE_STATION_COLOR = new Color(65, 105, 225); // Royal blue - Base stations
    private static final Color UAV_COLOR = new Color(255, 140, 0);          // Dark orange - UAVs
    private static final Color SATELLITE_COLOR = new Color(220, 20, 60);    // Crimson - Satellites
    private static final Color CLOUD_COLOR = new Color(128, 0, 128);        // Purple - Cloud servers
    
    // Connection Colors
    private static final Color ACTIVE_CONNECTION = new Color(0, 255, 0, 120); // Green - Active connections
    private static final Color IDLE_CONNECTION = new Color(255, 255, 0, 80);  // Yellow - Idle connections
    private static final Color DATA_FLOW = new Color(255, 255, 255, 180);    // White - Data flow
    
    // Simulation components
    private SimulationManager simulationManager;
    private Timer updateTimer;
    
    // Node tracking
    private final List<Node3D> nodeList = new CopyOnWriteArrayList<>();
    private final List<Connection3D> connectionList = new CopyOnWriteArrayList<>(); 
    private final Map<String, NodeMetrics> nodeMetrics = new ConcurrentHashMap<>();
    
    // Animation parameters
    private long animationTime = 0;
    private final Random random = new Random();
    
    /**
     * 3D节点信息类
     */
    private static class Node3D {
        String id;
        SkyGroundComputingNode.SkyGroundNodeType type;
        double x, y, z;           // 3D coordinates
        double projectedX, projectedY; // 2D projection coordinates
        Color color;
        NodeStatus status;
        double cpuUtilization;
        int nodeSize;
        boolean isVisible;
        
        Node3D(String id, SkyGroundComputingNode.SkyGroundNodeType type, double x, double y, double z) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.status = NodeStatus.IDLE;
            this.cpuUtilization = 0.0;
            this.nodeSize = 12;
            this.isVisible = true;
            setColorByType();
        }
        
        private void setColorByType() {
            switch (type) {
                case GROUND_SENSOR:
                    this.color = SENSOR_COLOR;
                    this.z = GROUND_ALTITUDE;  // Ground level (0m)
                    break;
                case BASE_STATION:
                    this.color = BASE_STATION_COLOR;
                    this.z = BASE_STATION_ALTITUDE;  // Ground level (0m) - same as sensors
                    break;
                case UAV:
                    this.color = UAV_COLOR;
                    this.z = UAV_ALTITUDE;  // Aerial layer (150m)
                    break;
                case LEO_SATELLITE:
                    this.color = SATELLITE_COLOR;
                    this.z = SATELLITE_ALTITUDE;  // Space layer (400m)
                    break;
                case CLOUD_SERVER:
                    this.color = CLOUD_COLOR;
                    this.z = CLOUD_ALTITUDE;  // Ground level (0m) - data centers on ground
                    break;
            }
        }
    }
    
    /**
     * 3D连接信息类
     */
    private static class Connection3D {
        Node3D fromNode, toNode;
        double bandwidth;
        boolean isActive;
        Color connectionColor;
        double animationPhase;
        
        Connection3D(Node3D from, Node3D to) {
            this.fromNode = from;
            this.toNode = to;
            this.bandwidth = 0.0;
            this.isActive = false;
            this.connectionColor = IDLE_CONNECTION;
            this.animationPhase = Math.random() * 2 * Math.PI;
        }
    }
    
    /**
     * 节点状态枚举
     */
    private enum NodeStatus {
        ACTIVE(Color.RED),
        IDLE(Color.BLUE),
        DEAD(Color.GRAY);
        
        private final Color statusColor;
        
        NodeStatus(Color color) {
            this.statusColor = color;
        }
        
        public Color getColor() {
            return statusColor;
        }
    }
    
    /**
     * 节点性能指标类
     */
    private static class NodeMetrics {
        double cpuUtilization;
        double memoryUsage;
        double networkUtilization;
        int activeTasks;
        long lastUpdate;
        
        NodeMetrics() {
            this.cpuUtilization = 0.0;
            this.memoryUsage = 0.0;
            this.networkUtilization = 0.0;
            this.activeTasks = 0;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    /**
     * 构造函数
     */
    public SkyGround3DMapChart(SimulationManager simulationManager) {
        this.simulationManager = simulationManager;
        
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        
        // 添加鼠标交互
        setupMouseInteraction();
        
        // 启动更新定时器
        startUpdateTimer();
        
        // 初始化节点数据
        initializeNodes();
        
        System.out.println("[3D Map] Sky-Ground 3D visualization initialized");
    }
    
    /**
     * 设置鼠标交互
     */
    private void setupMouseInteraction() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                lastMousePoint = null;
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // 双击重置视角
                    resetView();
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    int deltaX = e.getX() - lastMousePoint.x;
                    int deltaY = e.getY() - lastMousePoint.y;
                    
                    viewAngleY += deltaX * 0.5;
                    viewAngleX += deltaY * 0.5;
                    
                    // 限制旋转角度
                    viewAngleX = Math.max(-89, Math.min(89, viewAngleX));
                    
                    lastMousePoint = e.getPoint();
                    repaint();
                }
            }
        });
        
        addMouseWheelListener(e -> {
            double scaleFactor = 1.1;
            if (e.getWheelRotation() < 0) {
                zoom *= scaleFactor;
            } else {
                zoom /= scaleFactor;
            }
            zoom = Math.max(0.1, Math.min(5.0, zoom));
            repaint();
        });
    }
    
    /**
     * 重置视角
     */
    private void resetView() {
        viewAngleX = 25.0;
        viewAngleY = 45.0;
        zoom = 1.0;
        repaint();
    }
    
    /**
     * 启动更新定时器
     */
    private void startUpdateTimer() {
        updateTimer = new Timer(100, e -> {
            updateSimulationData();
            updateAnimations();
            repaint();
        });
        updateTimer.start();
    }
    
    /**
     * 初始化节点数据
     */
    private void initializeNodes() {
        nodeList.clear();
        
        // 从仿真管理器获取节点数据
        if (simulationManager != null && simulationManager.getDataCentersManager() != null) {
            ComputingNodesGenerator computingNodesGenerator = simulationManager.getDataCentersManager().getComputingNodesGenerator();
            
            // 处理边缘设备 (地面传感器)
            for (ComputingNode node : computingNodesGenerator.getMistOnlyList()) {
                if (node instanceof SkyGroundComputingNode) {
                    SkyGroundComputingNode skyGroundNode = (SkyGroundComputingNode) node;
                    double x = node.getMobilityModel().getCurrentLocation().getXPos();
                    double y = node.getMobilityModel().getCurrentLocation().getYPos();
                    
                    Node3D node3D = new Node3D(
                        "Sensor_" + node.getId(),
                        SkyGroundComputingNode.SkyGroundNodeType.GROUND_SENSOR,
                        x, y, GROUND_ALTITUDE
                    );
                    nodeList.add(node3D);
                }
            }
            
            // 处理边缘数据中心 (基站、无人机、卫星)
            for (ComputingNode node : computingNodesGenerator.getEdgeOnlyList()) {
                if (node instanceof SkyGroundComputingNode) {
                    SkyGroundComputingNode skyGroundNode = (SkyGroundComputingNode) node;
                    double x = node.getMobilityModel().getCurrentLocation().getXPos();
                    double y = node.getMobilityModel().getCurrentLocation().getYPos();
                    
                    Node3D node3D = new Node3D(
                        skyGroundNode.getNodeType().name() + "_" + node.getId(),
                        skyGroundNode.getNodeType(),
                        x, y, 0 // Z coordinate will be set by setColorByType()
                    );
                    nodeList.add(node3D);
                }
            }
            
            // 处理云服务器
            for (ComputingNode node : computingNodesGenerator.getCloudOnlyList()) {
                double x = SimulationParameters.simulationMapWidth / 2.0;
                double y = SimulationParameters.simulationMapLength / 2.0;
                
                Node3D node3D = new Node3D(
                    "Cloud_" + node.getId(),
                    SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER,
                    x, y, CLOUD_ALTITUDE
                );
                nodeList.add(node3D);
            }
        } else {
            // 创建演示数据
            createDemoNodes();
        }
        
        // 创建连接
        createConnections();
        
        System.out.println("[3D Map] Initialized " + nodeList.size() + " nodes");
    }
    
    /**
     * Create demo nodes with realistic sky-ground layer distribution
     */
    private void createDemoNodes() {
        double mapWidth = 1000.0;
        double mapHeight = 1000.0;
        
        // Ground sensors (15 nodes) - Distributed across ground level
        for (int i = 0; i < 15; i++) {
            double x = random.nextDouble() * mapWidth;
            double y = random.nextDouble() * mapHeight;
            double z = GROUND_ALTITUDE + (random.nextDouble() - 0.5) * 2; // Tiny random offset to avoid overlap
            Node3D sensor = new Node3D("Sensor_" + i, 
                SkyGroundComputingNode.SkyGroundNodeType.GROUND_SENSOR, x, y, z);
            nodeList.add(sensor);
        }
        
        // Base stations (5 nodes) - Strategic ground positions  
        for (int i = 0; i < 5; i++) {
            double x = (i + 1) * mapWidth / 6.0;
            double y = mapHeight / 2.0 + (random.nextDouble() - 0.5) * 200;
            double z = BASE_STATION_ALTITUDE + (random.nextDouble() - 0.5) * 2; // Same ground level
            Node3D baseStation = new Node3D("BS_" + i,
                SkyGroundComputingNode.SkyGroundNodeType.BASE_STATION, x, y, z);
            nodeList.add(baseStation);
        }
        
        // Cloud servers (2 nodes) - Ground data centers
        for (int i = 0; i < 2; i++) {
            double x = mapWidth / 2.0 + (i - 0.5) * 150;
            double y = mapHeight / 2.0 + (i - 0.5) * 100;
            double z = CLOUD_ALTITUDE + (random.nextDouble() - 0.5) * 2; // Same ground level
            Node3D cloud = new Node3D("Cloud_" + i,
                SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER, x, y, z);
            nodeList.add(cloud);
        }
        
        // UAVs (6 nodes) - Aerial layer with movement patterns
        for (int i = 0; i < 6; i++) {
            double x = random.nextDouble() * mapWidth;
            double y = random.nextDouble() * mapHeight;
            double z = UAV_ALTITUDE + (random.nextDouble() - 0.5) * 20; // Slight altitude variation
            Node3D uav = new Node3D("UAV_" + i,
                SkyGroundComputingNode.SkyGroundNodeType.UAV, x, y, z);
            nodeList.add(uav);
        }
        
        // LEO Satellites (3 nodes) - Space layer
        for (int i = 0; i < 3; i++) {
            double x = (i + 1) * mapWidth / 4.0;
            double y = mapHeight / 2.0 + (i - 1) * 200;
            double z = SATELLITE_ALTITUDE + (random.nextDouble() - 0.5) * 50; // Orbital variation
            Node3D satellite = new Node3D("SAT_" + i,
                SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE, x, y, z);
            nodeList.add(satellite);
        }
    }
    
    /**
     * 创建节点间连接
     */
    private void createConnections() {
        connectionList.clear();
        
        // 基于距离和层级关系创建连接
        for (Node3D node1 : nodeList) {
            for (Node3D node2 : nodeList) {
                if (node1 != node2 && shouldConnect(node1, node2)) {
                    connectionList.add(new Connection3D(node1, node2));
                }
            }
        }
        
        System.out.println("[3D Map] Created " + connectionList.size() + " connections");
    }
    
    /**
     * 判断两个节点是否应该连接
     */
    private boolean shouldConnect(Node3D node1, Node3D node2) {
        // 连接规则:
        // 1. 传感器连接到最近的基站或无人机
        // 2. 基站/无人机连接到卫星或云服务器
        // 3. 卫星连接到云服务器
        
        double distance2D = Math.sqrt(
            Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2)
        );
        
        // 距离阈值
        double maxDistance = 300.0;
        
        if (distance2D > maxDistance) return false;
        
        // 根据节点类型判断连接关系
        if (node1.type == SkyGroundComputingNode.SkyGroundNodeType.GROUND_SENSOR) {
            return node2.type == SkyGroundComputingNode.SkyGroundNodeType.BASE_STATION ||
                   node2.type == SkyGroundComputingNode.SkyGroundNodeType.UAV;
        }
        
        if (node1.type == SkyGroundComputingNode.SkyGroundNodeType.BASE_STATION ||
            node1.type == SkyGroundComputingNode.SkyGroundNodeType.UAV) {
            return node2.type == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE ||
                   node2.type == SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER;
        }
        
        if (node1.type == SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE) {
            return node2.type == SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER;
        }
        
        return false;
    }
    
    /**
     * 更新仿真数据
     */
    private void updateSimulationData() {
        long currentTime = System.currentTimeMillis();
        
        // Update node status and performance metrics with realistic patterns
        for (Node3D node : nodeList) {
            NodeMetrics metrics = nodeMetrics.computeIfAbsent(node.id, k -> new NodeMetrics());
            
            // Update metrics every second with realistic patterns
            if (currentTime - metrics.lastUpdate > 1000) {
                double targetCpu;
                
                switch (node.type) {
                    case GROUND_SENSOR:
                        // Sensors have periodic spikes for data collection
                        targetCpu = (currentTime % 10000 < 2000) ? 
                            60 + random.nextDouble() * 30 : 15 + random.nextDouble() * 25;
                        break;
                    case BASE_STATION:
                        // Base stations have steady moderate load with occasional peaks
                        targetCpu = 35 + random.nextDouble() * 45 + (random.nextDouble() < 0.1 ? 20 : 0);
                        break;
                    case UAV:
                        // UAVs have variable load based on mission status
                        boolean onMission = (currentTime % 15000) < 8000;
                        targetCpu = onMission ? 45 + random.nextDouble() * 35 : 15 + random.nextDouble() * 30;
                        break;
                    case LEO_SATELLITE:
                        // Satellites have high consistent load with orbital variations
                        double orbitalPhase = Math.sin(currentTime * 0.0001) * 0.5 + 0.5;
                        targetCpu = 55 + orbitalPhase * 25 + random.nextDouble() * 15;
                        break;
                    case CLOUD_SERVER:
                        // Cloud servers have high load with business hour patterns
                        double hourFactor = Math.sin((currentTime * 0.0002) % (2 * Math.PI)) * 0.3 + 0.7;
                        targetCpu = 45 + hourFactor * 35 + random.nextDouble() * 15;
                        break;
                    default:
                        targetCpu = 30 + random.nextDouble() * 40;
                }
                
                // Smooth transition to target CPU (persistence)
                metrics.cpuUtilization = metrics.cpuUtilization * 0.7 + targetCpu * 0.3;
                metrics.cpuUtilization = Math.max(0, Math.min(100, metrics.cpuUtilization));
                
                // Update other metrics
                metrics.memoryUsage = Math.max(30, metrics.cpuUtilization * 0.8 + random.nextDouble() * 20);
                metrics.networkUtilization = metrics.cpuUtilization * 0.6 + random.nextDouble() * 30;
                metrics.activeTasks = (int)(metrics.cpuUtilization / 20) + random.nextInt(3);
                metrics.lastUpdate = currentTime;
                
                // Update node status based on CPU utilization
                if (metrics.cpuUtilization > 70) {
                    node.status = NodeStatus.ACTIVE;
                } else if (metrics.cpuUtilization > 10) {
                    node.status = NodeStatus.IDLE;
                } else {
                    node.status = NodeStatus.DEAD;
                }
                
                node.cpuUtilization = metrics.cpuUtilization;
            }
        }
        
        // Update connection status with realistic patterns
        for (Connection3D conn : connectionList) {
            // Connection activity depends on both nodes being active
            boolean bothActive = conn.fromNode.status == NodeStatus.ACTIVE && 
                               conn.toNode.status == NodeStatus.ACTIVE;
            
            if (random.nextDouble() < 0.3) { // 30% chance to update
                if (bothActive) {
                    conn.isActive = random.nextDouble() < 0.75; // Higher chance if both nodes active
                } else {
                    conn.isActive = random.nextDouble() < 0.2;  // Lower chance otherwise
                }
                
                conn.connectionColor = conn.isActive ? ACTIVE_CONNECTION : IDLE_CONNECTION;
                
                // Update bandwidth based on activity
                if (conn.isActive) {
                    conn.bandwidth = 50 + random.nextDouble() * 200; // 50-250 Mbps
                } else {
                    conn.bandwidth = random.nextDouble() * 10; // 0-10 Mbps idle
                }
            }
        }
    }
    
    /**
     * 更新动画效果
     */
    private void updateAnimations() {
        animationTime += 50; // 50ms interval
        
        // 更新连接动画相位
        for (Connection3D conn : connectionList) {
            conn.animationPhase += 0.1;
            if (conn.animationPhase > 2 * Math.PI) {
                conn.animationPhase -= 2 * Math.PI;
            }
        }
        
        // 更新UAV位置 (简单的圆周运动)
        for (Node3D node : nodeList) {
            if (node.type == SkyGroundComputingNode.SkyGroundNodeType.UAV) {
                double time = animationTime * 0.001;
                double radius = 50.0;
                double centerX = 500.0;
                double centerY = 500.0;
                
                node.x = centerX + radius * Math.cos(time + node.id.hashCode());
                node.y = centerY + radius * Math.sin(time + node.id.hashCode());
            }
        }
    }
    
    /**
     * Enhanced 3D to 2D projection for better layer visualization
     */
    private Point2D project3D(double x, double y, double z) {
        // Enhanced 3D projection algorithm with better layer separation
        double angleXRad = Math.toRadians(viewAngleX);
        double angleYRad = Math.toRadians(viewAngleY);
        
        // Center the coordinates for better rotation
        double centerX = 500.0; // Map center X
        double centerY = 500.0; // Map center Y
        double relativeX = x - centerX;
        double relativeY = y - centerY;
        
        // Rotation transformation
        double cosX = Math.cos(angleXRad);
        double sinX = Math.sin(angleXRad);
        double cosY = Math.cos(angleYRad);
        double sinY = Math.sin(angleYRad);
        
        // Apply rotation matrix with enhanced Z-axis scaling
        double x1 = relativeX * cosY - z * sinY * 0.8; // Reduce Z distortion
        double y1 = relativeX * sinY * sinX + relativeY * cosX + z * cosY * sinX * 0.8;
        double z1 = relativeX * sinY * cosX - relativeY * sinX + z * cosY * cosX * 0.8;
        
        // Enhanced perspective projection
        double distance = 1500.0; // Increased distance for better perspective
        double scale = distance / (distance + z1 + 200); // Adjusted for better layer visibility
        
        // Convert to screen coordinates with enhanced scaling
        double screenX = getWidth() / 2.0 + x1 * scale * zoom * 0.6;
        double screenY = getHeight() / 2.0 - y1 * scale * zoom * 0.6;
        
        return new Point2D.Double(screenX, screenY);
    }
    
    /**
     * 绘制组件
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制背景
        drawBackground(g2d);
        
        // 更新所有节点的投影坐标
        updateProjections();
        
        // 绘制连接线
        drawConnections(g2d);
        
        // 绘制节点
        drawNodes(g2d);
        
        // 绘制图例和信息
        drawLegend(g2d);
        drawInfo(g2d);
    }
    
    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g2d) {
        // 空间背景渐变
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0, 0, 30),           // 深蓝色 (顶部)
            0, getHeight(), new Color(0, 0, 80)  // 蓝色 (底部)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制星空背景
        g2d.setColor(new Color(255, 255, 255, 100));
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight() / 2);
            g2d.fillOval(x, y, 2, 2);
        }
    }
    
    /**
     * 更新节点投影坐标
     */
    private void updateProjections() {
        for (Node3D node : nodeList) {
            Point2D projected = project3D(node.x, node.y, node.z);
            node.projectedX = projected.getX();
            node.projectedY = projected.getY();
            
            // 判断节点是否在可视范围内
            node.isVisible = projected.getX() >= -50 && projected.getX() <= getWidth() + 50 &&
                           projected.getY() >= -50 && projected.getY() <= getHeight() + 50;
        }
    }
    
    /**
     * 绘制连接线
     */
    private void drawConnections(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        for (Connection3D conn : connectionList) {
            if (!conn.fromNode.isVisible || !conn.toNode.isVisible) continue;
            
            g2d.setColor(conn.connectionColor);
            
            int x1 = (int) conn.fromNode.projectedX;
            int y1 = (int) conn.fromNode.projectedY;
            int x2 = (int) conn.toNode.projectedX;
            int y2 = (int) conn.toNode.projectedY;
            
            g2d.drawLine(x1, y1, x2, y2);
            
            // 绘制数据流动画
            if (conn.isActive) {
                drawDataFlow(g2d, x1, y1, x2, y2, conn.animationPhase);
            }
        }
    }
    
    /**
     * 绘制数据流动画
     */
    private void drawDataFlow(Graphics2D g2d, int x1, int y1, int x2, int y2, double phase) {
        g2d.setColor(DATA_FLOW);
        
        // 计算数据包位置
        double t = (Math.sin(phase) + 1) / 2; // 0到1之间的值
        int packetX = (int) (x1 + (x2 - x1) * t);
        int packetY = (int) (y1 + (y2 - y1) * t);
        
        // 绘制数据包
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(packetX - 2, packetY - 2, 4, 4);
    }
    
    /**
     * 绘制节点
     */
    private void drawNodes(Graphics2D g2d) {
        // 按Z坐标排序，远的先画
        List<Node3D> sortedNodes = new ArrayList<>(nodeList);
        sortedNodes.sort((a, b) -> Double.compare(b.z, a.z));
        
        for (Node3D node : sortedNodes) {
            if (!node.isVisible) continue;
            
            int x = (int) node.projectedX;
            int y = (int) node.projectedY;
            int size = node.nodeSize;
            
            // 根据高度调整节点大小
            double sizeScale = Math.max(0.5, 1.0 - node.z / 600.0);
            size = (int) (size * sizeScale);
            
            // 绘制节点阴影
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(x - size/2 + 2, y - size/2 + 2, size, size);
            
            // 绘制节点主体
            g2d.setColor(node.color);
            g2d.fillOval(x - size/2, y - size/2, size, size);
            
            // 绘制状态指示器
            g2d.setColor(node.status.getColor());
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(x - size/2, y - size/2, size, size);
            
            // 绘制CPU利用率指示器
            drawCpuIndicator(g2d, x + size/2 + 5, y, node.cpuUtilization, size/2);
            
            // 绘制节点标签
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            String label = node.id.split("_")[0]; // 只显示类型
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, x - labelWidth/2, y - size/2 - 5);
        }
    }
    
    /**
     * 绘制CPU利用率指示器
     */
    private void drawCpuIndicator(Graphics2D g2d, int x, int y, double cpuUtilization, int height) {
        int barWidth = 4;
        int barHeight = height;
        
        // 背景
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x, y - barHeight/2, barWidth, barHeight);
        
        // CPU利用率条
        int fillHeight = (int) (barHeight * cpuUtilization / 100.0);
        Color cpuColor;
        if (cpuUtilization > 80) {
            cpuColor = Color.RED;
        } else if (cpuUtilization > 50) {
            cpuColor = Color.YELLOW;
        } else {
            cpuColor = Color.GREEN;
        }
        
        g2d.setColor(cpuColor);
        g2d.fillRect(x, y + barHeight/2 - fillHeight, barWidth, fillHeight);
        
        // 边框
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(x, y - barHeight/2, barWidth, barHeight);
    }
    
    /**
     * Draw legend with proper encoding
     */
    private void drawLegend(Graphics2D g2d) {
        int legendX = 10;
        int legendY = 10;
        int itemHeight = 20;
        
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Node Types:", legendX, legendY + 15);
        
        String[] nodeTypes = {"Ground Sensors", "Base Stations", "UAVs", "Satellites", "Cloud Servers"};
        Color[] nodeColors = {SENSOR_COLOR, BASE_STATION_COLOR, UAV_COLOR, SATELLITE_COLOR, CLOUD_COLOR};
        
        for (int i = 0; i < nodeTypes.length; i++) {
            int y = legendY + (i + 2) * itemHeight;
            
            // Draw color square
            g2d.setColor(nodeColors[i]);
            g2d.fillOval(legendX, y, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(legendX, y, 12, 12);
            
            // Draw label
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(nodeTypes[i], legendX + 18, y + 9);
        }
    }
    
    /**
     * Draw info panel with node performance metrics
     */
    private void drawInfo(Graphics2D g2d) {
        int infoX = getWidth() - 240;
        int infoY = 10;
        
        // Background panel - larger to accommodate more info
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(infoX - 10, infoY, 230, 320, 10, 10);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("Sky-Ground Network Status", infoX, infoY + 15);
        
        // Calculate performance statistics
        Map<SkyGroundComputingNode.SkyGroundNodeType, Double> avgCpuByType = new HashMap<>();
        Map<SkyGroundComputingNode.SkyGroundNodeType, Integer> nodeCountByType = new HashMap<>();
        Map<SkyGroundComputingNode.SkyGroundNodeType, Integer> activeNodesByType = new HashMap<>();
        
        for (Node3D node : nodeList) {
            avgCpuByType.put(node.type, avgCpuByType.getOrDefault(node.type, 0.0) + node.cpuUtilization);
            nodeCountByType.put(node.type, nodeCountByType.getOrDefault(node.type, 0) + 1);
            if (node.status == NodeStatus.ACTIVE) {
                activeNodesByType.put(node.type, activeNodesByType.getOrDefault(node.type, 0) + 1);
            }
        }
        
        // Calculate averages
        for (SkyGroundComputingNode.SkyGroundNodeType type : avgCpuByType.keySet()) {
            int count = nodeCountByType.get(type);
            if (count > 0) {
                avgCpuByType.put(type, avgCpuByType.get(type) / count);
            }
        }
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        int lineY = infoY + 35;
        
        // 3D View Controls
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("3D View Controls:", infoX, lineY);
        lineY += 12;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        String[] controls = {
            "• Drag: Rotate view",
            "• Scroll: Zoom",
            "• Double-click: Reset view"
        };
        for (String control : controls) {
            g2d.drawString(control, infoX, lineY);
            lineY += 10;
        }
        lineY += 5;
        
        // View Parameters
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("View Parameters:", infoX, lineY);
        lineY += 12;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        String[] viewParams = {
            "X-axis: " + String.format("%.1f°", viewAngleX),
            "Y-axis: " + String.format("%.1f°", viewAngleY),
            "Zoom: " + String.format("%.2f", zoom)
        };
        for (String param : viewParams) {
            g2d.drawString(param, infoX, lineY);
            lineY += 10;
        }
        lineY += 5;
        
        // Layer Architecture
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Sky-Ground Layers:", infoX, lineY);
        lineY += 12;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        String[] layers = {
            "🛰️ Satellites: 400m (Space)",
            "✈️ UAVs: 150m (Aerial)", 
            "🏢 Base Stations: 0m (Ground)",
            "📱 Sensors: 0m (Ground)",
            "☁️ Cloud: 0m (Ground)"
        };
        for (String layer : layers) {
            g2d.drawString(layer, infoX, lineY);
            lineY += 10;
        }
        lineY += 5;
        
        // Performance Metrics
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Node Performance Metrics:", infoX, lineY);
        lineY += 12;
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        
        // Display metrics for each node type
        SkyGroundComputingNode.SkyGroundNodeType[] types = {
            SkyGroundComputingNode.SkyGroundNodeType.GROUND_SENSOR,
            SkyGroundComputingNode.SkyGroundNodeType.BASE_STATION,
            SkyGroundComputingNode.SkyGroundNodeType.UAV,
            SkyGroundComputingNode.SkyGroundNodeType.LEO_SATELLITE,
            SkyGroundComputingNode.SkyGroundNodeType.CLOUD_SERVER
        };
        
        String[] typeNames = {"Sensors", "Base Stations", "UAVs", "Satellites", "Cloud"};
        Color[] typeColors = {SENSOR_COLOR, BASE_STATION_COLOR, UAV_COLOR, SATELLITE_COLOR, CLOUD_COLOR};
        
        for (int i = 0; i < types.length; i++) {
            SkyGroundComputingNode.SkyGroundNodeType type = types[i];
            int totalNodes = nodeCountByType.getOrDefault(type, 0);
            int activeNodes = activeNodesByType.getOrDefault(type, 0);
            double avgCpu = avgCpuByType.getOrDefault(type, 0.0);
            
            if (totalNodes > 0) {
                g2d.setColor(typeColors[i]);
                g2d.fillOval(infoX, lineY - 8, 8, 8);
                
                g2d.setColor(Color.WHITE);
                String metrics = String.format("%s: %d/%d active", typeNames[i], activeNodes, totalNodes);
                g2d.drawString(metrics, infoX + 12, lineY);
                lineY += 10;
                
                g2d.setColor(Color.LIGHT_GRAY);
                String cpuInfo = String.format("  Avg CPU: %.1f%%", avgCpu);
                g2d.drawString(cpuInfo, infoX + 12, lineY);
                lineY += 10;
                
                // CPU utilization bar
                int barWidth = 100;
                int barHeight = 6;
                g2d.setColor(new Color(50, 50, 50));
                g2d.fillRect(infoX + 12, lineY - 5, barWidth, barHeight);
                
                int fillWidth = (int)(barWidth * avgCpu / 100.0);
                Color cpuColor = avgCpu > 80 ? Color.RED : avgCpu > 50 ? Color.YELLOW : Color.GREEN;
                g2d.setColor(cpuColor);
                g2d.fillRect(infoX + 12, lineY - 5, fillWidth, barHeight);
                
                g2d.setColor(Color.WHITE);
                g2d.drawRect(infoX + 12, lineY - 5, barWidth, barHeight);
                
                lineY += 12;
            }
        }
        
        // Network Statistics
        lineY += 5;
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("Network Statistics:", infoX, lineY);
        lineY += 12;
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        
        int totalConnections = connectionList.size();
        long activeConnections = connectionList.stream().mapToLong(conn -> conn.isActive ? 1 : 0).sum();
        double networkUtilization = totalConnections > 0 ? (double)activeConnections / totalConnections * 100 : 0;
        
        String[] networkStats = {
            "Total Nodes: " + nodeList.size(),
            "Total Links: " + totalConnections,
            "Active Links: " + activeConnections,
            "Network Load: " + String.format("%.1f%%", networkUtilization),
            "Data Flow: " + (activeConnections > 0 ? "Active" : "Idle")
        };
        
        for (String stat : networkStats) {
            g2d.drawString(stat, infoX, lineY);
            lineY += 10;
        }
        
        // Network utilization bar
        if (totalConnections > 0) {
            lineY += 5;
            int barWidth = 120;
            int barHeight = 8;
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(infoX, lineY - 5, barWidth, barHeight);
            
            int fillWidth = (int)(barWidth * networkUtilization / 100.0);
            Color netColor = networkUtilization > 80 ? Color.RED : networkUtilization > 50 ? Color.ORANGE : Color.GREEN;
            g2d.setColor(netColor);
            g2d.fillRect(infoX, lineY - 5, fillWidth, barHeight);
            
            g2d.setColor(Color.WHITE);
            g2d.drawRect(infoX, lineY - 5, barWidth, barHeight);
            g2d.setFont(new Font("Arial", Font.PLAIN, 8));
            g2d.drawString("Network Utilization", infoX + barWidth + 5, lineY);
        }
    }
    
    /**
     * 停止定时器
     */
    public void stopTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    
    /**
     * 更新仿真状态
     */
    public void updateSimulation() {
        initializeNodes();
        repaint();
    }
} 