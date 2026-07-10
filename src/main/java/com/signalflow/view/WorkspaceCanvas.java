package com.signalflow.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

/**
 * Main workspace pane that holds all block nodes, wire nodes, and the background grid.
 * Supports zoom (scroll wheel) and pan (Ctrl+drag or middle mouse button).
 */
public class WorkspaceCanvas extends Pane {

    private static final double PREFERRED_WIDTH = 2000.0;
    private static final double PREFERRED_HEIGHT = 2000.0;
    private static final double MIN_SCALE = 0.3;
    private static final double MAX_SCALE = 3.0;
    private static final double ZOOM_FACTOR = 1.08;
    private static final double DOT_SPACING = 20.0;
    private static final Color BACKGROUND_COLOR = Color.web("#0d1117");
    private static final Color DOT_COLOR = Color.web("#1c2333");

    private final Canvas gridCanvas;
    private final Group wireLayer;
    private final Group blockLayer;
    private final Scale scaleTransform;

    private double currentScale = 1.0;

    // Pan state
    private double panAnchorX;
    private double panAnchorY;
    private double panTranslateStartX;
    private double panTranslateStartY;
    private boolean isPanning;

    public WorkspaceCanvas() {
        setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        setStyle("-fx-background-color: #0d1117;");

        // Clip to prevent rendering outside bounds
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        // Grid background layer
        gridCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        drawGrid();

        // Wire layer (below blocks)
        wireLayer = new Group();

        // Block layer (above wires)
        blockLayer = new Group();

        // Scale transform applied to content layers
        scaleTransform = new Scale(1.0, 1.0, 0, 0);
        wireLayer.getTransforms().add(scaleTransform);
        blockLayer.getTransforms().add(scaleTransform);
        gridCanvas.getTransforms().add(scaleTransform);

        // Add layers: grid -> wires -> blocks
        getChildren().addAll(gridCanvas, wireLayer, blockLayer);

        configureZoom();
        configurePan();
    }

    // --- Grid drawing ---

    private void drawGrid() {
        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT);

        gc.setFill(DOT_COLOR);
        for (double x = DOT_SPACING; x < PREFERRED_WIDTH; x += DOT_SPACING) {
            for (double y = DOT_SPACING; y < PREFERRED_HEIGHT; y += DOT_SPACING) {
                gc.fillOval(x - 1.5, y - 1.5, 3, 3);
            }
        }
    }

    // --- Zoom support ---

    private void configureZoom() {
        addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            if (delta == 0) {
                return;
            }

            double factor = (delta > 0) ? ZOOM_FACTOR : 1.0 / ZOOM_FACTOR;
            double newScale = currentScale * factor;

            // Clamp to allowed range
            newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

            currentScale = newScale;
            scaleTransform.setX(currentScale);
            scaleTransform.setY(currentScale);

            event.consume();
        });
    }

    // --- Pan support (Ctrl+drag or middle mouse button) ---

    private void configurePan() {
        setOnMousePressed(event -> {
            boolean ctrlDrag = event.isPrimaryButtonDown() && event.isControlDown();
            boolean middleDrag = event.getButton() == MouseButton.MIDDLE;

            if (ctrlDrag || middleDrag) {
                isPanning = true;
                panAnchorX = event.getSceneX();
                panAnchorY = event.getSceneY();
                panTranslateStartX = getTranslateX();
                panTranslateStartY = getTranslateY();
                event.consume();
            }
        });

        setOnMouseDragged(event -> {
            if (isPanning) {
                double dx = event.getSceneX() - panAnchorX;
                double dy = event.getSceneY() - panAnchorY;
                setTranslateX(panTranslateStartX + dx);
                setTranslateY(panTranslateStartY + dy);
                event.consume();
            }
        });

        setOnMouseReleased(event -> {
            isPanning = false;
        });
    }

    // --- Public API ---

    /**
     * Adds a block node to the block layer at the specified position.
     */
    public void addBlockNode(BlockNode node, double x, double y) {
        node.setLayoutX(x);
        node.setLayoutY(y);
        blockLayer.getChildren().add(node);
    }

    /**
     * Removes a block node from the block layer.
     */
    public void removeBlockNode(BlockNode node) {
        blockLayer.getChildren().remove(node);
    }

    /**
     * Adds a wire node to the wire layer (rendered below blocks).
     * Accepts any Node to allow forward-reference to WireNode class.
     */
    public void addWireNode(Node wire) {
        wireLayer.getChildren().add(wire);
    }

    /**
     * Removes a wire node from the wire layer.
     */
    public void removeWireNode(Node wire) {
        wireLayer.getChildren().remove(wire);
    }

    public Group getBlockLayer() {
        return blockLayer;
    }

    public Group getWireLayer() {
        return wireLayer;
    }

    public double getCurrentScale() {
        return currentScale;
    }
}
