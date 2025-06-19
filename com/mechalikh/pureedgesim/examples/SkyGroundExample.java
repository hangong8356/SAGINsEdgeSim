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

import com.mechalikh.pureedgesim.simulationmanager.Simulation;

/**
 * Sky-Ground Edge Computing Scenario Example with Reinforcement Learning
 * 
 * 🌍 天地一体化边缘计算仿真 (Sky-Ground Integrated Edge Computing Simulation)
 * 
 * This example demonstrates a complex sky-ground integrated edge computing environment, including:
 * 1. 📱 Ground sensor devices - Generate various types of tasks (IoT data collection, environmental monitoring, image recognition, etc.)
 * 2. ✈️ UAV (Unmanned Aerial Vehicle) - Mobile edge computing nodes, providing aerial computing services
 * 3. 🏢 Base Station - Ground fixed edge nodes, covering specific areas
 * 4. 🛰️ LEO Satellite (Low Earth Orbit) - Space edge computing nodes, covering large areas
 * 5. ☁️ Cloud servers - Centralized high-performance computing resources
 * 
 * Task scheduling strategy:
 * - Low-latency tasks prioritize the nearest computing nodes (UAVs, base stations)
 * - Compute-intensive tasks consider satellites or cloud servers
 * - Consider network conditions, device load and energy consumption factors
 * 
 * Reinforcement Learning Integration:
 * - Uses SARL (Simulated Annealing Reinforcement Learning) algorithm for intelligent task orchestration
 * - Learns optimal task allocation policies over time with realistic constraints
 * - Balances latency, energy consumption, load distribution, and realistic failure modeling
 * 
 * 🎮 3D Visualization Available:
 * For interactive 3D visualization of the five-layer architecture, use:
 * → java examples.SkyGroundExample3D --realistic-sarl
 * 
 * Features 3D visualization:
 * - Interactive rotation and zoom
 * - Real-time data flow animation  
 * - Five-layer architecture display
 * - Connection status visualization
 * 
 * @author Your Name
 * @since PureEdgeSim Extension
 */
public class SkyGroundExample {
    
    // Settings file path
    private static String settingsPath = "PureEdgeSim/examples/SkyGround_settings/";
    
    // Output file path
    private static String outputPath = "PureEdgeSim/examples/SkyGround_output/";
    
    // Orchestrator selection modes
    private static String orchestratorMode = "realistic-sarl"; // default to realistic SARL
    
    public SkyGroundExample() {
        System.out.println("=== Starting Sky-Ground Edge Computing Simulation ===");
        System.out.println("Orchestrator Mode: " + orchestratorMode);
        
        // Create PureEdgeSim simulation instance
        Simulation sim = new Simulation();
        
        // Set custom output folder
        sim.setCustomOutputFolder(outputPath);
        
        // Set custom configuration folder
        sim.setCustomSettingsFolder(settingsPath);
        
        // Set custom computing node class for sky-ground nodes
        sim.setCustomComputingNode(SkyGroundComputingNode.class);
        System.out.println("[OK] Custom Sky-Ground Computing Nodes Loaded");
        
        // Set custom orchestrator based on mode
        switch (orchestratorMode) {
            case "realistic-sarl":
                sim.setCustomEdgeOrchestrator(SkyGroundRealisticSARLOrchestrator.class);
                System.out.println("[OK] Using Realistic SARL Orchestrator (Enhanced with uncertainty)");
                break;
            case "sarl":
                sim.setCustomEdgeOrchestrator(SkyGroundSARLOrchestrator.class);
                System.out.println("[OK] Using SARL Orchestrator (Simulated Annealing RL)");
                break;
            case "rl":
                sim.setCustomEdgeOrchestrator(SkyGroundRLOrchestrator.class);
                System.out.println("[OK] Using Q-Learning Orchestrator");
                break;
            case "traditional":
                sim.setCustomEdgeOrchestrator(SkyGroundOrchestrator.class);
                System.out.println("[OK] Using Traditional Sky-Ground Orchestrator");
                break;
            default:
                sim.setCustomEdgeOrchestrator(SkyGroundRealisticSARLOrchestrator.class);
                System.out.println("[OK] Using Default Realistic SARL Orchestrator");
                break;
        }
        
        // Set custom mobility model for UAVs and satellites
        sim.setCustomMobilityModel(SkyGroundMobilityModel.class);
        System.out.println("[OK] Custom Sky-Ground Mobility Model Loaded");
        
        // Set custom network model for sky-ground communications
        sim.setCustomNetworkModel(SkyGroundNetworkModel.class);
        System.out.println("[OK] Custom Sky-Ground Network Model Loaded");
        
        System.out.println("[OK] Configuration completed. Starting simulation...");
        System.out.println("Settings path: " + settingsPath);
        System.out.println("Output path: " + outputPath);
        
        // Launch simulation with timing
        long startTime = System.currentTimeMillis();
        sim.launchSimulation();
        long endTime = System.currentTimeMillis();
        
        System.out.println("[OK] Simulation completed in " + (endTime - startTime) / 1000.0 + " seconds");
        
        // Print orchestrator-specific statistics
        printOrchestratorStatistics();
        
        System.out.println("=== Sky-Ground Simulation Finished ===");
    }
    
