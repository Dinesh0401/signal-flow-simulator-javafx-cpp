package com.signalflow.view;

import com.signalflow.model.ScopeBlock;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Visual representation of a ScopeBlock — Simulink style.
 * The block itself is a small rectangle labeled "Scope".
 * Double-clicking opens a separate popup Stage with a MATLAB-style scope display:
 * black background, yellow waveform, grid, live updating.
 */
public class ScopeBlockNode extends BlockNode {

    private static final double SCOPE_WINDOW_WIDTH = 500.0;
    private static final double SCOPE_WINDOW_HEIGHT = 350.0;
    private static final Color SCOPE_BG_COLOR = Color.BLACK;
    private static final Color SCOPE_GRID_COLOR = Color.web("#333333");
    private static final Color SCOPE_WAVEFORM_COLOR = Color.web("#FFD600");
    private static final Color SCOPE_TEXT_COLOR = Color.web("#AAAAAA");
    private static final Color SCOPE_CENTER_LINE_COLOR = Color.web("#555555");
    private static final double SCOPE_GRID_SPACING = 40.0;
    private static final double WAVEFORM_LINE_WIDTH = 1.5;

    private Stage scopeStage;
    private Canvas scopeCanvas;

    public ScopeBlockNode(ScopeBlock modelBlock) {
        super(modelBlock);
        configureDoubleClick();
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#AB47BC");
    }

    @Override
    protected String getIconSymbol() {
        return "\uD83D\uDCCA";
    }

    @Override
    protected Region createContent() {
        // Simulink-style: no embedded content, just the label
        return null;
    }

