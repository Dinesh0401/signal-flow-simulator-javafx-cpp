# Engineering Report: Signal Flow Simulator

## 1. Architectural Design

The application enforces a strict **Model-View-Controller (MVC)** separation to maintain clean architecture, highly decoupled code, and single-responsibility principles.

### Model Layer
All classes in `com.signalflow.model` are pure Java POJOs devoid of any JavaFX imports. 
- **Graph & Topology:** The `BlockGraph` class maintains the Directed Acyclic Graph (DAG) of `Block` and `Connection` instances. It utilizes Kahn’s Algorithm for topological sorting to ensure proper execution order (upstream nodes calculate before downstream nodes).
- **Extensibility:** The base `Block` abstract class allows easy creation of new signal processing entities. Each block exposes standard `Port` interfaces.

### View Layer
All classes in `com.signalflow.view` handle visual representation using JavaFX Nodes (`Group`, `Circle`, `CubicCurve`).
- **Interactive:** `BlockNode` handles its own draggable layout mathematics. `PortNode` scales and glows on hover.
- **Declarative Separation:** The visual nodes simply expose getters for their layout properties, completely ignorant of the simulation logic.

### Controller & Engine Layer
Bridges the gap between data and visualization.
- **SimulationEngine:** Iterates over the graph sequentially based on the elapsed time delta (`dt`).
- **SimulationController:** Hooks into JavaFX’s `AnimationTimer` to synchronize the engine ticks with the screen refresh rate (~60Hz). It handles the task of pushing the newly calculated model state back into the view layer (e.g., updating the Scope plotting).
- **WorkspaceController:** Manages user interactions such as dragging a wire from an output port to an input port. It uses raycasting/hit-testing against the JavaFX Scene Graph (`pickResult`) to validate wire connection targets securely.

## 2. JNI Backend & Fallback Strategy

The assignment required a C++ mathematical backend. The implementation uses Java Native Interface (JNI). 

**Design Decisions:**
- **Performance:** Math operations (`sin`, `cos`) are routed through `jni_bridge.cpp` to the pure C++ logic. 
- **Resilience:** If the C++ `.dll` is not compiled or missing from the path, the application must not crash. The `NativeMath` Java wrapper uses a static initializer block with a `try-catch` on `System.loadLibrary()`. If it fails, a boolean flag gracefully routes all mathematical requests to the standard `java.lang.Math` library, allowing UI and workflow testing out-of-the-box.
- **Build System:** A `CMakeLists.txt` file is provided that automates building the JNI shared library and places it directly into the project root for automatic Java detection.

## 3. SOLID Principles Check

- **Single Responsibility:** A `Connection` only routes data. A `SimulationEngine` only computes data. A `ScopeBlockNode` only renders data.
- **Open/Closed:** The system is open for extension (adding new types of blocks by extending `Block` and `BlockNode`) but closed for modification (the core Engine does not need to change when a new block is introduced).
- **Liskov Substitution:** All subclasses of `Block` seamlessly integrate into the topological tick loop.
- **Interface Segregation / Dependency Inversion:** Controllers depend on abstractions (`Block`) rather than concrete implementations for ticking logic.

## 4. UI / UX Design

Aesthetic quality is enforced via `styles.css`. 
- **Theme:** A cohesive dark theme (inspired by modern developer tools like GitHub Dark) prevents eye strain.
- **Feedback:** Ports scale and glow upon mouse hover to indicate interactivity. A "dashed" temporary wire follows the cursor when routing.
- **Data Visualization:** The Scope uses an optimized `Canvas` element. Rather than updating a JavaFX Scene Graph with hundreds of line segments, it utilizes direct `GraphicsContext2D` immediate-mode drawing for maximum performance at 60 FPS.

## 5. Known Limitations
- The current routing allows only one wire per input port (to prevent signal collision), which is mathematically correct but could be expanded to support summing nodes.
- Cyclic connections are not allowed due to the topological sort. Feedback loops would require the introduction of a "Delay/Z-1" block to buffer state across frames.
