package com.signalflow.model;

import java.lang.reflect.Method;

public class CosineBlock extends Block {

    private boolean useNative;

    // Cached reflective handles for the native path
    private static Method nativeCosMethod;
    private static boolean nativeResolved;

    public CosineBlock(String name) {
        super(name);
        this.useNative = false;
        addInputPort("in");
        addOutputPort("out");
    }

    @Override
    public void tick(double dt) {
        double inputValue = getInputPort("in").getValue();
        double result = computeCosine(inputValue);
        getOutputPort("out").setValue(result);
    }

    private double computeCosine(double value) {
        if (useNative) {
            try {
                return invokeNativeCos(value);
            } catch (Exception e) {
                // NativeMath unavailable — fall back to java.lang.Math
                return Math.cos(value);
            }
        }
        return Math.cos(value);
    }

    /**
     * Reflectively invokes com.signalflow.engine.NativeMath.cos(double)
     * so the model layer has no compile-time dependency on the engine module.
     */
    private static double invokeNativeCos(double value) throws Exception {
        if (!nativeResolved) {
            Class<?> clazz = Class.forName("com.signalflow.engine.NativeMath");
            nativeCosMethod = clazz.getMethod("cos", double.class);
            nativeResolved = true;
        }
        return (double) nativeCosMethod.invoke(null, value);
    }

    public boolean isUseNative() {
        return useNative;
    }

    public void setUseNative(boolean useNative) {
        this.useNative = useNative;
    }
}
