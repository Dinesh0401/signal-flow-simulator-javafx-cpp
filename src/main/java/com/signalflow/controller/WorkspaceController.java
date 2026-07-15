package com.signalflow.controller;

import com.signalflow.model.Block;
import com.signalflow.model.BlockGraph;
import com.signalflow.model.ClockBlock;
import com.signalflow.model.Connection;
import com.signalflow.model.CosineBlock;
import com.signalflow.model.Port;
import com.signalflow.model.ScopeBlock;
import com.signalflow.model.SineBlock;
import com.signalflow.view.BlockNode;
import com.signalflow.view.ClockBlockNode;
import com.signalflow.view.CosineBlockNode;
import com.signalflow.view.PortNode;
import com.signalflow.view.ScopeBlockNode;
import com.signalflow.view.SineBlockNode;
import com.signalflow.view.WireNode;
import com.signalflow.view.WorkspaceCanvas;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Central controller that manages the workspace: adding/removing blocks and
 * wires, and handling the interactive port-to-port connection workflow.
 */
public class WorkspaceController {

    private final WorkspaceCanvas canvas;
    private final BlockGraph graph;
    private final List<BlockNode> blockNodes = new ArrayList<>();
    private final List<WireNode> wireNodes = new ArrayList<>();

    // Transient state for the "drag-a-wire" interaction
    private PortNode connectionSource;
    private Line tempWire;

    // Undo stack for actions
    private final Deque<Runnable> undoStack = new ArrayDeque<>();

    public WorkspaceController(WorkspaceCanvas canvas, BlockGraph graph) {
        this.canvas = canvas;
        this.graph = graph;
        installCanvasHandlers();
    }

    // ── Block lifecycle ─────────────────────────────────────────────────

    /**
     * Factory method: creates a model block + its corresponding view node,
     * wires up port interaction handlers, and places it on the canvas.
     *
     * @param type one of "Clock", "Sine", "Cosine", "Scope"
     * @param x    initial layout-X on the canvas
     * @param y    initial layout-Y on the canvas
     * @return the newly created BlockNode (also added to internal list)
     */
    public BlockNode addBlock(String type, double x, double y) {
        Block modelBlock = createModelBlock(type);
        BlockNode viewNode = createViewNode(type, modelBlock);

        graph.addBlock(modelBlock);
        canvas.addBlockNode(viewNode, x, y);
        blockNodes.add(viewNode);

        setupPortHandlers(viewNode);
        enableBlockDragging(viewNode);

        undoStack.push(() -> removeBlock(viewNode));

        return viewNode;
    }

    /**
     * Programmatically connects two blocks (for workspace seeding).
     */
    public void connectBlocks(BlockNode src, String srcPortName, BlockNode dst, String dstPortName) {
        PortNode sourcePortNode = src.findPortNode(src.getModelBlock().getOutputPort(srcPortName));
        PortNode destPortNode = dst.findPortNode(dst.getModelBlock().getInputPort(dstPortName));
        
        if (sourcePortNode != null && destPortNode != null) {
            Connection conn = graph.addConnection(sourcePortNode.getModelPort(), destPortNode.getModelPort());
            if (conn != null) {
                WireNode wire = new WireNode(sourcePortNode, destPortNode, conn);
                canvas.addWireNode(wire);
                wireNodes.add(wire);
            }
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
        }
    }

    public void clearUndoStack() {
        undoStack.clear();
    }

    /**
     * Removes a block and every wire attached to it.
     */
    public void removeBlock(BlockNode node) {
        // Remove all wires connected to any port of this block
        Iterator<WireNode> it = wireNodes.iterator();
        while (it.hasNext()) {
            WireNode wire = it.next();
            if (wire.getSourcePortNode().getParentBlockNode() == node
                    || wire.getDestPortNode().getParentBlockNode() == node) {
                graph.removeConnection(wire.getModelConnection());
                canvas.removeWireNode(wire);
                it.remove();
            }
        }

        graph.removeBlock(node.getModelBlock());
        canvas.getBlockLayer().getChildren().remove(node);
        blockNodes.remove(node);
    }

    // ── Connection lifecycle ────────────────────────────────────────────

    /**
     * Removes a single wire (both model and view).
     */
    private void removeWire(WireNode wire) {
        graph.removeConnection(wire.getModelConnection());
        canvas.getWireLayer().getChildren().remove(wire);
        wireNodes.remove(wire);
    }

    // ── Accessors ───────────────────────────────────────────────────────

    public List<BlockNode> getBlockNodes() {
        return blockNodes;
    }

    public List<WireNode> getWireNodes() {
        return wireNodes;
    }

    public WorkspaceCanvas getCanvas() {
        return canvas;
    }

    public BlockGraph getGraph() {
        return graph;
    }

    // ── Port interaction handlers ───────────────────────────────────────

