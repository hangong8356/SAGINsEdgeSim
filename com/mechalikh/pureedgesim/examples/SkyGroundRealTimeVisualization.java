/**
 * Sky-Ground Real-Time 3D Visualization System
 * 
 * 三维实时显示天地一体化边缘计算网络：
 * - 地面传感设备 (Ground Sensors)
 * - 基站 (Base Stations) 
 * - 无人机 (UAVs)
 * - 卫星 (Satellites)
 * - 云服务器 (Cloud Servers)
 * 
 * 实时监控指标：
 * - CPU利用率
 * - 任务成功率
 * - 网络连接状态
 * - 能耗情况
 */
package examples;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mechalikh.pureedgesim.simulationengine.OnSimulationStartListener;

public class SkyGroundRealTimeVisualization extends JFrame implements OnSimulationStartListener {
    
    private static final long serialVersionUID = 1L;
    
    // 3D View Parameters
    private double viewAngleX = 20.0;  // X轴旋转角度
    private double viewAngleY = 30.0;  // Y轴旋转角度
    private double zoom = 1.0;         // 缩放比例
    private Point lastMousePoint;      // 上次鼠标位置
    
    // Colors for different node types
    private static final Color SENSOR_COLOR = new Color(34, 139, 34);      // 森林绿 - 地面传感器
    private static final Color BASE_STATION_COLOR = new Color(65, 105, 225); // 皇家蓝 - 基站
    private static final Color UAV_COLOR = new Color(255, 140, 0);          // 深橙色 - 无人机
    private static final Color SATELLITE_COLOR = new Color(220, 20, 60);    // 深红色 - 卫星
    private static final Color CLOUD_COLOR = new Color(128, 0, 128);        // 紫色 - 云服务器
    
    // Visualization components
    private VisualizationPanel visualPanel;
    private JPanel statusPanel;
    private JPanel metricsPanel;
    private Timer updateTimer;
    
    // Node tracking
    private final List<NodeInfo> nodeList = new CopyOnWriteArrayList<>();
    private final Map<String, NodeMetrics> nodeMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<Double>> cpuHistory = new ConcurrentHashMap<>();
    
    // Performance metrics
    private volatile double averageCpuUtilization = 0.0;
    private volatile double taskSuccessRate = 0.0;
    private volatile int totalTasks = 0;
    private volatile int successfulTasks = 0;
    
    // Node type statistics
    private final Map<SkyGroundComputingNode.SkyGroundNodeType, NodeTypeStats> nodeTypeStats = 
        new ConcurrentHashMap<>();
    
    /**
     * 节点信息类
     */
    private static class NodeInfo {
        String id;
        SkyGroundComputingNode.SkyGroundNodeType type;
        double x, y, z;  // 3D坐标
        double cpuUtilization;
        boolean isActive;
        Color color;
        String status;
        
        NodeInfo(String id, SkyGroundComputingNode.SkyGroundNodeType type, 
                double x, double y, double z) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.cpuUtilization = 0.0;
            this.isActive = true;
            this.status = "Active";
            setColorByType();
        }
        
        private void setColorByType() {
            switch (type) {
                case GROUND_SENSOR:
                    color = SENSOR_COLOR;
                    break;
                case BASE_STATION:
                    color = BASE_STATION_COLOR;
                    break;
                case UAV:
                    color = UAV_COLOR;
                    break;
                case LEO_SATELLITE:
                    color = SATELLITE_COLOR;
                    break;
                case CLOUD_SERVER:
                    color = CLOUD_COLOR;
                    break;
                default:
                    color = Color.GRAY;
            }
        }
    }
    
    /**
     * 节点性能指标类
     */
    private static class NodeMetrics {
        double cpuUtilization;
        double memoryUsage;
        double energyConsumption;
        int tasksProcessed;
        int tasksSuccessful;
        long lastUpdateTime;
        
        NodeMetrics() {
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        double getSuccessRate() {
            return tasksProcessed > 0 ? (double) tasksSuccessful / tasksProcessed * 100.0 : 0.0;
        }
    }
    
    /**
     * 节点类型统计类
     */
    private static class NodeTypeStats {
        int nodeCount;
        double avgCpuUtilization;
        double avgSuccessRate;
        double totalEnergyConsumption;
        
        NodeTypeStats() {
            this.nodeCount = 0;
            this.avgCpuUtilization = 0.0;
            this.avgSuccessRate = 0.0;
            this.totalEnergyConsumption = 0.0;
        }
    }
    
    public SkyGroundRealTimeVisualization() {
        super("天地一体化边缘计算网络 - 三维实时可视化系统");
        initializeComponents();
        setupUI();
        startUpdateTimer();
        
        // 初始化节点类型统计
        for (SkyGroundComputingNode.SkyGroundNodeType type : 
             SkyGroundComputingNode.SkyGroundNodeType.values()) {
            nodeTypeStats.put(type, new NodeTypeStats());
        }
    }
    
    private void initializeComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        
        // 创建可视化面板
        visualPanel = new VisualizationPanel();
        visualPanel.setPreferredSize(new Dimension(1200, 800));
        visualPanel.setBackground(Color.BLACK);
        
        // 创建状态面板
        statusPanel = createStatusPanel();
        
        // 创建指标面板
        metricsPanel = createMetricsPanel();
    }
    
    private void setupUI() {
        // 主面板布局
        add(visualPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(metricsPanel, BorderLayout.EAST);
        
        // 添加鼠标交互
        addMouseInteraction();
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "系统状态", 0, 0, null, Color.WHITE));
        
        return panel;
    }
    
    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new Dimension(350, 600));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "性能指标监控", 0, 0, null, Color.WHITE));
        
        return panel;
    }
    
    private void addMouseInteraction() {
        MouseListener mouseListener = new MouseListener() {
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
                // 双击重置视角
                if (e.getClickCount() == 2) {
                    viewAngleX = 20.0;
                    viewAngleY = 30.0;
                    zoom = 1.0;
                    visualPanel.repaint();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        };
        
        MouseMotionListener mouseMotionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    int deltaX = e.getX() - lastMousePoint.x;
                    int deltaY = e.getY() - lastMousePoint.y;
                    
                    viewAngleY += deltaX * 0.5;
                    viewAngleX += deltaY * 0.5;
                    
                    // 限制旋转角度
                    viewAngleX = Math.max(-90, Math.min(90, viewAngleX));
                    
                    lastMousePoint = e.getPoint();
                    visualPanel.repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {}
        };
        
        visualPanel.addMouseListener(mouseListener);
        visualPanel.addMouseMotionListener(mouseMotionListener);
        
        // 添加鼠标滚轮缩放
        visualPanel.addMouseWheelListener(e -> {
            zoom *= (1.0 - e.getWheelRotation() * 0.1);
            zoom = Math.max(0.1, Math.min(5.0, zoom));
            visualPanel.repaint();
        });
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateVisualization();
                updateMetrics();
                visualPanel.repaint();
            }
        });
 