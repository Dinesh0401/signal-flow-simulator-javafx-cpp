package com.signalflow.model;

import java.lang.reflect.Method;

public class SineBlock extends Block {

    private boolean useNative;

    // Cached reflective handles for the native path
    private static Method nativeSinMethod;
    private static boolean nativeResolved;

    public SineBlock(String name) {
        super(name);
        this.useNative = false;
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    public void tick(double dt) {
        double inputValue = getInputPort("in").getValue();
        double result = computeSine(inputValue);
        getOutputPort("out").setValue(result);
    }

    private double computeSine(double value) {
        if (useNative) {
            try {
                return invokeNativeSin(value);
            } catch (Exception e) {
                // NativeMath unavailable — fall back to java.lang.Math
                return Math.sin(value);
            }
        }
        return Math.sin(value);
    }

    /**
     * Reflectively invokes com.signalflow.engine.NativeMath.sin(double)
     * so the model layer has no compile-time dependency on the engine module.
     */
    private static double invokeNativeSin(double value) throws Exception {
        if (!nativeResolved) {
            Class<?> clazz = Class.forName("com.signalflow.engine.NativeMath");
            nativeSinMethod = clazz.getMethod("sin", double.class);
            nativeResolved = true;
        }
        return (double) nativeSinMethod.invoke(null, value);
    }

    public boolean isUseNative() {
        return useNative;
    }

    public void setUseNative(boolean useNative) {
        this.useNative = useNative;
    }
}
