package com.signalflow.view;

import com.signalflow.model.Connection;

import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * Visual representation of a {@link Connection} between two {@link PortNode}s.
 * Renders as a straight line that automatically tracks the positions of its
 * source and destination block nodes.
 *
 * Changed from CubicCurve to straight Line per assignment requirement:
 * "connecting lines should be straight rather than curvelines".
 */
public class WireNode extends Group {

    private final PortNode sourcePortNode;
    private final PortNode destPortNode;
    private final Connection modelConnection;
    private final Line line;

    public WireNode(PortNode sourcePortNode, PortNode destPortNode, Connection modelConnection) {
        this.sourcePortNode = sourcePortNode;
        this.destPortNode = destPortNode;
        this.modelConnection = modelConnection;

        this.line = new Line();
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.5);
        line.setStrokeLineCap(StrokeLineCap.ROUND);

        // Consume right-click so the context-menu handling stays with the controller
        line.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });

        getChildren().add(line);

        // Bind to parent block layout changes so the wire follows drag operations
        ChangeListener<Number> layoutUpdater = (obs, oldVal, newVal) -> updatePositions();

        sourcePortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        sourcePortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);

        updatePositions();
    }

    /**
     * Recomputes the line geometry from the current port positions.
     * Source and destination coordinates are derived from the port's local
     * offset within its parent block node plus the block node's layout position.
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

        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
    }

    public Connection getModelConnection() {
        return modelConnection;
    }

    public Line getLine() {
        return line;
    }

    public PortNode getSourcePortNode() {
        return sourcePortNode;
    }

    public PortNode getDestPortNode() {
        return destPortNode;
    }
}