    /**
     * Print orchestrator-specific statistics after simulation
     */
    private void printOrchestratorStatistics() {
        try {
            System.out.println("\n=== " + orchestratorMode.toUpperCase() + " Analysis ===");
            
            if (orchestratorMode.equals("realistic-sarl")) {
                System.out.println("Realistic SARL orchestrator was used during simulation.");
                System.out.println("Enhanced with real-world constraints: network congestion, device failures, resource limits.");
                
                System.out.println("\nRealistic SARL Features Applied:");
                System.out.println("- 🌍 Network congestion modeling");
                System.out.println("- 🔋 Device energy depletion (10% rate)");  
                System.out.println("- ❌ Real failure simulation (15% rate)");
                System.out.println("- 📡 Dynamic device availability");
                System.out.println("- 🌡️ Slow temperature decay (0.998)");
                
            } else if (orchestratorMode.equals("sarl")) {
                System.out.println("SARL (Simulated Annealing RL) orchestrator was used.");
                System.out.println("Fast temperature decay with basic uncertainty modeling.");
                
            } else if (orchestratorMode.equals("rl")) {
                System.out.println("Q-learning orchestrator was used during simulation.");
                System.out.println("Pure reinforcement learning without simulated annealing.");
                
            } else {
                System.out.println("Traditional orchestrator was used for baseline comparison.");
            }
            
            System.out.println("\nFor detailed analysis, check:");
            System.out.println("1. Simulation logs in: " + outputPath);
            System.out.println("2. Learning progress files (if applicable)");
            System.out.println("3. Performance CSV files");
            System.out.println("4. Real-time charts (if enabled)");
            
        } catch (Exception e) {
            System.out.println("Could not retrieve detailed statistics: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🌍 Sky-Ground Realistic SARL Edge Computing Simulator");
        System.out.println("Based on PureEdgeSim Framework with Enhanced Realism");
        System.out.println("========================================");
        
        // Parse command line arguments
        if (args.length > 0) {
            switch (args[0]) {
                case "--realistic-sarl":
                case "--realistic":
                    orchestratorMode = "realistic-sarl";
                    System.out.println("Command line: Using realistic SARL orchestrator");
                    break;
                case "--sarl":
                    orchestratorMode = "sarl";
                    System.out.println("Command line: Using basic SARL orchestrator");
                    break;
                case "--rl":
                    orchestratorMode = "rl";
                    System.out.println("Command line: Using Q-learning orchestrator");
                    break;
                case "--traditional":
                    orchestratorMode = "traditional";
                    System.out.println("Command line: Using traditional orchestrator");
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    return;
                default:
                    System.out.println("Unknown argument: " + args[0]);
                    printUsage();
                    return;
            }
        } else {
            System.out.println("No arguments provided. Using default realistic SARL mode.");
            System.out.println("🌍 Ready for enhanced realistic simulation!");
        }
        
        // Display memory and system information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
        System.out.println("Available memory: " + maxMemory + " MB");
        
        if (maxMemory < 3072) {
            System.out.println("WARNING: Low memory detected for realistic simulation!");
            System.out.println("  Recommended JVM options: -Xms3072m -Xmx6144m -XX:+UseG1GC");
        }
        
        try {
            new SkyGroundExample();
        } catch (Exception e) {
            System.err.println("Simulation failed with error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nTroubleshooting tips:");
            System.err.println("1. Check if all required files exist in settings folder");
            System.err.println("2. Verify JVM memory settings (recommended: 6GB)");
            System.err.println("3. Ensure SkyGroundRealisticSARLOrchestrator.java is compiled");
            System.err.println("4. Check realistic simulation parameters");
            System.err.println("5. Verify realistic_network_model=true in settings");
        }
    }
    
    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("\nUsage: java examples.SkyGroundExample [options]");
        System.out.println("Orchestrator Options:");
        System.out.println("  --realistic-sarl  Use realistic SARL with uncertainty modeling (default)");
        System.out.println("  --sarl           Use basic SARL orchestrator");
        System.out.println("  --rl             Use Q-learning orchestrator");
        System.out.println("  --traditional    Use traditional orchestrator");
        System.out.println("  --help, -h       Show this help message");
        System.out.println("\nRecommended JVM options for realistic simulation:");
        System.out.println("  -Xms3072m -Xmx6144m -XX:+UseG1GC -XX:MaxGCPauseMillis=200");
        System.out.println("\nExamples:");
        System.out.println("  java -Xms3072m -Xmx6144m examples.SkyGroundExample --realistic-sarl");
        System.out.println("  java examples.SkyGroundExample --traditional");
        System.out.println("\n🎮 3D Visualization Version:");
        System.out.println("  java -Xms2048m -Xmx4096m examples.SkyGroundExample3D --realistic-sarl");
        System.out.println("  Features: Interactive 3D view, real-time data flow, five-layer architecture");
        System.out.println("\nExpected Results with Realistic SARL:");
        System.out.println("  ✅ Success rate: 75-85% (not 99%+)");
        System.out.println("  ✅ Device failures: 5-15%");
        System.out.println("  ✅ Network congestion effects");
        System.out.println("  ✅ Gradual learning curve");
        System.out.println("  🎮 Interactive 3D visualization (in 3D version)");
    }
} 