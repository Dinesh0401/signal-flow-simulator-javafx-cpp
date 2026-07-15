# Signal Flow Simulator

A professional signal-processing block-diagram application featuring draggable blocks, CubicCurve wiring, live simulation, and a C++ mathematical backend.

## Project Features

- **Draggable Blocks:** Add, move, and connect signal processing blocks dynamically.
- **Wire Connections:** Drag wires from output to input ports with smooth cubic Bézier curves.
- **Live Simulation Engine:** Ticks the directed acyclic graph (DAG) topologically at 60 FPS using JavaFX `AnimationTimer`.
- **Live Scope Plotting:** Visualizes waveforms in real-time.
- **C++ Native Backend:** Math computations (sine, cosine) offloaded to C++ using JNI, with a seamless fallback to Java `Math` if the native library is missing.
- **Modern UI:** Professional dark-themed UI, grid backgrounds, zoom/pan workspace, and animated states.

## Folder Structure

```
signal-flow-simulator/
├── pom.xml                        # Maven configuration
├── src/main/java/com/signalflow/  # JavaFX Frontend and Engine Source
│   ├── model/                     # Pure Java POJO model (Graph, Ports, Blocks)
│   ├── view/                      # JavaFX UI components (Nodes, Canvas, Ports)
│   ├── controller/                # Interaction handling and Simulation bridge
│   ├── engine/                    # Simulation ticking and JNI bridge
│   └── App.java                   # Application Entry Point
├── src/main/resources/com/signalflow/
│   └── styles.css                 # Application styling
├── native/                        # C++ Backend Source
│   ├── include/native_math.h
│   ├── src/native_math.cpp
│   ├── src/jni_bridge.cpp
│   └── CMakeLists.txt             # Build script for native shared library
└── .vscode/                       # Editor configurations
    ├── settings.json
    └── launch.json
```

## Setup Instructions

### Prerequisites
- **Java 21 (JDK)**
- **Maven** (A Maven wrapper is included in `.mvn` if you don't have Maven installed globally).
- **C++ Compiler (Optional but recommended):** MinGW-w64 (`g++`) and `cmake` for building the native backend.

### 1. Build and Run the Java Application
The application can be compiled and launched using Maven. Without the native library, it will use the graceful fallback (Java standard Math library) and log a warning.

```bash
mvn clean compile
mvn javafx:run
```

If using VS Code, simply open the project and press `F5` to run the pre-configured "Signal Flow Simulator" launch configuration.

### 2. Build the Native C++ Backend (Optional)
To enable the native C++ math computations, compile the shared library and ensure it is placed in the project root directory (where `pom.xml` resides).

1. Ensure `g++` and `cmake` are in your PATH.
2. Open a terminal and navigate to the `native/` folder:
   ```bash
   cd native
   cmake -B build -G "MinGW Makefiles"
   cmake --build build
   ```
3. The CMake configuration automatically outputs the compiled `.dll` (on Windows) directly into the project root directory.
4. Run the Java application again. The UI's top bar should indicate the native backend is active.

## Screenshots Instructions

To submit screenshots for this assignment:
1. **Launch the application.** Add a Clock block, Sine block, and Scope block.
2. **Connect them:** Drag a wire from Clock's output to Sine's input. Then Sine's output to Scope's input.
3. **Capture UI:** Take a screenshot of the main workspace showing the connected blocks and curved wires.
4. **Capture Simulation:** Click "Start" on the toolbar. Take a screenshot showing the live waveform plotting on the Scope.
5. **Save to folder:** Store all captured screenshots in a `screenshots/` directory within this project.

## Git Commits Recommendations

When initializing this repository, it is recommended to use the following logical commit history:
- `chore: initial maven project scaffold and vscode config`
- `feat: implement pure java model layer (DAG, Blocks, Ports)`
- `feat: implement draggable view layer and workspace canvas`
- `feat: add wire connection interactions (CubicCurve)`
- `feat: integrate simulation engine and AnimationTimer`
- `feat: implement live scope plotting`
- `feat: add C++ JNI backend and graceful fallback`
- `style: apply professional dark theme UI polish`
