package com.signalflow.view;

import com.signalflow.model.Connection;

import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;

/**
 * Visual representation of a {@link Connection} between two {@link PortNode}s.
 * Renders as a smooth cubic Bézier curve that automatically tracks the
 * positions of its source and destination block nodes.
 */
public class WireNode extends Group {

    private final PortNode sourcePortNode;
    private final PortNode destPortNode;
    private final Connection modelConnection;
    private final CubicCurve curve;

    public WireNode(PortNode sourcePortNode, PortNode destPortNode, Connection modelConnection) {
        this.sourcePortNode = sourcePortNode;
        this.destPortNode = destPortNode;
        this.modelConnection = modelConnection;

        this.curve = new CubicCurve();
        curve.setStroke(Color.web("#4FC3F7", 0.8));
        curve.setStrokeWidth(2.5);
        curve.setFill(null);
        curve.setStrokeLineCap(StrokeLineCap.ROUND);

        // Consume right-click so the context-menu handling stays with the controller
        curve.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });

        getChildren().add(curve);

        // Bind to parent block layout changes so the wire follows drag operations
        ChangeListener<Number> layoutUpdater = (obs, oldVal, newVal) -> updatePositions();

        sourcePortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        sourcePortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutXProperty().addListener(layoutUpdater);
        destPortNode.getParentBlockNode().layoutYProperty().addListener(layoutUpdater);

        updatePositions();
    }

    /**
     * Recomputes the curve geometry from the current port positions.
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

        curve.setStartX(startX);
        curve.setStartY(startY);
        curve.setEndX(endX);
        curve.setEndY(endY);

        // Horizontal-biased Bézier control points for a smooth "S" curve
        double horizontalOffset = Math.abs(endX - startX) * 0.5;
        curve.setControlX1(startX + horizontalOffset);
        curve.setControlY1(startY);
        curve.setControlX2(endX - horizontalOffset);
        curve.setControlY2(endY);
    }

    public Connection getModelConnection() {
        return modelConnection;
    }

    public CubicCurve getCurve() {
        return curve;
    }

    public PortNode getSourcePortNode() {
        return sourcePortNode;
    }

    public PortNode getDestPortNode() {
        return destPortNode;
    }
}
