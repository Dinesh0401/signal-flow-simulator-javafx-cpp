package com.signalflow.view;

import com.signalflow.model.Port;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Visual representation of a port on a block node.
 * Input ports are light blue, output ports are coral.
 */
public class PortNode extends Circle {

    private static final double RADIUS = 8.0;
    private static final double STROKE_WIDTH = 2.0;
    private static final double HOVER_SCALE = 1.3;
    private static final Color INPUT_COLOR = Color.web("#4FC3F7");
    private static final Color OUTPUT_COLOR = Color.web("#FF8A65");

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
        boolean isInput = modelPort.getDirection() == Port.Direction.IN;
        Color portColor = isInput ? INPUT_COLOR : OUTPUT_COLOR;

        setFill(portColor);
        setStroke(portColor.darker());
        setStrokeWidth(STROKE_WIDTH);
    }

    private void configureHoverEffects() {
        DropShadow glow = new DropShadow();
        glow.setColor(((Color) getFill()).brighter());
        glow.setRadius(12.0);
        glow.setSpread(0.4);

        setOnMouseEntered(event -> {
            setScaleX(HOVER_SCALE);
            setScaleY(HOVER_SCALE);
            setEffect(glow);
        });

        setOnMouseExited(event -> {
            setScaleX(1.0);
            setScaleY(1.0);
            setEffect(null);
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
