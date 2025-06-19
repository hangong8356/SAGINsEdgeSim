/**
 * 颜色和标记一致性测试类
 * 
 * 验证所有图表中的节点类型、网络连接和失败原因
 * 都使用统一的颜色和标记符号
 */
package examples;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ColorConsistencyTest {
    
    public static void main(String[] args) {
        System.out.println("🎨 颜色和标记一致性测试开始...");
        
        ColorConsistencyTest test = new ColorConsistencyTest();
        test.generateConsistencyTestCharts();
        
        System.out.println("✅ 颜色和标记一致性测试完成！");
        System.out.println("📊 查看生成的图表验证以下一致性：");
        System.out.println("   🟢 IoT Devices: 绿色 + 圆形");
        System.out.println("   🔵 Base Stations: 蓝色 + 正方形");
        System.out.println("   🔷 UAV Nodes: 青色 + 菱形");
        System.out.println("   🟠 Satellites: 橙色 + 三角形");
        System.out.println("   🔴 Cloud Servers: 红色 + 加号");
    }
    
    public void generateConsistencyTestCharts() {
        String outputPath = "PureEdgeSim/examples/SkyGround_output/Color_Consistency_Test/";
        
        try {
            Files.createDirectories(Paths.get(outputPath));
        } catch (IOException e) {
            System.err.println("Failed to create output directory: " + e.getMessage());
        }
        
        // 1. 节点类型一致性测试
        XYChart nodeChart = createNodeConsistencyChart();
        saveBitmap(nodeChart, outputPath, "Node_Type_Consistency_Test");
        
        // 2. 网络连接一致性测试
        XYChart networkChart = createNetworkConsistencyChart();
        saveBitmap(networkChart, outputPath, "Network_Connection_Consistency_Test");
        
        // 3. 失败原因一致性测试
        XYChart failureChart = createFailureConsistencyChart();
        saveBitmap(failureChart, outputPath, "Failure_Cause_Consistency_Test");
        
        // 4. 综合一致性展示
        XYChart comprehensiveChart = createComprehensiveConsistencyChart();
        saveBitmap(comprehensiveChart, outputPath, "Comprehensive_Consistency_Demo");
    }
    
    protected XYChart createNodeConsistencyChart() {
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .theme(ChartTheme.Matlab)
            .title("节点类型颜色和标记一致性测试")
            .xAxisTitle("测试数据点")
            .yAxisTitle("性能指标")
            .build();
            
        double[] x = {1, 2, 3, 4, 5};
        double[] iotData = {85, 87, 89, 91, 88};
        double[] bsData = {92, 94, 96, 98, 95};
        double[] uavData = {78, 80, 82, 84, 81};
        double[] satelliteData = {88, 90, 92, 94, 91};
        double[] cloudData = {95, 97, 99, 98, 96};
        
        // 使用标准化颜色和标记
        addStandardSeries(chart, "IoT Devices", x, iotData, Color.GREEN, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "Base Stations", x, bsData, Color.BLUE, SeriesMarkers.SQUARE);
        addStandardSeries(chart, "UAV Nodes", x, uavData, Color.CYAN, SeriesMarkers.DIAMOND);
        addStandardSeries(chart, "Satellites", x, satelliteData, Color.ORANGE, SeriesMarkers.TRIANGLE_UP);
        addStandardSeries(chart, "Cloud Servers", x, cloudData, Color.RED, SeriesMarkers.PLUS);
        
        return chart;
    }
    
    protected XYChart createNetworkConsistencyChart() {
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .theme(ChartTheme.Matlab)
            .title("网络连接颜色和标记一致性测试")
            .xAxisTitle("连接距离 (km)")
            .yAxisTitle("连接质量")
            .build();
            
        double[] distance = {1, 5, 10, 15, 20};
        double[] iotToUAV = {0.95, 0.88, 0.82, 0.75, 0.68};
        double[] iotToBS = {0.98, 0.92, 0.86, 0.80, 0.74};
        double[] uavToBS = {0.93, 0.86, 0.79, 0.72, 0.65};
        double[] uavToSat = {0.85, 0.78, 0.71, 0.64, 0.57};
        double[] bsToSat = {0.88, 0.81, 0.74, 0.67, 0.60};
        double[] satToCloud = {0.82, 0.75, 0.68, 0.61, 0.54};
        
        // 使用标准化网络连接颜色和标记
        addStandardSeries(chart, "IoT→UAV", distance, iotToUAV, Color.GREEN, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "IoT→Base Station", distance, iotToBS, Color.BLUE, SeriesMarkers.SQUARE);
        addStandardSeries(chart, "UAV→Base Station", distance, uavToBS, Color.CYAN, SeriesMarkers.DIAMOND);
        addStandardSeries(chart, "UAV→Satellite", distance, uavToSat, Color.ORANGE, SeriesMarkers.TRIANGLE_UP);
        addStandardSeries(chart, "Base Station→Satellite", distance, bsToSat, Color.RED, SeriesMarkers.PLUS);
        addStandardSeries(chart, "Satellite→Cloud Server", distance, satToCloud, Color.MAGENTA, SeriesMarkers.CROSS);
        
        return chart;
    }
    
    protected XYChart createFailureConsistencyChart() {
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .theme(ChartTheme.Matlab)
            .title("失败原因颜色和标记一致性测试")
            .xAxisTitle("系统负载 (%)")
            .yAxisTitle("失败率 (%)")
            .build();
            
        double[] load = {20, 40, 60, 80, 100};
        double[] latencyFailure = {2.1, 4.5, 7.8, 12.5, 18.9};
        double[] resourceFailure = {1.5, 3.2, 6.1, 10.8, 16.4};
        double[] mobilityFailure = {0.8, 1.9, 3.5, 6.2, 9.8};
        double[] deviceFailure = {0.6, 1.4, 2.8, 5.1, 8.3};
        
        // 使用标准化失败原因颜色和标记（都是圆形但颜色不同）
        addStandardSeries(chart, "Latency Timeout", load, latencyFailure, Color.RED, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "Resource Unavailable", load, resourceFailure, Color.ORANGE, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "Mobility Issues", load, mobilityFailure, Color.BLUE, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "Device Death", load, deviceFailure, Color.MAGENTA, SeriesMarkers.CIRCLE);
        
        return chart;
    }
    
    protected XYChart createComprehensiveConsistencyChart() {
        XYChart chart = new XYChartBuilder()
            .width(1000).height(700)
            .theme(ChartTheme.Matlab)
            .title("Sky-Ground IoT 系统综合颜色标记一致性展示")
            .xAxisTitle("时间或负载")
            .yAxisTitle("性能指标 (归一化)")
            .build();
            
        double[] x = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        
        // 节点性能数据（归一化到0-1范围）
        double[] iotPerf = {0.85, 0.87, 0.82, 0.79, 0.75, 0.71, 0.68, 0.64, 0.60, 0.56};
        double[] bsPerf = {0.95, 0.93, 0.91, 0.88, 0.85, 0.82, 0.78, 0.74, 0.70, 0.66};
        double[] uavPerf = {0.88, 0.86, 0.83, 0.80, 0.76, 0.72, 0.68, 0.63, 0.58, 0.53};
        double[] satPerf = {0.92, 0.90, 0.87, 0.84, 0.80, 0.76, 0.71, 0.66, 0.61, 0.56};
        double[] cloudPerf = {0.98, 0.97, 0.95, 0.93, 0.91, 0.88, 0.85, 0.82, 0.78, 0.74};
        
        // 展示所有标准化样式
        addStandardSeries(chart, "IoT Devices", x, iotPerf, Color.GREEN, SeriesMarkers.CIRCLE);
        addStandardSeries(chart, "Base Stations", x, bsPerf, Color.BLUE, SeriesMarkers.SQUARE);
        addStandardSeries(chart, "UAV Nodes", x, uavPerf, Color.CYAN, SeriesMarkers.DIAMOND);
        addStandardSeries(chart, "Satellites", x, satPerf, Color.ORANGE, SeriesMarkers.TRIANGLE_UP);
        addStandardSeries(chart, "Cloud Servers", x, cloudPerf, Color.RED, SeriesMarkers.PLUS);
        
        return chart;
    }
    
    protected void addStandardSeries(XYChart chart, String name, double[] x, double[] y, Color color, org.knowm.xchart.style.markers.Marker marker) {
        org.knowm.xchart.XYSeries series = chart.addSeries(name, x, y);
        series.setMarker(marker);
        series.setLineColor(color);
        series.setLineStyle(new java.awt.BasicStroke(2f));
    }
    
    protected void saveBitmap(XYChart chart, String folderPath, String fileName) {
        try {
            BitmapEncoder.saveBitmapWithDPI(chart, folderPath + fileName, BitmapFormat.PNG, 300);
            System.out.println("✅ 生成: " + fileName + ".png");
        } catch (IOException e) {
            System.err.println("❌ 保存图表失败: " + fileName + " - " + e.getMessage());
        }
    }
} 