    /**
     * Configures double-click on the block to open the scope popup window.
     */
    private void configureDoubleClick() {
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openScopeWindow();
                event.consume();
            }
        });
    }

    /**
     * Opens (or brings to front) the scope popup window.
     */
    public void openScopeWindow() {
        if (scopeStage != null && scopeStage.isShowing()) {
            scopeStage.toFront();
            return;
        }

        scopeCanvas = new Canvas(SCOPE_WINDOW_WIDTH - 20, SCOPE_WINDOW_HEIGHT - 20);
        clearScopeCanvas();

        StackPane root = new StackPane(scopeCanvas);
        root.setStyle("-fx-background-color: black; -fx-padding: 10;");

        Scene scene = new Scene(root, SCOPE_WINDOW_WIDTH, SCOPE_WINDOW_HEIGHT);

        scopeStage = new Stage();
        scopeStage.setTitle(getModelBlock().getName());
        scopeStage.setScene(scene);
        scopeStage.setOnCloseRequest(e -> {
            scopeStage = null;
            scopeCanvas = null;
        });
        scopeStage.show();
    }

    /**
     * Called every animation frame to redraw the waveform plot.
     * If the popup scope window is open, draws to the popup canvas.
     */
    public void updatePlot() {
        if (scopeStage == null || !scopeStage.isShowing() || scopeCanvas == null) {
            return;
        }

        ScopeBlock scope = (ScopeBlock) getModelBlock();
        double[][] buffers = scope.getBufferSnapshots();
        int sampleCount = scope.getSampleCount();

        GraphicsContext gc = scopeCanvas.getGraphicsContext2D();
        double w = scopeCanvas.getWidth();
        double h = scopeCanvas.getHeight();

        // 1. Clear with black background
        gc.setFill(SCOPE_BG_COLOR);
        gc.fillRect(0, 0, w, h);

        // 2. Draw grid lines (MATLAB Scope style)
        gc.setStroke(SCOPE_GRID_COLOR);
        gc.setLineWidth(0.5);
        for (double gx = SCOPE_GRID_SPACING; gx < w; gx += SCOPE_GRID_SPACING) {
            gc.strokeLine(gx, 0, gx, h);
        }
        for (double gy = SCOPE_GRID_SPACING; gy < h; gy += SCOPE_GRID_SPACING) {
            gc.strokeLine(0, gy, w, gy);
        }

        // 3. Draw horizontal center line
        gc.setStroke(SCOPE_CENTER_LINE_COLOR);
        gc.setLineWidth(1.0);
        gc.strokeLine(0, h / 2.0, w, h / 2.0);

        // 4. Draw vertical center line
        gc.strokeLine(w / 2.0, 0, w / 2.0, h);

        if (buffers == null || sampleCount == 0) {
            return;
        }

        // Determine actual number of samples to plot
        int count = Math.min(sampleCount, buffers[0].length);
        if (count < 2) {
            return;
        }

        // 5. Compute min/max for auto-scaling across both buffers
        double minVal = Double.MAX_VALUE;
        double maxVal = -Double.MAX_VALUE;
        for (double[] buffer : buffers) {
            for (int i = 0; i < count; i++) {
                double v = buffer[i];
                if (v < minVal) minVal = v;
                if (v > maxVal) maxVal = v;
            }
        }

        // Ensure we have a non-zero range
        double range = maxVal - minVal;
        if (range < 1e-9) {
            range = 1.0;
            double center = (maxVal + minVal) / 2.0;
            minVal = center - 0.5;
            maxVal = center + 0.5;
        }

        // Add 10% padding to range
        double padding = range * 0.1;
        minVal -= padding;
        maxVal += padding;
        range = maxVal - minVal;

        // 6. Draw the waveforms as connected line segments
        Color[] channelColors = { SCOPE_WAVEFORM_COLOR, Color.web("#42A5F5") }; // Yellow, Blue
        
        double xStep = w / (count - 1);
        
        for (int ch = 0; ch < buffers.length; ch++) {
            double[] buffer = buffers[ch];
            gc.setStroke(channelColors[ch % channelColors.length]);
            gc.setLineWidth(WAVEFORM_LINE_WIDTH);
            gc.beginPath();

            for (int i = 0; i < count; i++) {
                double x = i * xStep;
                // Map value to canvas Y (inverted: top = max, bottom = min)
                double y = h - ((buffer[i] - minVal) / range) * h;

                if (i == 0) {
                    gc.moveTo(x, y);
                } else {
                    gc.lineTo(x, y);
                }
            }
            gc.stroke();
        }

        // 7. Draw Y-axis labels
        gc.setFill(SCOPE_TEXT_COLOR);
        gc.setFont(Font.font("Consolas", 10));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.format("%.2f", maxVal), 4, 14);
        gc.fillText(String.format("%.2f", (maxVal + minVal) / 2.0), 4, h / 2.0 - 4);
        gc.fillText(String.format("%.2f", minVal), 4, h - 4);

        // 8. Draw current values in top-right corner
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Consolas", 11));
        
        double currentVal1 = buffers[0][count - 1];
        gc.setFill(channelColors[0]);
        gc.fillText(String.format("CH1: %.4f", currentVal1), w - 4, 14);
        
        double currentVal2 = buffers[1][count - 1];
        gc.setFill(channelColors[1]);
        gc.fillText(String.format("CH2: %.4f", currentVal2), w - 4, 28);
    }

    private void clearScopeCanvas() {
        if (scopeCanvas == null) {
            return;
        }
        GraphicsContext gc = scopeCanvas.getGraphicsContext2D();
        double w = scopeCanvas.getWidth();
        double h = scopeCanvas.getHeight();

        gc.setFill(SCOPE_BG_COLOR);
        gc.fillRect(0, 0, w, h);

        // Draw grid
        gc.setStroke(SCOPE_GRID_COLOR);
        gc.setLineWidth(0.5);
        for (double gx = SCOPE_GRID_SPACING; gx < w; gx += SCOPE_GRID_SPACING) {
            gc.strokeLine(gx, 0, gx, h);
        }
        for (double gy = SCOPE_GRID_SPACING; gy < h; gy += SCOPE_GRID_SPACING) {
            gc.strokeLine(0, gy, w, gy);
        }

        // Center lines
        gc.setStroke(SCOPE_CENTER_LINE_COLOR);
        gc.setLineWidth(1.0);
        gc.strokeLine(0, h / 2.0, w, h / 2.0);
        gc.strokeLine(w / 2.0, 0, w / 2.0, h);
    }
}
