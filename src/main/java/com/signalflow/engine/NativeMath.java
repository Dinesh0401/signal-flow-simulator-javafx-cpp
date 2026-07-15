package com.signalflow.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides math operations with optional JNI acceleration.
 * Falls back transparently to java.lang.Math when the native library
 * is not available on the classpath.
 */
public final class NativeMath {

    private static final Logger LOGGER = Logger.getLogger(NativeMath.class.getName());

    private static boolean nativeAvailable = false;

    static {
        try {
            System.loadLibrary("signalflow_native");
            nativeAvailable = true;
            LOGGER.info("Native math backend loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            nativeAvailable = false;
            LOGGER.log(Level.WARNING,
                    "Native math library not found — falling back to java.lang.Math. "
                    + "To enable native acceleration, place signalflow_native in java.library.path.",
                    e);
        }
    }

    private NativeMath() {
        // Static utility class — no instantiation
    }

    // ── JNI native declarations ─────────────────────────────────────────

    public static native double nativeSin(double x);

    public static native double nativeCos(double x);

    public static native double nativeClockTick(double currentTime, double dt, double frequency);

    // ── Public wrapper methods with automatic fallback ──────────────────

    /**
     * Computes the sine of {@code x} (radians).
     * Uses the native backend when available, otherwise {@link Math#sin}.
     */
    public static double sin(double x) {
        if (nativeAvailable) {
            return nativeSin(x);
        }
        return Math.sin(x);
    }

    /**
     * Computes the cosine of {@code x} (radians).
     * Uses the native backend when available, otherwise {@link Math#cos}.
     */
    public static double cos(double x) {
        if (nativeAvailable) {
            return nativeCos(x);
        }
        return Math.cos(x);
    }

    /**
     * Advances a clock signal by one tick.
     *
     * @param currentTime current accumulated time
     * @param dt          time delta for this tick
     * @param frequency   clock frequency multiplier
     * @return the new accumulated time
     */
    public static double clockTick(double currentTime, double dt, double frequency) {
        if (nativeAvailable) {
            return nativeClockTick(currentTime, dt, frequency);
        }
        return currentTime + dt * frequency;
    }

    /**
     * @return {@code true} if the JNI native library was loaded successfully.
     */
    public static boolean isNativeAvailable() {
        return nativeAvailable;
    }
}
