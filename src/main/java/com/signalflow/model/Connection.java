package com.signalflow.model;

import java.util.UUID;

public class Connection {

    private final String id;
    private final Port sourcePort;
    private final Port destPort;

    public Connection(Port sourcePort, Port destPort) {
        if (sourcePort == null) {
            throw new IllegalArgumentException("Source port must not be null");
        }
        if (destPort == null) {
            throw new IllegalArgumentException("Destination port must not be null");
        }
        if (sourcePort.getDirection() != Port.Direction.OUT) {
            throw new IllegalArgumentException(
                    "Source port must have direction OUT, got " + sourcePort.getDirection());
        }
        if (destPort.getDirection() != Port.Direction.IN) {
            throw new IllegalArgumentException(
                    "Destination port must have direction IN, got " + destPort.getDirection());
        }
        this.id = UUID.randomUUID().toString();
        this.sourcePort = sourcePort;
        this.destPort = destPort;
    }

    public String getId() {
        return id;
    }

    public Port getSourcePort() {
        return sourcePort;
    }

    public Port getDestPort() {
        return destPort;
    }

    /**
     * Copies the current value from the source port to the destination port.
     */
    public void propagate() {
        destPort.setValue(sourcePort.getValue());
    }

    @Override
    public String toString() {
        return "Connection{id='" + id + "', source=" + sourcePort + ", dest=" + destPort + "}";
    }
}
