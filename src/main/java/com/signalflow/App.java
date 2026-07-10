package com.signalflow;

import com.signalflow.controller.SimulationController;
import com.signalflow.controller.WorkspaceController;
import com.signalflow.engine.NativeMath;
import com.signalflow.engine.SimulationEngine;
import com.signalflow.model.BlockGraph;
import com.signalflow.view.WorkspaceCanvas;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Entry point for the Signal Flow Simulator.
 * Assembles the model, engine, view, and controller layers and launches
 * the primary window with a toolbar and a scrollable workspace canvas.
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

        // ── Toolbar ─────────────────────────────────────────────────────
        Button startBtn = new Button("\u25B6 Start");
        startBtn.getStyleClass().add("toolbar-button");
        startBtn.setOnAction(e -> simController.start());

        Button stopBtn = new Button("\u23F9 Stop");
        stopBtn.getStyleClass().add("toolbar-button");
        stopBtn.setOnAction(e -> simController.stop());

        Button resetBtn = new Button("\u21BA Reset");
        resetBtn.getStyleClass().add("toolbar-button");
        resetBtn.setOnAction(e -> simController.reset());

        // Disable Start when already running; disable Stop/Reset when idle
        startBtn.disableProperty().bind(simController.runningProperty());
        stopBtn.disableProperty().bind(simController.runningProperty().not());

        Label addBlockLabel = new Label("Add Block:");
        addBlockLabel.getStyleClass().add("toolbar-label");

        Button clockBtn = new Button("\uD83D\uDD50 Clock");
        clockBtn.getStyleClass().add("toolbar-button");
        clockBtn.setOnAction(e -> workspaceController.addBlock("Clock", randomX(), randomY()));

        Button sineBtn = new Button("\u223F Sine");
        sineBtn.getStyleClass().add("toolbar-button");
        sineBtn.setOnAction(e -> workspaceController.addBlock("Sine", randomX(), randomY()));

        Button cosineBtn = new Button("\u223F Cosine");
        cosineBtn.getStyleClass().add("toolbar-button");
        cosineBtn.setOnAction(e -> workspaceController.addBlock("Cosine", randomX(), randomY()));

        Button scopeBtn = new Button("\uD83D\uDCCA Scope");
        scopeBtn.getStyleClass().add("toolbar-button");
        scopeBtn.setOnAction(e -> workspaceController.addBlock("Scope", randomX(), randomY()));

        Label timeLabel = new Label("t = 0.000 s");
        timeLabel.getStyleClass().add("toolbar-label");
        timeLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("t = %.3f s", simController.simulationTimeProperty().get()),
                        simController.simulationTimeProperty()));

        String backendText = NativeMath.isNativeAvailable()
                ? "Backend: Native (JNI)"
                : "Backend: Java Math";
        Label backendLabel = new Label(backendText);
        backendLabel.getStyleClass().add("toolbar-label");
        backendLabel.setTextFill(NativeMath.isNativeAvailable()
                ? Color.web("#66BB6A")
                : Color.web("#FFA726"));

        ToolBar toolbar = new ToolBar(
                startBtn, stopBtn, resetBtn,
                new Separator(),
                addBlockLabel, clockBtn, sineBtn, cosineBtn, scopeBtn,
                new Separator(),
                timeLabel,
                new Separator(),
                backendLabel);
        toolbar.getStyleClass().add("main-toolbar");
        toolbar.setPadding(new Insets(6, 10, 6, 10));

        // ── Layout ──────────────────────────────────────────────────────
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("workspace-scroll");

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(scrollPane);
        root.getStyleClass().add("root-pane");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Apply optional CSS stylesheet from resources
        String css = getClass().getResource("/styles.css") != null
                ? getClass().getResource("/styles.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }

        primaryStage.setTitle("Signal Flow Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // ── Seed the workspace with a default signal chain ──────────────
        workspaceController.addBlock("Clock", 100, 200);
        workspaceController.addBlock("Sine", 350, 200);
        workspaceController.addBlock("Scope", 600, 200);
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
