package com.signalflow.engine;

import com.signalflow.model.Block;
import com.signalflow.model.BlockGraph;

/**
 * Core simulation engine that drives the block graph forward in time.
 * Delegates all topological ordering and signal propagation to BlockGraph.
 */
public class SimulationEngine {

    private final BlockGraph graph;
    private boolean running = false;
    private double simulationTime = 0.0;

    public SimulationEngine(BlockGraph graph) {
        this.graph = graph;
    }

    /**
     * Advances the simulation by dt seconds if the engine is running.
     */
    public void tick(double dt) {
        if (running) {
            graph.tick(dt);
            simulationTime += dt;
        }
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    /**
     * Full reset: stops the engine, zeroes the clock, and resets every block
     * in the graph back to its initial state.
     */
    public void reset() {
        running = false;
        simulationTime = 0.0;
        for (Block block : graph.getBlocks()) {
            block.reset();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public BlockGraph getGraph() {
        return graph;
    }
}
