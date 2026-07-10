package com.signalflow.view;

import com.signalflow.model.Block;
import com.signalflow.model.Port;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base visual block component.
 * Each concrete subclass defines its color and custom content region.
 */
public abstract class BlockNode extends Group {

    private static final double HEADER_HEIGHT = 30.0;
    private static final double CORNER_RADIUS = 8.0;

    private final Block modelBlock;
    private final List<PortNode> inputPortNodes;
    private final List<PortNode> outputPortNodes;

    private Rectangle headerBar;
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
     * Returns the primary color for this block type.
     */
    protected abstract Color getBlockColor();

    /**
     * Creates the custom content region to be placed inside the body area.
     */
    protected abstract Region createContent();

    // --- Overridable dimensions ---

    protected double getBlockWidth() {
        return 160.0;
    }

    protected double getBlockHeight() {
        return 120.0;
    }

    // --- UI construction ---

    private void buildUI() {
        double width = getBlockWidth();
        double height = getBlockHeight();
        Color color = getBlockColor();

        buildHeader(width, color);
        buildBody(width, height, color);
        buildPorts(width, height);

        getChildren().addAll(bodyRect, headerBar, titleLabel);

        Region content = createContent();
        if (content != null) {
            StackPane contentWrapper = new StackPane(content);
            contentWrapper.setLayoutX(4);
            contentWrapper.setLayoutY(HEADER_HEIGHT + 4);
            contentWrapper.setPrefSize(width - 8, height - HEADER_HEIGHT - 8);
            contentWrapper.setMaxSize(width - 8, height - HEADER_HEIGHT - 8);
            contentWrapper.setAlignment(Pos.TOP_CENTER);
            contentWrapper.setPadding(new Insets(4));
            getChildren().add(contentWrapper);
        }

        // Add port nodes on top of everything
        getChildren().addAll(inputPortNodes);
        getChildren().addAll(outputPortNodes);
    }

    private void buildHeader(double width, Color color) {
        headerBar = new Rectangle(width, HEADER_HEIGHT);
        headerBar.setArcWidth(CORNER_RADIUS);
        headerBar.setArcHeight(CORNER_RADIUS);
        headerBar.setFill(color);

        titleLabel = new Label(modelBlock.getName());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setLayoutX(10);
        titleLabel.setLayoutY(5);
        titleLabel.setMaxWidth(width - 20);
    }

    private void buildBody(double width, double height, Color color) {
        bodyRect = new Rectangle(width, height);
        bodyRect.setArcWidth(CORNER_RADIUS);
        bodyRect.setArcHeight(CORNER_RADIUS);
        bodyRect.setFill(color.deriveColor(0, 0.6, 0.35, 1.0));
        bodyRect.setStroke(color.deriveColor(0, 0.8, 0.6, 0.7));
        bodyRect.setStrokeWidth(1.5);
    }



    private void buildPorts(double width, double height) {
        List<Port> inputs = modelBlock.getInputPorts();
        List<Port> outputs = modelBlock.getOutputPorts();

        double bodyTop = HEADER_HEIGHT;
        double bodyHeight = height - HEADER_HEIGHT;

        // Position input ports along the left edge
        for (int i = 0; i < inputs.size(); i++) {
            PortNode portNode = new PortNode(inputs.get(i), this);
            double ySpacing = bodyHeight / (inputs.size() + 1);
            portNode.setLayoutX(-8);
            portNode.setLayoutY(bodyTop + ySpacing * (i + 1));
            inputPortNodes.add(portNode);
        }

        // Position output ports along the right edge
        for (int i = 0; i < outputs.size(); i++) {
            PortNode portNode = new PortNode(outputs.get(i), this);
            double ySpacing = bodyHeight / (outputs.size() + 1);
            portNode.setLayoutX(width + 8);
            portNode.setLayoutY(bodyTop + ySpacing * (i + 1));
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
