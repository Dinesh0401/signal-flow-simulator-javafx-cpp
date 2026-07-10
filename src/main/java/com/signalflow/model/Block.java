package com.signalflow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Block {

    private final String id;
    private final String name;
    private final List<Port> inputPorts;
    private final List<Port> outputPorts;

    protected Block(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Block name must not be null or blank");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Port> getInputPorts() {
        return Collections.unmodifiableList(inputPorts);
    }

    public List<Port> getOutputPorts() {
        return Collections.unmodifiableList(outputPorts);
    }

    /**
     * Creates and registers an input port on this block.
     */
    public Port addInputPort(String name) {
        Port port = new Port(name, Port.Direction.IN, this);
        inputPorts.add(port);
        return port;
    }

    /**
     * Creates and registers an output port on this block.
     */
    public Port addOutputPort(String name) {
        Port port = new Port(name, Port.Direction.OUT, this);
        outputPorts.add(port);
        return port;
    }

    /**
     * Finds an input port by name, or null if not found.
     */
    public Port getInputPort(String name) {
        for (Port port : inputPorts) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        return null;
    }

    /**
     * Finds an output port by name, or null if not found.
     */
    public Port getOutputPort(String name) {
        for (Port port : outputPorts) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        return null;
    }

    /**
     * Advance the block's internal state by one simulation step.
     *
     * @param dt the time delta for this step, in seconds
     */
    public abstract void tick(double dt);

    /**
     * Resets all ports to their default value (0.0).
     * Subclasses should call super.reset() to ensure ports are cleared.
     */
    public void reset() {
        for (Port port : inputPorts) {
            port.reset();
        }
        for (Port port : outputPorts) {
            port.reset();
        }
    }

    /**
     * Returns the simple class name of this block, useful for UI labels.
     */
    public String getBlockType() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getBlockType() + "{id='" + id + "', name='" + name + "'}";
    }
}
