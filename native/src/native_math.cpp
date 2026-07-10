#include "native_math.h"
#include <cmath>

namespace signalflow {

    double compute_sin(double x) {
        return std::sin(x);
    }

    double compute_cos(double x) {
        return std::cos(x);
    }

    double clock_tick(double currentTime, double dt, double frequency) {
        return currentTime + dt * frequency;
    }

} // namespace signalflow
