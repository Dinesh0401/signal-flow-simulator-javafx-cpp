# Signal Flow Simulator

A desktop-based **Signal Flow Simulator** built using **Java 21**, **JavaFX**, and **Maven**. The application allows users to visually create signal-processing pipelines using draggable blocks, connect them with dynamic Bézier curve wires, and observe real-time waveform simulation through an oscilloscope (Scope).

The project demonstrates graph-based execution, real-time visualization, and optional native C++ acceleration using JNI with a graceful Java fallback.

## Resources

- **Project Demo:** https://www.youtube.com/watch?v=RMCln3emfm0
- **Technical Blog:** https://medium.com/@dineshsj/building-a-signal-flow-simulator-with-javafx-maven-jni-my-technical-assignment-journey-d490aa64c6b6


## Project Overview

The simulator provides an interactive workspace where users can:

- Create signal-processing block diagrams
- Connect blocks using drag-and-drop wiring
- Simulate signal flow in real time
- Visualize waveforms using a Scope block
- Execute mathematical operations using either:
  - Native C++ (JNI)
  - Java `Math` fallback (default)

---

## Features

### Interactive Workspace
- Draggable signal-processing blocks
- Infinite canvas with zoom and pan
- Dynamic Cubic Bézier wire connections
- Real-time node updates

### Simulation Engine
- Directed Acyclic Graph (DAG) execution
- Kahn's Topological Sort
- 60 FPS simulation using JavaFX AnimationTimer
- Stable simulation timestep

### Signal Blocks
- Clock Generator
- Sine Wave Generator
- Scope (Waveform Viewer)

### Native Backend
- JNI integration with C++
- Automatic fallback to Java `Math`
- No application crash when native library is unavailable

### User Interface
- Modern dark theme
- Responsive layout
- Smooth animations
- Grid-based workspace

---

# Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 21 LTS | Core Application |
| JavaFX 21 | Desktop GUI |
| Maven 3.9 | Build & Dependency Management |
| C++ | Native Math Backend |
| JNI | Java ↔ C++ Communication |
| CSS | UI Styling |
| VS Code | Development Environment |

---

# Project Structure

```
signal-flow-simulator/
│
├── pom.xml
├── README.md
├── REPORT.md
├── mvnw.cmd
├── .mvn/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/signalflow/
│   │   │       ├── controller/
│   │   │       ├── engine/
│   │   │       ├── model/
│   │   │       ├── view/
│   │   │       └── App.java
│   │   │
│   │   └── resources/
│   │       └── styles.css
│
├── native/
│   ├── include/
│   ├── src/
│   └── CMakeLists.txt
│
└── .vscode/
```

---

# Requirements

- Java 21 or above
- Maven 3.9+
- VS Code (Recommended)

Optional:

- MinGW-w64
- CMake

These are required only for building the native C++ backend.

---

# Build Instructions

Clone the repository:

```bash
git clone https://github.com/Dinesh0401/signal-flow-simulator.git

cd signal-flow-simulator
```

Compile the project:

```bash
mvn clean compile
```

Run the application:

```bash
mvn javafx:run
```

---

# Native Backend (Optional)

The simulator supports native mathematical computation through JNI.

To enable it:

```bash
cd native

cmake -B build -G "MinGW Makefiles"

cmake --build build
```

Place the generated shared library (`signalflow_native.dll`) in the project root.

If the native library is not found, the application automatically switches to Java's built-in `Math` implementation.

This behavior is intentional and does not affect the functionality of the application.

---

# How to Use

1. Launch the application.
2. Add a **Clock** block.
3. Add a **Sine** block.
4. Add a **Scope** block.
5. Connect:

```
Clock → Sine → Scope
```

6. Start the simulation.
7. Observe the waveform in the Scope block.
8. Adjust the Clock frequency to see live waveform updates.

---

# Verification

The project has been verified using the following commands.

Build:

```bash
mvn clean
```

Result:

```
BUILD SUCCESS
```

Compile:

```bash
mvn compile
```

Result:

```
BUILD SUCCESS
```

Run:

```bash
mvn javafx:run
```

Verified:

- Application launches successfully
- Signal blocks function correctly
- Scope renders waveform
- Drag-and-drop wiring works
- Zoom and pan work
- Java fallback operates correctly when JNI library is absent

---

# Screenshots

For submission, include screenshots showing:

- Main workspace
- Connected signal blocks
- Curved wire connections
- Live waveform on the Scope
- Running simulation

Store screenshots inside:

```
screenshots/
```

---

# Design Highlights

- MVC Architecture
- Modular code organization
- Graph-based execution
- Property binding for wire synchronization
- Canvas-based waveform rendering
- Graceful native fallback
- Clean and maintainable codebase

---

# Future Improvements

- Additional signal blocks
- Save/Open projects
- Undo/Redo support
- FFT visualization
- Plugin architecture
- Multi-threaded simulation
- Export waveform data

---

# Author

**Dinesh S J**

Final Year – Computer Science and Business Systems

Knowledge Institute of Technology, Salem

Email: **sjdineshofficial@gmail.com**

GitHub: **https://github.com/Dinesh0401**

LinkedIn: **https://www.linkedin.com/in/dinesh-s-j**

---

# License

This project was developed as part of the **PASS Science Works Technical Assignment** for evaluation purposes.
