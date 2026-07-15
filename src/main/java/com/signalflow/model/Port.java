package com.signalflow.model;

public class Port {

    public enum Direction {
        IN,
        OUT
    }

    private final String name;
    private final Direction direction;
    private final Block parent;
    private double value;

    public Port(String name, Direction direction, Block parent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Port name must not be null or blank");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Port direction must not be null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Port parent must not be null");
        }
        this.name = name;
        this.direction = direction;
        this.parent = parent;
        this.value = 0.0;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    public Block getParent() {
        return parent;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void reset() {
        this.value = 0.0;
    }

    @Override
    public String toString() {
        return "Port{name='" + name + "', direction=" + direction + ", value=" + value + "}";
    }
}
