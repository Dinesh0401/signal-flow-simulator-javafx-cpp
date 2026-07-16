package com.signalflow;

import com.signalflow.controller.SimulationController;
import com.signalflow.controller.WorkspaceController;
import com.signalflow.engine.NativeMath;
import com.signalflow.engine.SimulationEngine;
import com.signalflow.model.BlockGraph;
import com.signalflow.view.WorkspaceCanvas;
import com.signalflow.view.BlockNode;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Entry point for the Signal Flow Simulator.
 * Assembles the model, engine, view, and controller layers and launches
 * the primary window with a MATLAB Simulink-style toolbar and a scrollable
 * workspace canvas.
 */
public class App extends Application {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        // ── Model layer ─────────────────────────────────────────────────
        BlockGraph graph = new BlockGraph();

        // ── View layer ──────────────────────────────────────────────────
        WorkspaceCanvas canvas = new WorkspaceCanvas();
        canvas.setPrefSize(2400, 1600);

        // ── Controller layer ────────────────────────────────────────────
        WorkspaceController workspaceController = new WorkspaceController(canvas, graph);

        // ── Engine + simulation controller ──────────────────────────────
        SimulationEngine engine = new SimulationEngine(graph);
        SimulationController simController = new SimulationController(engine, workspaceController);

        // ── Toolbar — MATLAB Simulink Ribbon Style ──────────────────────

        // -- Simulation section --
        Label simLabel = new Label("Simulation");
        simLabel.getStyleClass().add("toolbar-label");
        simLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");

        Button runBtn = new Button("Run");
        runBtn.getStyleClass().add("toolbar-button");

        Button stopBtn = new Button("Stop");
        Button resetBtn = new Button("Reset");
        Button undoBtn = new Button("↶ Undo");
        Button stepSizeBtn = new Button("Step Size");

        // Attach controller actions
        runBtn.setOnAction(e -> simController.start());
        stopBtn.setOnAction(e -> simController.stop());
        resetBtn.setOnAction(e -> simController.reset());
        undoBtn.setOnAction(e -> workspaceController.undo());
        stepSizeBtn.setOnAction(e -> showStepSizeDialog(simController));
        
        stopBtn.getStyleClass().add("toolbar-button");
        resetBtn.getStyleClass().add("toolbar-button");
        undoBtn.getStyleClass().add("toolbar-button");
        stepSizeBtn.getStyleClass().add("toolbar-button");

        // Disable Run when already running; disable Stop when idle
        runBtn.disableProperty().bind(simController.runningProperty());
        stopBtn.disableProperty().bind(simController.runningProperty().not());


        // -- Add Block section --
        Label addBlockLabel = new Label("Add Block");
        addBlockLabel.getStyleClass().add("toolbar-label");
        addBlockLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");

        Button clockBtn = new Button("Clock");
        clockBtn.getStyleClass().add("toolbar-button");
        clockBtn.setOnAction(e -> workspaceController.addBlock("Clock", randomX(), randomY()));

        Button sineBtn = new Button("Sine");
        sineBtn.getStyleClass().add("toolbar-button");
        sineBtn.setOnAction(e -> workspaceController.addBlock("Sine", randomX(), randomY()));

        Button cosineBtn = new Button("Cosine");
        cosineBtn.getStyleClass().add("toolbar-button");
        cosineBtn.setOnAction(e -> workspaceController.addBlock("Cosine", randomX(), randomY()));

        Button scopeBtn = new Button("Scope");
        scopeBtn.getStyleClass().add("toolbar-button");
        scopeBtn.setOnAction(e -> workspaceController.addBlock("Scope", randomX(), randomY()));

