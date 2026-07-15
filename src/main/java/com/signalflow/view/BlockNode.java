package com.signalflow.view;

import com.signalflow.model.Block;
import com.signalflow.model.Port;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base visual block component — Simulink style.
 * Small white rectangular blocks with thin black border and centered name label.
 * Each concrete subclass can optionally provide an icon character.
 */
public abstract class BlockNode extends Group {

    private final Block modelBlock;
    private final List<PortNode> inputPortNodes;
    private final List<PortNode> outputPortNodes;

    private Rectangle bodyRect;
    private Label titleLabel;

    // Drag state
    private double dragOffsetX;
    private double dragOffsetY;

    protected BlockNode(Block modelBlock) {
        this.modelBlock = modelBlock;
        this.inputPortNodes = new ArrayList<>();
        this.outputPortNodes = new ArrayList<>();

        buildUI();
        configureDragBehavior();
    }

    // --- Abstract methods for subclass customization ---

    /**
     * Returns the primary color for this block type (used as a subtle accent).
     */
    protected abstract Color getBlockColor();

    /**
     * Creates the custom content region to be placed inside the body area.
     * For Simulink-style blocks, this typically returns null (no embedded content).
     */
    protected abstract Region createContent();

    // --- Overridable dimensions (small Simulink-style) ---

    protected double getBlockWidth() {
        return 80.0;
    }

    protected double getBlockHeight() {
        return 50.0;
    }

    /**
     * Returns an optional icon/symbol character for the block label.
     * Subclasses can override to provide a block-specific icon.
     */
    protected String getIconSymbol() {
        return "";
    }

    // --- UI construction ---

    private void buildUI() {
        double width = getBlockWidth();
        double height = getBlockHeight();

        buildBody(width, height);
        buildTitle(width, height);
        buildPorts(width, height);

        getChildren().addAll(bodyRect, titleLabel);

        Region content = createContent();
        if (content != null) {
            StackPane contentWrapper = new StackPane(content);
            contentWrapper.setLayoutX(2);
            contentWrapper.setLayoutY(2);
            contentWrapper.setPrefSize(width - 4, height - 4);
            contentWrapper.setMaxSize(width - 4, height - 4);
            contentWrapper.setAlignment(Pos.CENTER);
            getChildren().add(contentWrapper);
        }

        // Add port nodes on top of everything
        getChildren().addAll(inputPortNodes);
        getChildren().addAll(outputPortNodes);
    }

    private void buildBody(double width, double height) {
        bodyRect = new Rectangle(width, height);
        bodyRect.setFill(Color.WHITE);
        bodyRect.setStroke(Color.web("#333333"));
        bodyRect.setStrokeWidth(1.0);
        // Sharp corners — Simulink style (no arc)
        bodyRect.setArcWidth(0);
        bodyRect.setArcHeight(0);
    }

    private void buildTitle(double width, double height) {
        String icon = getIconSymbol();
        String displayName = icon.isEmpty()
                ? modelBlock.getName()
                : icon + " " + modelBlock.getName();

        titleLabel = new Label(displayName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        titleLabel.setTextFill(Color.web("#222222"));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPrefWidth(width);
        titleLabel.setPrefHeight(height);
        titleLabel.setMaxWidth(width);
    }

    private void buildPorts(double width, double height) {
        List<Port> inputs = modelBlock.getInputPorts();
        List<Port> outputs = modelBlock.getOutputPorts();

        // Position input ports along the left edge
        for (int i = 0; i < inputs.size(); i++) {
            PortNode portNode = new PortNode(inputs.get(i), this);
            double ySpacing = height / (inputs.size() + 1);
            portNode.setLayoutX(-4);
            portNode.setLayoutY(ySpacing * (i + 1));
            inputPortNodes.add(portNode);
        }

        // Position output ports along the right edge
        for (int i = 0; i < outputs.size(); i++) {
            PortNode portNode = new PortNode(outputs.get(i), this);
            double ySpacing = height / (outputs.size() + 1);
            portNode.setLayoutX(width + 4);
            portNode.setLayoutY(ySpacing * (i + 1));
            outputPortNodes.add(portNode);
        }
    }

    // --- Drag behavior ---

    private void configureDragBehavior() {
        setOnMousePressed(event -> {
            dragOffsetX = event.getSceneX() - getLayoutX();
            dragOffsetY = event.getSceneY() - getLayoutY();
            toFront();
            event.consume();
        });

        setOnMouseDragged(event -> {
            setLayoutX(event.getSceneX() - dragOffsetX);
            setLayoutY(event.getSceneY() - dragOffsetY);
            event.consume();
        });
    }

    // --- Public API ---

    public Block getModelBlock() {
        return modelBlock;
    }

    public List<PortNode> getInputPortNodes() {
        return Collections.unmodifiableList(inputPortNodes);
    }

    public List<PortNode> getOutputPortNodes() {
        return Collections.unmodifiableList(outputPortNodes);
    }

    /**
     * Finds the PortNode that represents the given model Port, or null.
     */
    public PortNode findPortNode(Port port) {
        for (PortNode pn : inputPortNodes) {
            if (pn.getModelPort() == port) {
                return pn;
            }
        }
        for (PortNode pn : outputPortNodes) {
            if (pn.getModelPort() == port) {
                return pn;
            }
        }
        return null;
    }
}
