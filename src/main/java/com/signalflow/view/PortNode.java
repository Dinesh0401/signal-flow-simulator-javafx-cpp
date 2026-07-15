package com.signalflow.view;

import com.signalflow.model.Port;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Visual representation of a port on a block node.
 * Styled as small black circles to match MATLAB Simulink port appearance.
 */
public class PortNode extends Circle {

    private static final double RADIUS = 4.0;
    private static final double STROKE_WIDTH = 1.0;
    private static final double HOVER_SCALE = 1.3;
    private static final Color PORT_COLOR = Color.BLACK;

    private final Port modelPort;
    private final BlockNode parentBlockNode;

    public PortNode(Port modelPort, BlockNode parentBlockNode) {
        super(RADIUS);
        this.modelPort = modelPort;
        this.parentBlockNode = parentBlockNode;

        configureAppearance();
        configureHoverEffects();
    }

    private void configureAppearance() {
        setFill(PORT_COLOR);
        setStroke(PORT_COLOR);
        setStrokeWidth(STROKE_WIDTH);
    }

    private void configureHoverEffects() {
        setOnMouseEntered(event -> {
            setScaleX(HOVER_SCALE);
            setScaleY(HOVER_SCALE);
        });

        setOnMouseExited(event -> {
            setScaleX(1.0);
            setScaleY(1.0);
        });
    }

    public Port getModelPort() {
        return modelPort;
    }

    public BlockNode getParentBlockNode() {
        return parentBlockNode;
    }

    /**
     * Returns the X center of this port in workspace (parent of BlockNode) coordinates.
     */
    public double getCenterInWorkspaceX() {
        return parentBlockNode.getLayoutX() + getLayoutX();
    }

    /**
     * Returns the Y center of this port in workspace (parent of BlockNode) coordinates.
     */
    public double getCenterInWorkspaceY() {
        return parentBlockNode.getLayoutY() + getLayoutY();
    }

    /**
     * Returns the center position of this port in workspace coordinates as a Point2D.
     */
    public Point2D getCenterInWorkspace() {
        return new Point2D(getCenterInWorkspaceX(), getCenterInWorkspaceY());
    }
}