    /**
     * Installs mouse-press handlers on every output port (start a wire)
     * and mouse-release handlers on every input port (complete a wire).
     */
    private void setupPortHandlers(BlockNode node) {
        for (PortNode outPort : node.getOutputPortNodes()) {
            outPort.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown()) {
                    startConnection(outPort, event);
                    event.consume();
                }
            });
            
            outPort.setOnMouseDragged(event -> {
                if (event.isPrimaryButtonDown() && connectionSource != null) {
                    javafx.geometry.Point2D p = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
                    if (tempWire != null) {
                        tempWire.setEndX(p.getX());
                        tempWire.setEndY(p.getY());
                    }
                    event.consume();
                }
            });
            
            outPort.setOnMouseReleased(event -> {
                if (connectionSource != null) {
                    javafx.scene.Node picked = event.getPickResult().getIntersectedNode();
                    if (picked instanceof PortNode portNode && portNode.getModelPort().getDirection() == Port.Direction.IN) {
                        completeConnection(portNode);
                    } else {
                        cancelConnection();
                    }
                    event.consume();
                }
            });
        }
    }

    /**
     * Creates the temporary "rubber-band" wire (straight line) that follows the cursor.
     */
    private void startConnection(PortNode source, MouseEvent event) {
        connectionSource = source;

        double startX = source.getParentBlockNode().getLayoutX()
                + source.getLayoutX() + source.getRadius();
        double startY = source.getParentBlockNode().getLayoutY()
                + source.getLayoutY() + source.getRadius();

        tempWire = new Line(startX, startY, startX, startY);
        tempWire.setStroke(Color.web("#666666", 0.7));
        tempWire.setStrokeWidth(1.5);
        tempWire.setStrokeLineCap(StrokeLineCap.ROUND);
        tempWire.getStrokeDashArray().addAll(6.0, 3.0);
        tempWire.setMouseTransparent(true);

        canvas.getWireLayer().getChildren().add(tempWire);
    }

    /**
     * Validates the connection and, if legal, creates the model Connection
     * and the visual WireNode.
     */
    private void completeConnection(PortNode destPort) {
        if (connectionSource == null) {
            return;
        }

        // Guard: must not connect ports on the same block
        if (destPort.getParentBlockNode() == connectionSource.getParentBlockNode()) {
            cancelConnection();
            return;
        }

        // Guard: destination port must not already have an incoming wire
        for (WireNode existing : wireNodes) {
            if (existing.getDestPortNode() == destPort) {
                cancelConnection();
                return;
            }
        }

        Port sourceModelPort = connectionSource.getModelPort();
        Port destModelPort = destPort.getModelPort();

        Connection connection = graph.addConnection(sourceModelPort, destModelPort);
        if (connection != null) {
            WireNode wire = new WireNode(connectionSource, destPort, connection);
            canvas.addWireNode(wire);
            wireNodes.add(wire);
            
            undoStack.push(() -> removeWire(wire));
        }

        cancelConnection();
    }

    /**
     * Removes the temporary wire and clears the connection-in-progress state.
     */
    private void cancelConnection() {
        if (tempWire != null) {
            canvas.getWireLayer().getChildren().remove(tempWire);
            tempWire = null;
        }
        connectionSource = null;
    }

    // ── Canvas-level mouse handlers ─────────────────────────────────────

    private void installCanvasHandlers() {
        // Empty now: PortNode handles all connection interactions securely.
    }

    private void updateTempWire(MouseEvent event) {
        if (tempWire == null) {
            return;
        }
        tempWire.setEndX(event.getX());
        tempWire.setEndY(event.getY());
    }

    // ── Block dragging ──────────────────────────────────────────────────

    /**
     * Makes a block node draggable on the canvas via primary mouse button.
     */
    private void enableBlockDragging(BlockNode node) {
        final double[] dragAnchor = new double[2];
        final double[] originalPos = new double[2];

        node.setOnMousePressed(event -> {
            if (event.getTarget() instanceof PortNode) {
                return; // Let port handlers process this
            }
            if (event.isPrimaryButtonDown()) {
                dragAnchor[0] = event.getSceneX() - node.getLayoutX();
                dragAnchor[1] = event.getSceneY() - node.getLayoutY();
                originalPos[0] = node.getLayoutX();
                originalPos[1] = node.getLayoutY();
            }
        });

        node.setOnMouseDragged(event -> {
            if (event.getTarget() instanceof PortNode) {
                return; // Let port handlers or canvas process this
            }
            if (event.isPrimaryButtonDown()) {
                // If we're currently drawing a wire, don't drag
                if (connectionSource != null) {
                    return;
                }
                node.setLayoutX(event.getSceneX() - dragAnchor[0]);
                node.setLayoutY(event.getSceneY() - dragAnchor[1]);
                event.consume();
            }
        });

        node.setOnMouseReleased(event -> {
            double finalX = node.getLayoutX();
            double finalY = node.getLayoutY();
            double oldX = originalPos[0];
            double oldY = originalPos[1];
            
            // Only push undo action if it actually moved
            if (Math.abs(finalX - oldX) > 1.0 || Math.abs(finalY - oldY) > 1.0) {
                undoStack.push(() -> {
                    node.setLayoutX(oldX);
                    node.setLayoutY(oldY);
                });
            }
        });
    }

    // ── Factory helpers ─────────────────────────────────────────────────

    private Block createModelBlock(String type) {
        return switch (type) {
            case "Clock" -> new ClockBlock("Clock");
            case "Sine" -> new SineBlock("Sine");
            case "Cosine" -> new CosineBlock("Cosine");
            case "Scope" -> new ScopeBlock("Scope");
            default -> throw new IllegalArgumentException("Unknown block type: " + type);
        };
    }

    private BlockNode createViewNode(String type, Block model) {
        return switch (type) {
            case "Clock" -> new ClockBlockNode((ClockBlock) model);
            case "Sine" -> new SineBlockNode((SineBlock) model);
            case "Cosine" -> new CosineBlockNode((CosineBlock) model);
            case "Scope" -> new ScopeBlockNode((ScopeBlock) model);
            default -> throw new IllegalArgumentException("Unknown block type: " + type);
        };
    }
}