        // -- Time display --
        Label timeLabel = new Label("t = 0.000 s");
        timeLabel.getStyleClass().add("toolbar-label");
        timeLabel.setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: white;");
        timeLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("t = %.3f s", simController.simulationTimeProperty().get()),
                        simController.simulationTimeProperty()));

        // -- Backend indicator --
        String backendText = NativeMath.isNativeAvailable()
                ? "JNI"
                : "Java";
        Label backendLabel = new Label("Backend: " + backendText);
        backendLabel.getStyleClass().add("toolbar-label");
        backendLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");

        ToolBar toolbar = new ToolBar(
                simLabel,
                runBtn, stopBtn, resetBtn, undoBtn, stepSizeBtn,
                new Separator(),
                addBlockLabel, clockBtn, sineBtn, cosineBtn, scopeBtn,
                new Separator(),
                timeLabel,
                new Separator(),
                backendLabel);
        toolbar.getStyleClass().add("main-toolbar");
        toolbar.setPadding(new Insets(4, 8, 4, 8));

        // ── Menu Bar ────────────────────────────────────────────────────
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        Menu fileMenu = new Menu("FILE");
        Menu simMenu = new Menu("SIMULATION");
        Menu debugMenu = new Menu("DEBUG");
        Menu modelingMenu = new Menu("MODELING");
        Menu formatMenu = new Menu("FORMAT");
        Menu appsMenu = new Menu("APPS");

        // Add dummy items so they behave like menus
        fileMenu.getItems().add(new MenuItem("New"));
        simMenu.getItems().add(new MenuItem("Model Settings"));
        debugMenu.getItems().add(new MenuItem("Step Forward"));
        modelingMenu.getItems().add(new MenuItem("Library Browser"));
        formatMenu.getItems().add(new MenuItem("Auto Arrange"));
        appsMenu.getItems().add(new MenuItem("Simulink Coder"));

        menuBar.getMenus().addAll(fileMenu, simMenu, debugMenu, modelingMenu, formatMenu, appsMenu);

        VBox topContainer = new VBox(menuBar, toolbar);

        // ── Layout ──────────────────────────────────────────────────────
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("workspace-scroll");

        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(scrollPane);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Apply CSS stylesheet from resources
        String css = getClass().getResource("styles.css") != null
                ? getClass().getResource("styles.css").toExternalForm()
                : null;
        if (css == null) {
            // Fallback: try root classpath
            css = getClass().getResource("/com/signalflow/styles.css") != null
                    ? getClass().getResource("/com/signalflow/styles.css").toExternalForm()
                    : null;
        }
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("Untitled - Signal Flow Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // ── Seed the workspace with a default signal chain ──────────────
        // Layout matching MATLAB Simulink reference:
        // Clock far-left, Sine/Cosine stacked in the middle, Scope far-right
        BlockNode clockNode  = workspaceController.addBlock("Clock",  80, 200);
        BlockNode sineNode   = workspaceController.addBlock("Sine",   300, 120);
        BlockNode cosineNode = workspaceController.addBlock("Cosine", 300, 280);
        BlockNode scopeNode  = workspaceController.addBlock("Scope",  550, 200);

        workspaceController.connectBlocks(clockNode, "out", sineNode, "in");
        workspaceController.connectBlocks(clockNode, "out", cosineNode, "in");
        workspaceController.connectBlocks(sineNode, "out", scopeNode, "in1");
        workspaceController.connectBlocks(cosineNode, "out", scopeNode, "in2");
        
        workspaceController.clearUndoStack();
    }

    /**
     * Shows a Step Size configuration dialog.
     * The user can enter a numeric step size value. If valid, it is stored
     * in the SimulationController and used for subsequent simulation ticks.
     */
    private void showStepSizeDialog(SimulationController simController) {
        TextInputDialog dialog = new TextInputDialog(
                simController.getStepSize() > 0
                        ? String.valueOf(simController.getStepSize())
                        : "0.01");
        dialog.setTitle("Step Size");
        dialog.setHeaderText("Simulation Step Size");
        dialog.setContentText("Step size (seconds):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                double stepSize = Double.parseDouble(value.trim());
                if (stepSize > 0) {
                    simController.setStepSize(stepSize);
                }
            } catch (NumberFormatException ignored) {
                // Invalid input — silently ignore
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private double randomX() {
        return 80 + Math.random() * 500;
    }

    private double randomY() {
        return 80 + Math.random() * 400;
    }
}
