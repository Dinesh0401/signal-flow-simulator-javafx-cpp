package com.signalflow.model;

import java.util.Arrays;

public class ScopeBlock extends Block {

    private final int bufferSize;
    private double[] buffer1;
    private double[] buffer2;
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
        this.buffer1 = new double[bufferSize];
        this.buffer2 = new double[bufferSize];
        this.writeIndex = 0;
        this.sampleCount = 0;
        
        // Scope needs 2 input ports to plot sine and cosine simultaneously
        addInputPort("in1");
        addInputPort("in2");
    }

    @Override
    public void tick(double dt) {
        double val1 = getInputPort("in1").getValue();
        double val2 = getInputPort("in2").getValue();
        
        buffer1[writeIndex] = val1;
        buffer2[writeIndex] = val2;
        
        writeIndex = (writeIndex + 1) % bufferSize;
        sampleCount++;
    }

    /**
     * Returns a copy of the ring buffers in chronological order
     * (oldest sample first, newest sample last).
     * [0] contains buffer1 snapshot, [1] contains buffer2 snapshot.
     */
    public double[][] getBufferSnapshots() {
        double[] snap1 = new double[bufferSize];
        double[] snap2 = new double[bufferSize];

        if (sampleCount < bufferSize) {
            // Buffer hasn't wrapped yet — data starts at index 0
            System.arraycopy(buffer1, 0, snap1, 0, sampleCount);
            System.arraycopy(buffer2, 0, snap2, 0, sampleCount);
        } else {
            // Buffer has wrapped — writeIndex points to the oldest entry
            int tailLength = bufferSize - writeIndex;
            System.arraycopy(buffer1, writeIndex, snap1, 0, tailLength);
            System.arraycopy(buffer1, 0, snap1, tailLength, writeIndex);
            
            System.arraycopy(buffer2, writeIndex, snap2, 0, tailLength);
            System.arraycopy(buffer2, 0, snap2, tailLength, writeIndex);
        }

        return new double[][]{snap1, snap2};
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void reset() {
        Arrays.fill(buffer1, 0.0);
        Arrays.fill(buffer2, 0.0);
        writeIndex = 0;
        sampleCount = 0;
        super.reset();
    }
}
