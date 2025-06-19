# -ISCC-SAGINs-PureEdgeSim
# PureEdgeSim-SAGIN: Space-Air-Ground Integrated Network Edge Computing Simulator


## Project Overview

**PureEdgeSim-SAGIN** is an extension of [PureEdgeSim](https://github.com/CharafeddineMechalikh/PureEdgeSim) for simulating Space-Air-Ground Integrated Networks (SAGIN) with edge computing. It supports heterogeneous multi-layer nodes (sensors, UAVs, base stations, satellites, cloud servers) and is designed for research on multi-hop MEC, intelligent orchestration, resource allocation, and energy optimization.

## Key Features

- **Five-layer SAGIN architecture**: IoT sensors, UAVs, ground base stations, LEO satellites, and cloud servers
- **Multiple orchestration algorithms**: Q-Learning, DQN, SARL (Simulated Annealing RL), and traditional scheduling
- **Multi-hop routing & dynamic topology**: Supports UAV→Base Station→Satellite→Cloud multi-hop offloading and network changes
- **Energy & latency modeling**: Detailed modeling of node power, bandwidth, and computing capacity
- **Visualization & performance analysis**: Automatic generation of multi-dimensional performance charts and technical reports
- **Highly configurable**: All node parameters, network topology, and task models are customizable via XML/Properties files

## System Architecture

```mermaid
graph TD
  IoT[IoT Sensors] --> UAV[UAVs]
  UAV--> UAV[UAVs]
  UAV --> BS[Base Stations]
  BS--> BS[Base Stations]
  BS --> SAT[LEO Satellites]
  SAT --> CLOUD[Cloud Server]

## Node Configuration (from config files)

| Node Type   | Count | CPU/Cores | MIPS   | RAM(GB) | Storage(GB) | Battery/Power(Wh/W)   | Bandwidth(Mbps) | Config File         |
|-------------|-------|-----------|--------|---------|-------------|----------------------|-----------------|---------------------|
| Sensor      | 50    | 1         | 1200   | 0.5     | 1           | 50Wh, 0.01~0.5W      | 10              | edge_devices.xml    |
| UAV         | 20    | 4         | 30000  | 8       | 64          | 200Wh, 8~150W        | 100             | edge_devices.xml    |
| Base Station| 5     | 8         | 30000  | 32      | 500         | 50~200W              | 1000            | edge_datacenters.xml|
| Satellite   | 3     | 4         | 20000  | 16      | 128         | 30~100W              | 1000            | edge_datacenters.xml|
| Cloud       | 1     | 500       | 60000  | 128     | 10000       | 500~8000W            | 10000           | cloud.xml           |

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/PureEdgeSim-SAGIN.git
   cd PureEdgeSim-SAGIN
   ```

2. **Configure dependencies**
   - Run `download_dependencies.bat` or manually place required JARs in `PureEdgeSim/libs/`

3. **Customize simulation parameters**
   - Edit XML/Properties files in `PureEdgeSim/settings/` to adjust nodes, network, and tasks

4. **Compile and run**
   ```bash
   javac -cp "PureEdgeSim/libs/*;PureEdgeSim/" PureEdgeSim/examples/SkyGroundExampleSARL.java
   java -Xms4g -Xmx8g -cp "PureEdgeSim/libs/*;PureEdgeSim/" examples.SkyGroundExampleSARL --sarl
   ```

5. **Results & Visualization**
   - After simulation, multi-dimensional performance charts and technical reports are generated in `PureEdgeSim/examples/SkyGround_output/`

#####
The paper titled "Integrated Sensing, Communication, and Computation in SAGINs: Multi-hop Task Offloading via Deep Reinforcement Learning" is currently under submission. At present, only some of the functionalities are demonstrated, and all features will be fully released after the paper is accepted.
