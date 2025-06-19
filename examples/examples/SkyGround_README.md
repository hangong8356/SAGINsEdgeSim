# Sky-Ground Edge Computing Scenario

## 概述 (Overview)

这是一个基于PureEdgeSim框架开发的空天地一体化边缘计算仿真场景，模拟了包含地面传感器、无人机(UAV)、基站、低轨卫星(LEO)和云服务器的复杂异构计算环境。

This is a sky-ground integrated edge computing simulation scenario developed based on the PureEdgeSim framework, simulating a complex heterogeneous computing environment including ground sensors, UAVs, base stations, LEO satellites, and cloud servers.

## 场景特点 (Scenario Features)

### 1. 多类型计算节点 (Multi-type Computing Nodes)
- **地面传感器 (Ground Sensors)**: 资源受限设备，主要负责数据收集和任务生成
- **无人机 (UAVs)**: 移动边缘计算节点，具有中等计算能力和3D移动性
- **基站 (Base Stations)**: 地面固定边缘节点，提供稳定的计算服务
- **低轨卫星 (LEO Satellites)**: 空间边缘计算节点，覆盖范围大，按轨道运动
- **云服务器 (Cloud Servers)**: 集中式高性能计算资源

### 2. 智能任务调度 (Intelligent Task Scheduling)
- **SKY_GROUND_SMART算法**: 综合考虑延迟、计算能力、负载均衡和节点类型适应性
- **多因素评分机制**: 
  - 延迟得分 (40%)
  - 计算能力得分 (30%)  
  - 负载均衡得分 (20%)
  - 类型适应性得分 (10%)

### 3. 网络模型 (Network Model)
- **多层网络架构**: 支持卫星-地面、卫星间、无人机-地面等多种通信链路
- **动态链路质量**: 基于距离、高度差和移动性计算链路质量
- **差异化带宽配置**: 不同类型链路具有不同的带宽和延迟特性

### 4. 移动模型 (Mobility Model)
- **无人机3D移动**: 随机移动模式，考虑高度变化
- **卫星轨道运动**: 基于轨道力学的运动模式
- **地面节点**: 固定或车辆移动模式

## 文件结构 (File Structure)

```
PureEdgeSim/examples/
├── SkyGroundExample.java              # 主仿真类
├── SkyGroundComputingNode.java        # 自定义计算节点
├── SkyGroundOrchestrator.java         # 任务编排器
├── SkyGroundMobilityModel.java        # 移动模型
├── SkyGroundNetworkModel.java         # 网络模型
├── SkyGround_settings/                # 配置文件目录
│   ├── simulation_parameters.properties
│   ├── applications.xml
│   ├── edge_devices.xml
│   ├── edge_datacenters.xml
│   └── cloud.xml
├── SkyGround_output/                  # 输出目录
└── SkyGround_README.md               # 本文档
```

## 应用类型 (Application Types)

1. **IoT数据收集 (IoT_Data_Collection)**: 高频率传感器数据采集
2. **紧急响应 (Emergency_Response)**: 极低延迟的紧急任务
3. **无人机导航 (UAV_Navigation)**: 实时导航控制任务
4. **地球观测 (Earth_Observation)**: 卫星地球观测数据处理
5. **图像处理 (Image_Processing)**: 计算密集型图像识别任务
6. **重计算任务 (Heavy_Computation)**: 适合云端的批处理任务

## 设备配置 (Device Configuration)

### 边缘设备分布 (Edge Devices Distribution)
- 地面IoT传感器: 40%
- 无人机设备: 20%  
- 地面基站设备: 15%
- 移动地面车辆: 15%
- 高端地面计算节点: 10%

### 边缘数据中心 (Edge Datacenters)
- 4个分布式基站
- 1个中央指挥基站
- 3个LEO卫星节点

## 运行方法 (How to Run)

### 1. 编译项目
```bash
cd PureEdgeSim
javac -cp "lib/*:." examples/SkyGroundExample.java
```

### 2. 运行仿真
```bash
java -cp "lib/*:." examples.SkyGroundExample
```

### 3. 查看结果
仿真结果将保存在 `PureEdgeSim/examples/SkyGround_output/` 目录中。

## 核心算法 (Core Algorithms)

### 任务调度算法 (Task Scheduling Algorithm)
```java
// 节点适应性得分计算
score = latencyScore * 0.4 + 
        computingScore * 0.3 + 
        loadScore * 0.2 + 
        typeAdaptationScore * 0.1
```

### 链路质量计算 (Link Quality Calculation)
```java
quality = distanceFactor * altitudeFactor * mobilityFactor
```

## 性能指标 (Performance Metrics)

- **任务完成率**: 成功完成的任务比例
- **平均延迟**: 任务处理的平均延迟时间
- **能耗效率**: 不同类型节点的能耗表现
- **负载均衡**: 各节点的负载分布情况
- **网络利用率**: 不同链路类型的带宽利用率

## 参数调优 (Parameter Tuning)

### 仿真参数 (Simulation Parameters)
- `simulation_time`: 仿真时间长度
- `length/width`: 仿真区域大小
- `edge_datacenters_coverage`: 基站/卫星覆盖范围

### 网络参数 (Network Parameters)
- `wan_bandwidth`: 广域网带宽
- `cellular_bandwidth`: 蜂窝网络带宽
- `wifi_bandwidth`: WiFi网络带宽

### 算法参数 (Algorithm Parameters)
修改 `SkyGroundOrchestrator.java` 中的权重配置:
```java
score += latencyScore * 0.4;      // 延迟权重
score += computingScore * 0.3;    // 计算能力权重  
score += loadScore * 0.2;         // 负载均衡权重
score += typeScore * 0.1;         // 类型适应性权重
```

## 扩展开发 (Extension Development)

### 添加新的节点类型
1. 在 `SkyGroundNodeType` 枚举中添加新类型
2. 在 `initializeNodeCharacteristics()` 中定义特性
3. 更新调度算法中的类型适应性逻辑

### 实现新的调度算法
1. 在 `SkyGroundOrchestrator.java` 中添加新的算法实现
2. 在 `findComputingNode()` 方法中添加算法选择逻辑
3. 在配置文件中指定算法名称

### 自定义移动模式
1. 在 `SkyGroundMobilityModel.java` 中实现新的移动函数
2. 根据节点类型调用相应的移动函数

## 常见问题 (FAQ)

**Q: 如何调整卫星的轨道参数？**
A: 修改 `SkyGroundMobilityModel.java` 中的 `getSatelliteNextLocation()` 方法。

**Q: 如何添加新的应用类型？**
A: 在 `applications.xml` 中添加新的应用定义，并在调度算法中添加相应的处理逻辑。

**Q: 如何修改网络延迟模型？**
A: 修改 `SkyGroundComputingNode.java` 中的 `getCommLatencyTo()` 方法。

## 引用 (Citation)

如果您在研究中使用了此场景，请引用：

```
@misc{skyground_pureedgesim,
  title={Sky-Ground Edge Computing Simulation Scenario},
  author={Your Name},
  year={2024},
  note={Based on PureEdgeSim Framework}
}
```

## 联系信息 (Contact)

如有问题或建议，请联系开发团队。

---

**注意**: 此场景基于PureEdgeSim框架开发，需要确保PureEdgeSim的依赖库正确配置。 