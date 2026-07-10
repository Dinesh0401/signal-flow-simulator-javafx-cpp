package com.signalflow.model;

public class ClockBlock extends Block {

    private double time;
    private double frequency;

    public ClockBlock(String name) {
        super(name);
        this.time = 0.0;
        this.frequency = 1.0;
        addOutputPort("time");
    }

    @Override
    public void tick(double dt) {
        time += dt * frequency;
        Port output = getOutputPort("time");
        output.setValue(time);
    }

    @Override
    public void reset() {
        time = 0.0;
        super.reset();
    }

    public double getTime() {
        return time;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
}
