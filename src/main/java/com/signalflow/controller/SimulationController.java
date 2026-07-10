package com.signalflow.controller;

import com.signalflow.engine.SimulationEngine;
import com.signalflow.view.BlockNode;
import com.signalflow.view.ClockBlockNode;
import com.signalflow.view.CosineBlockNode;
import com.signalflow.view.ScopeBlockNode;
import com.signalflow.view.SineBlockNode;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Bridges the {@link SimulationEngine} to the JavaFX render loop via an
 * {@link AnimationTimer}. Each frame, it ticks the engine and pushes the
 * latest model state into the view nodes.
 */
public class SimulationController {

    /** Maximum per-frame time step (seconds). Prevents huge jumps when the
     *  window is un-minimised or the system stalls. */
    private static final double MAX_DT = 0.05;

    private final SimulationEngine engine;
    private final WorkspaceController workspaceController;
    private final AnimationTimer timer;

    private long lastTimestamp = 0;

    private final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
    private final DoubleProperty simulationTimeProperty = new SimpleDoubleProperty(0.0);

    public SimulationController(SimulationEngine engine, WorkspaceController workspaceController) {
        this.engine = engine;
        this.workspaceController = workspaceController;

        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onFrame(now);
            }
        };
    }

    // ── Lifecycle ───────────────────────────────────────────────────────

    public void start() {
        engine.start();
        lastTimestamp = 0;
        timer.start();
        runningProperty.set(true);
    }

    public void stop() {
        engine.stop();
        timer.stop();
        runningProperty.set(false);
    }

    /**
     * Full reset: stops the timer, resets the engine, and refreshes every
     * visual block to reflect zeroed-out model state.
     */
    public void reset() {
        stop();
        engine.reset();

        // Push the zeroed model state into every view node
        for (BlockNode node : workspaceController.getBlockNodes()) {
            if (node instanceof ScopeBlockNode scopeNode) {
                scopeNode.updatePlot();
            } else if (node instanceof ClockBlockNode clockNode) {
                clockNode.updateDisplay();
            } else if (node instanceof SineBlockNode sineNode) {
                sineNode.updateDisplay();
            } else if (node instanceof CosineBlockNode cosineNode) {
                cosineNode.updateDisplay();
            }
        }

        runningProperty.set(false);
        simulationTimeProperty.set(0.0);
    }

    // ── Property accessors ──────────────────────────────────────────────

    public boolean isRunning() {
        return runningProperty.get();
    }

    public BooleanProperty runningProperty() {
        return runningProperty;
    }

    public DoubleProperty simulationTimeProperty() {
        return simulationTimeProperty;
    }

    // ── Per-frame callback ──────────────────────────────────────────────

    /**
     * Called once per JavaFX pulse (~60 Hz). Computes the real-time delta,
     * advances the engine, and refreshes all visual block displays.
     */
    private void onFrame(long now) {
        if (lastTimestamp == 0) {
            lastTimestamp = now;
            return;
        }

        double dt = (now - lastTimestamp) / 1_000_000_000.0;
        if (dt > MAX_DT) {
            dt = MAX_DT;
        }
        lastTimestamp = now;

        engine.tick(dt);

        // Push updated model values into the view layer
        for (BlockNode node : workspaceController.getBlockNodes()) {
            if (node instanceof ScopeBlockNode scopeNode) {
                scopeNode.updatePlot();
            } else if (node instanceof ClockBlockNode clockNode) {
                clockNode.updateDisplay();
            } else if (node instanceof SineBlockNode sineNode) {
                sineNode.updateDisplay();
            } else if (node instanceof CosineBlockNode cosineNode) {
                cosineNode.updateDisplay();
            }
        }

        simulationTimeProperty.set(engine.getSimulationTime());
    }
}
