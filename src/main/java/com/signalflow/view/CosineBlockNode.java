package com.signalflow.view;

import com.signalflow.model.Block;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Visual representation of a CosineBlock — Simulink style.
 * Small rectangular block with "∿ Cosine" label.
 * Double-clicking opens a unified "Block Properties" popup (Simulink-style)
 * with name, type, function, and live I/O values.
 */
public class CosineBlockNode extends BlockNode {

    private Stage propertiesStage;
    private Label inputLabel;
    private Label outputLabel;

    public CosineBlockNode(Block modelBlock) {
        super(modelBlock);
        configureDoubleClick();
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#FFA726");
    }

    @Override
    protected String getIconSymbol() {
        return "\u223F";
    }

    @Override
    protected Region createContent() {
        return null;
    }

    private void configureDoubleClick() {
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openPropertiesDialog();
                event.consume();
            }
        });
    }

    private void openPropertiesDialog() {
        if (propertiesStage != null && propertiesStage.isShowing()) {
            propertiesStage.toFront();
            return;
        }

        Block block = getModelBlock();

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // ── Header ──────────────────────────────────────────────────────
        Label header = new Label("Block Properties");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        // ── Properties grid ─────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 8, 0));

        int row = 0;

        // Name
        grid.add(makePropertyLabel("Name:"), 0, row);
        grid.add(makeValueLabel(block.getName()), 1, row++);

        // Type
        grid.add(makePropertyLabel("Type:"), 0, row);
        grid.add(makeValueLabel("CosineBlock"), 1, row++);

        // Function
        grid.add(makePropertyLabel("Function:"), 0, row);
        grid.add(makeValueLabel("cos(t)"), 1, row++);

        // Input
        grid.add(makePropertyLabel("Input:"), 0, row);
        inputLabel = makeValueLabel("0.000");
        grid.add(inputLabel, 1, row++);

        // Output
        grid.add(makePropertyLabel("Output:"), 0, row);
        outputLabel = makeValueLabel("0.000");
        grid.add(outputLabel, 1, row++);

        root.getChildren().addAll(header, new Separator(), grid);

        Scene scene = new Scene(root, 280, 230);
        propertiesStage = new Stage();
        propertiesStage.setTitle("Properties - " + block.getName());
        propertiesStage.setScene(scene);
        propertiesStage.setResizable(false);
        propertiesStage.setOnCloseRequest(e -> {
            propertiesStage = null;
            inputLabel = null;
            outputLabel = null;
        });

        // Inherit main app styling
        if (getScene() != null && !getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }

        propertiesStage.show();
    }

    /**
     * Called externally each frame to refresh the displayed values.
     */
    public void updateDisplay() {
        if (inputLabel != null && getModelBlock().getInputPorts().size() > 0) {
            double inVal = getModelBlock().getInputPorts().get(0).getValue();
            inputLabel.setText(String.format("%.3f", inVal));
        }
        if (outputLabel != null && getModelBlock().getOutputPorts().size() > 0) {
            double outVal = getModelBlock().getOutputPorts().get(0).getValue();
            outputLabel.setText(String.format("%.3f", outVal));
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private Label makePropertyLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #555555;");
        return label;
    }

    private Label makeValueLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Consolas", 12));
        label.setStyle("-fx-text-fill: #222222;");
        return label;
    }
}
