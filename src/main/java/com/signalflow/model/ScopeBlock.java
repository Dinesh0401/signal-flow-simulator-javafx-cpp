package com.signalflow.model;

import java.util.Arrays;

public class ScopeBlock extends Block {

    private final int bufferSize;
    private double[] buffer;
    private int writeIndex;
    private int sampleCount;

    public ScopeBlock(String name) {
        this(name, 500);
    }

    public ScopeBlock(String name, int bufferSize) {
        super(name);
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive, got " + bufferSize);
        }
        this.bufferSize = bufferSize;
        this.buffer = new double[bufferSize];
        this.writeIndex = 0;
        this.sampleCount = 0;
        addInputPort("in");
    }

    @Override
    public void tick(double dt) {
        double inputValue = getInputPort("in").getValue();
        buffer[writeIndex] = inputValue;
        writeIndex = (writeIndex + 1) % bufferSize;
        sampleCount++;
    }

    /**
     * Returns a copy of the ring buffer in chronological order
     * (oldest sample first, newest sample last).
     */
    public double[] getBufferSnapshot() {
        double[] snapshot = new double[bufferSize];

        if (sampleCount < bufferSize) {
            // Buffer hasn't wrapped yet — data starts at index 0
            System.arraycopy(buffer, 0, snapshot, 0, sampleCount);
            // Remaining entries stay 0.0
        } else {
            // Buffer has wrapped — writeIndex points to the oldest entry
            int tailLength = bufferSize - writeIndex;
            System.arraycopy(buffer, writeIndex, snapshot, 0, tailLength);
            System.arraycopy(buffer, 0, snapshot, tailLength, writeIndex);
        }

        return snapshot;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void reset() {
        Arrays.fill(buffer, 0.0);
        writeIndex = 0;
        sampleCount = 0;
        super.reset();
    }
}
