package com.signalflow.view;

import com.signalflow.model.ClockBlock;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Visual representation of a ClockBlock — Simulink style.
 * Small rectangular block with "Clock" label.
 * Double-clicking opens a unified "Block Properties" popup (Simulink-style)
 * with name, type, parameters (frequency), and live output.
 */
public class ClockBlockNode extends BlockNode {

    private Stage propertiesStage;
    private Label timeLabel;
    private Label outputLabel;
    private Slider freqSlider;
    private Label freqValueLabel;

    public ClockBlockNode(ClockBlock modelBlock) {
        super(modelBlock);
        configureDoubleClick();
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#42A5F5");
    }

    @Override
    protected String getIconSymbol() {
        return "\u23F1";
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

        ClockBlock clock = (ClockBlock) getModelBlock();

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
        grid.add(makeValueLabel(clock.getName()), 1, row++);

        // Type
        grid.add(makePropertyLabel("Type:"), 0, row);
        grid.add(makeValueLabel("ClockBlock"), 1, row++);

        // Output port
        grid.add(makePropertyLabel("Output:"), 0, row);
        outputLabel = makeValueLabel("0.000");
        grid.add(outputLabel, 1, row++);

        // Time
        grid.add(makePropertyLabel("Time:"), 0, row);
        timeLabel = makeValueLabel("0.000 s");
        grid.add(timeLabel, 1, row++);

        // ── Parameters section ──────────────────────────────────────────
        Label paramsHeader = new Label("Parameters");
        paramsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        paramsHeader.setPadding(new Insets(4, 0, 0, 0));

        GridPane paramsGrid = new GridPane();
        paramsGrid.setHgap(16);
        paramsGrid.setVgap(8);

        freqValueLabel = makeValueLabel(String.format("%.2f Hz", clock.getFrequency()));
        paramsGrid.add(makePropertyLabel("Frequency:"), 0, 0);
        paramsGrid.add(freqValueLabel, 1, 0);

        freqSlider = new Slider(0.1, 5.0, clock.getFrequency());
        freqSlider.setShowTickMarks(true);
        freqSlider.setShowTickLabels(true);
        freqSlider.setMajorTickUnit(1.0);
        freqSlider.setPrefWidth(200);

        freqSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            clock.setFrequency(newVal.doubleValue());
            freqValueLabel.setText(String.format("%.2f Hz", newVal.doubleValue()));
        });

        root.getChildren().addAll(
                header, new Separator(),
                grid,
                new Separator(),
                paramsHeader, paramsGrid, freqSlider);

        Scene scene = new Scene(root, 320, 310);
        propertiesStage = new Stage();
        propertiesStage.setTitle("Properties - " + clock.getName());
        propertiesStage.setScene(scene);
        propertiesStage.setResizable(false);
        propertiesStage.setOnCloseRequest(e -> {
            propertiesStage = null;
            timeLabel = null;
            outputLabel = null;
            freqSlider = null;
            freqValueLabel = null;
        });

        // Inherit main app styling
        if (getScene() != null && !getScene().getStylesheets().isEmpty()) {
            scene.getStylesheets().addAll(getScene().getStylesheets());
        }

        propertiesStage.show();
    }

    /**
     * Called externally each frame to refresh displayed values.
     */
    public void updateDisplay() {
        if (timeLabel != null) {
            ClockBlock clock = (ClockBlock) getModelBlock();
            timeLabel.setText(String.format("%.3f s", clock.getTime()));
        }
        if (outputLabel != null) {
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
