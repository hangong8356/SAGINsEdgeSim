/**
 * Sky-Ground Example with Real-Time Visualization
 * 
 * 集成了天地一体化边缘计算仿真和实时可视化系统
 * 
 * @author Your Name
 */
package examples;

import com.mechalikh.pureedgesim.simulationmanager.Simulation;
import javax.swing.SwingUtilities;
import java.io.File;

/**
 * 天地一体化边缘计算仿真 + 实时可视化系统
 * 
 * 功能特性：
 * 1. 三维网络拓扑可视化
 * 2. 实时CPU利用率监控
 * 3. 任务成功率统计
 * 4. 节点类型性能分析
 * 5. 移动节点轨迹追踪
 */
public class SkyGroundExampleWithVisualization {
    
    // 配置路径
    private static String settingsPath = "PureEdgeSim/examples/SkyGround_settings/";
    private static String outputPath = "PureEdgeSim/examples/SkyGround_output/";
    
    // 可视化系统
    private static SkyGroundVisualization visualizationSystem;
    
    public SkyGroundExampleWithVisualization() {
        System.out.println("=== 天地一体化边缘计算仿真与可视化系统 ===");
        System.out.println("🚀 正在启动集成系统...");
        
        // 启动可视化系统
        startVisualizationSystem();
        
        // 延迟启动仿真，确保可视化系统完全加载
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 等待2秒
                launchSimulation();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 启动可视化系统
     */
    private void startVisualizationSystem() {
        SwingUtilities.invokeLater(() -> {
            visualizationSystem = new SkyGroundVisualization();
            visualizationSystem.setVisible(true);
            
            System.out.println("✅ 可视化系统启动完成");
            System.out.println("📊 实时监控界面已激活");
        });
    }
    
    /**
     * 启动仿真系统
     */
    private void launchSimulation() {
        try {
            System.out.println("🎯 正在初始化天地一体化仿真环境...");
            
            // 设置输出目录
            String currentTime = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String customOutput = outputPath + "SkyGround_Visualization_" + currentTime + "/";
            
            System.setProperty("custom_output_folder", customOutput);
            System.out.println("📁 输出目录: " + customOutput);
            
            // 创建仿真实例
            Simulation sim = new Simulation();
            sim.setCustomSettingsFolder(settingsPath);
            sim.setCustomOutputFolder(customOutput);
            
            // 配置天地一体化组件
            configureIntegratedSystem(sim);
            
            System.out.println("🧠 配置强化学习编排器...");
            
            // 设置自定义组件
            sim.setCustomEdgeOrchestrator(SkyGroundSARLOrchestrator.class);
            sim.setCustomComputingNode(SkyGroundComputingNode.class);
            sim.setCustomMobilityModel(SkyGroundMobilityModel.class);
            sim.setCustomNetworkModel(SkyGroundNetworkModel.class);
            
            System.out.println("✅ 自定义组件配置完成");
            
            // 启动仿真
            System.out.println("🎯 启动仿真引擎...");
            long startTime = System.currentTimeMillis();
            
            sim.launchSimulation();
            
            long endTime = System.currentTimeMillis();
            System.out.println("✅ 仿真完成！用时: " + (endTime - startTime) / 1000.0 + " 秒");
            
            // 生成最终报告
            generateIntegratedReports(customOutput);
            
        } catch (Exception e) {
            System.err.println("❌ 仿真执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 配置集成系统参数
     */
    private void configureIntegratedSystem(Simulation sim) {
        // SARL算法参数配置
        System.setProperty("sarl.initialTemperature", "12.0");
        System.setProperty("sarl.coolingRate", "0.92");
        System.setProperty("sarl.minTemperature", "0.001");
        
        // 奖励函数权重配置
        System.setProperty("sarl.rewardWeight.latency", "0.35");
        System.setProperty("sarl.rewardWeight.energy", "0.25");
        System.setProperty("sarl.rewardWeight.success", "0.3");
        System.setProperty("sarl.rewardWeight.loadBalance", "0.1");
        
        // 可视化集成配置
        System.setProperty("visualization.enabled", "true");
        System.setProperty("visualization.updateInterval", "1000");
        System.setProperty("visualization.detailLevel", "high");
        
        System.out.println("   ✓ SARL算法配置：温度策略优化");
        System.out.println("   ✓ 奖励权重：延迟35%, 能耗25%, 成功率30%, 负载均衡10%");
        System.out.println("   ✓ 可视化集成：实时更新间隔1秒");
    }
    
    /**
     * 生成集成报告
     */
    private void generateIntegratedReports(String outputFolder) {
        System.out.println("\n📊 生成天地一体化集成分析报告...");
        
        try {
            // 查找仿真结果文件
            String csvFileName = findSimulationResultsFile(outputFolder);
            
            if (csvFileName != null) {
                // 生成性能图表
                System.out.println("📈 生成性能分析图表...");
                SkyGroundChartsGenerator chartsGenerator = new SkyGroundChartsGenerator(csvFileName);
                chartsGenerator.generateAllCharts();
                
                // 生成详细性能分析
                System.out.println("📊 生成详细性能分析...");
                SkyGroundPerformanceAnalyzer performanceAnalyzer = new SkyGroundPerformanceAnalyzer(csvFileName);
                performanceAnalyzer.generateAllPerformanceCharts();
                
                System.out.println("✅ 集成报告生成完成！");
                
                // 打印报告摘要
                printReportSummary(outputFolder);
                
            } else {
                System.out.println("⚠️  未找到仿真结果文件，生成示例报告");
                createDemoReport(outputFolder);
            }
            
        } catch (Exception e) {
            System.err.println("❌ 报告生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 查找仿真结果文件
     */
    private String findSimulationResultsFile(String outputFolder) {
        File outputDir = new File(outputFolder);
        if (outputDir.exists()) {
            File[] csvFiles = outputDir.listFiles((dir, name) -> name.endsWith(".csv"));
            if (csvFiles != null && csvFiles.length > 0) {
                System.out.println("📄 找到仿真结果文件: " + csvFiles[0].getName());
                return csvFiles[0].getAbsolutePath();
            }
        }
        return null;
    }
    
    /**
     * 创建演示报告
     */
    private void createDemoReport(String outputFolder) {
        try {
            File demoFile = new File(outputFolder + "demo_visualization_results.csv");
            demoFile.getParentFile().mkdirs();
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(demoFile)) {
                // CSV头部
                writer.println("Architecture,Algorithm,Edge devices count,Tasks successfully executed," +
                    "Tasks failed (delay),Average waiting time (s),Average execution delay (s)," +
                    "Energy consumption (Wh),Network usage (s)");
                
                // 天地一体化SARL数据
                writer.println("EDGE_AND_CLOUD,SARL,15,14200,800,0.0018,0.0045,2841.3,18.67");
                writer.println("EDGE_AND_CLOUD,SARL,25,23800,1200,0.0021,0.0052,4235.7,28.43");
                writer.println("EDGE_AND_CLOUD,SARL,35,33600,1400,0.0024,0.0058,5892.1,35.21");
                
                // 传统算法对比
                writer.println("EDGE_AND_CLOUD,ROUND_ROBIN,15,9200,5800,0.0067,0.0156,3842.5,32.18");
                writer.println("EDGE_AND_CLOUD,TRADE_OFF,15,10800,4200,0.0048,0.0118,3456.2,26.75");
                writer.println("EDGE_AND_CLOUD,FUZZY_LOGIC,15,11500,3500,0.0041,0.0095,3234.8,24.12");
            }
            
            System.out.println("📝 演示报告创建完成: " + demoFile.getName());
            
        } catch (Exception e) {
            System.err.println("创建演示报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 打印报告摘要
     */
    private void printReportSummary(String outputFolder) {
        System.out.println("\n📋 天地一体化仿真与可视化集成报告:");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🗂️  报告位置: " + outputFolder);
        System.out.println();
        System.out.println("📊 主要分析模块:");
        System.out.println("├─ 实时可视化系统");
        System.out.println("│  ├─ 三维网络拓扑显示");
        System.out.println("│  ├─ 节点CPU利用率监控");
        System.out.println("│  ├─ 任务成功率统计");
        System.out.println("│  └─ 移动节点轨迹追踪");
        System.out.println("├─ SARL强化学习分析");
        System.out.println("│  ├─ 学习曲线收敛分析");
        System.out.println("│  ├─ 温度衰减策略效果");
        System.out.println("│  ├─ 奖励函数优化情况");
        System.out.println("│  └─ 决策质量演化过程");
        System.out.println("├─ 天地一体化网络分析");
        System.out.println("│  ├─ 空天地网络拓扑性能");
        System.out.println("│  ├─ 多跳路由效率分析");
        System.out.println("│  ├─ 链路质量评估");
        System.out.println("│  └─ 网络负载分布");
        System.out.println("├─ 节点类型性能对比");
        System.out.println("│  ├─ 地面传感器性能分析");
        System.out.println("│  ├─ 基站处理能力评估");
        System.out.println("│  ├─ 无人机移动计算效果");
        System.out.println("│  ├─ 卫星覆盖能力分析");
        System.out.println("│  └─ 云服务器处理效率");
        System.out.println("└─ 综合性能评估");
        System.out.println("   ├─ SARL vs 传统算法对比");
        System.out.println("   ├─ 天地一体化架构优势");
        System.out.println("   ├─ 能效比分析");
        System.out.println("   └─ 系统可扩展性评估");
        System.out.println();
        System.out.println("🎯 建议查看重点:");
        System.out.println("   1. 实时可视化界面 - 观察网络动态");
        System.out.println("   2. SARL学习收敛图 - 验证算法效果");
        System.out.println("   3. 节点类型对比图 - 分析架构优势");
        System.out.println("   4. 综合性能雷达图 - 整体评估");
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * 显示使用说明
     */
    private void displayUsageInstructions() {
        System.out.println("\n💡 系统使用说明:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🖥️  可视化界面操作:");
        System.out.println("   • 实时监控各类型节点的CPU利用率");
        System.out.println("   • 查看任务成功率统计");
        System.out.println("   • 观察移动节点(无人机、卫星)的位置变化");
        System.out.println("   • 分析不同节点类型的性能表现");
        System.out.println();
        System.out.println("📊 性能指标含义:");
        System.out.println("   • CPU利用率: 反映节点计算负载状况");
        System.out.println("     - 绿色(<30%): 负载较轻");
        System.out.println("     - 黄色(30-70%): 负载适中");
        System.out.println("     - 红色(>70%): 负载较重");
        System.out.println("   • 任务成功率: 反映任务处理效果");
        System.out.println("     - >90%: 优秀");
        System.out.println("     - 75-90%: 良好");
        System.out.println("     - <75%: 需要优化");
        System.out.println();
        System.out.println("🌐 节点类型特点:");
        System.out.println("   • 地面传感器: 任务生成源，CPU利用率较低");
        System.out.println("   • 基站: 固定边缘计算节点，处理能力强");
        System.out.println("   • 无人机: 移动边缘计算，适合实时任务");
        System.out.println("   • 低轨卫星: 空间计算节点，覆盖范围广");
        System.out.println("   • 云服务器: 中心化计算，处理复杂任务");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    public static void main(String[] args) {
        System.out.println("Sky-Ground Edge Computing Simulation with Real-Time Visualization");
        System.out.println("Based on PureEdgeSim Framework + SARL Reinforcement Learning");
        System.out.println("================================================================");
        
        // 显示内存信息
        long totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        System.out.println("💾 内存配置: " + totalMemory + "MB / " + maxMemory + "MB");
        
        // 启动集成系统
        SkyGroundExampleWithVisualization example = new SkyGroundExampleWithVisualization();
        
        // 显示使用说明
        example.displayUsageInstructions();
    }
} 