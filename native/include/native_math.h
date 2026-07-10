#ifndef NATIVE_MATH_H
#define NATIVE_MATH_H

namespace signalflow {

    // Compute sine of x (radians)
    double compute_sin(double x);

    // Compute cosine of x (radians)
    double compute_cos(double x);

    // Advance clock: returns currentTime + dt * frequency
    double clock_tick(double currentTime, double dt, double frequency);

} // namespace signalflow

#endif // NATIVE_MATH_H
