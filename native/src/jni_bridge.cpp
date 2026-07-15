#include <jni.h>
#include "native_math.h"

extern "C" {

/*
 * Class:     com_signalflow_engine_NativeMath
 * Method:    nativeSin
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_com_signalflow_engine_NativeMath_nativeSin
  (JNIEnv *env, jclass cls, jdouble x) {
    return signalflow::compute_sin(static_cast<double>(x));
}

/*
 * Class:     com_signalflow_engine_NativeMath
 * Method:    nativeCos
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_com_signalflow_engine_NativeMath_nativeCos
  (JNIEnv *env, jclass cls, jdouble x) {
    return signalflow::compute_cos(static_cast<double>(x));
}

/*
 * Class:     com_signalflow_engine_NativeMath
 * Method:    nativeClockTick
 * Signature: (DDD)D
 */
JNIEXPORT jdouble JNICALL Java_com_signalflow_engine_NativeMath_nativeClockTick
  (JNIEnv *env, jclass cls, jdouble currentTime, jdouble dt, jdouble frequency) {
    return signalflow::clock_tick(
        static_cast<double>(currentTime),
        static_cast<double>(dt),
        static_cast<double>(frequency)
    );
}

} // extern "C"
