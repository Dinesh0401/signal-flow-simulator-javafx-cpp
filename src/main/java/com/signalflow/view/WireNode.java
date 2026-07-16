package com.signalflow.view;

import com.signalflow.model.Connection;

import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Visual representation of a {@link Connection} between two {@link PortNode}s.
 * Renders as orthogonal straight lines with an arrowhead (▶) at the destination.
 * Matches MATLAB Simulink wire style.
 */
public class WireNode extends Group {

    private final PortNode sourcePortNode;
    private final PortNode destPortNode;
    private final Connection modelConnection;
    private final Polyline line;
    private final Polygon arrowHead;

    /** Size of the arrowhead triangle. */
    private static final double ARROW_LENGTH = 8.0;
    private static final double ARROW_HALF_WIDTH = 3.5;

    public WireNode(PortNode sourcePortNode, PortNode destPortNode, Connection modelConnection) {
        this.sourcePortNode = sourcePortNode;
        this.destPortNode = destPortNode;
        this.modelConnection = modelConnection;

        // Wire line
        this.line = new Polyline();
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.5);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeLineJoin(StrokeLineJoin.MITER);

        // Arrowhead triangle at destination
        this.arrowHead = new Polygon();
        arrowHead.setFill(Color.BLACK);

        // Consume right-click so the context-menu handling stays with the controller
        line.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });

        getChildren().addAll(line, arrowHead);

        // Bind to parent block layout changes so the wire follows drag operations
        ChangeListener<Number> layoutUpdater = (obs, oldVal, newVal) -> updatePositions();

        sourcePortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        sourcePortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);

        updatePositions();
    }

    /**
     * Recomputes the wire and arrowhead positions.
     */
    public void updatePositions() {
        double startX = sourcePortNode.getParentBlockNode().getLayoutX()
                + sourcePortNode.getLayoutX()
                + sourcePortNode.getRadius();
        double startY = sourcePortNode.getParentBlockNode().getLayoutY()
                + sourcePortNode.getLayoutY()
                + sourcePortNode.getRadius();

        double endX = destPortNode.getParentBlockNode().getLayoutX()
                + destPortNode.getLayoutX()
                + destPortNode.getRadius();
        double endY = destPortNode.getParentBlockNode().getLayoutY()
                + destPortNode.getLayoutY()
                + destPortNode.getRadius();

        double arrowBaseX = endX - ARROW_LENGTH;

        // --- Orthogonal wire routing ---
        if (Math.abs(startY - endY) < 2.0) {
            // Straight horizontal wire
            line.getPoints().setAll(
                startX, startY,
                arrowBaseX, startY
            );
        } else {
            // H-V-H routing
            double midX = (startX + endX) / 2.0;
            line.getPoints().setAll(
                startX, startY,
                midX, startY,
                midX, endY,
                arrowBaseX, endY
            );
        }

        // --- Arrowhead pointing right (▶) at destination ---
        arrowHead.getPoints().setAll(
            arrowBaseX, endY - ARROW_HALF_WIDTH,
            endX, endY,
            arrowBaseX, endY + ARROW_HALF_WIDTH
        );
    }

    public Connection getModelConnection() {
        return modelConnection;
    }

    public Polyline getLine() {
        return line;
    }

    public PortNode getSourcePortNode() {
        return sourcePortNode;
    }

    public PortNode getDestPortNode() {
        return destPortNode;
    }
}
