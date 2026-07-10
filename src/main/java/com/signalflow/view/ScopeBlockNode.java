package com.signalflow.view;

import com.signalflow.model.ScopeBlock;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Visual representation of a ScopeBlock with an embedded waveform canvas.
 * Plots signal data in real time with auto-scaled Y axis.
 */
public class ScopeBlockNode extends BlockNode {

    private static final double CANVAS_WIDTH = 260.0;
    private static final double CANVAS_HEIGHT = 140.0;
    private static final Color BACKGROUND_COLOR = Color.web("#1a1a2e");
    private static final Color GRID_COLOR = Color.web("#333355");
    private static final Color WAVEFORM_COLOR = Color.web("#00E676");
    private static final Color TEXT_COLOR = Color.web("#90CAF9");
    private static final Color CENTER_LINE_COLOR = Color.web("#444477");
    private static final double GRID_SPACING = 20.0;
    private static final double WAVEFORM_LINE_WIDTH = 2.0;

    private Canvas waveformCanvas;

    public ScopeBlockNode(ScopeBlock modelBlock) {
        super(modelBlock);
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#AB47BC");
    }

    @Override
    protected double getBlockWidth() {
        return 280.0;
    }

    @Override
    protected double getBlockHeight() {
        return 200.0;
    }

    @Override
    protected Region createContent() {
        waveformCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw initial empty state
        clearCanvas();

        StackPane canvasPane = new StackPane(waveformCanvas);
        canvasPane.setPadding(new Insets(2));
        return canvasPane;
    }

    /**
     * Called every animation frame to redraw the waveform plot from the model's buffer.
     */
    public void updatePlot() {
        ScopeBlock scope = (ScopeBlock) getModelBlock();
        double[] buffer = scope.getBufferSnapshot();
        int sampleCount = scope.getSampleCount();

        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        double w = waveformCanvas.getWidth();
        double h = waveformCanvas.getHeight();

        // 1. Clear with dark background
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, w, h);

        // 2. Draw grid dots
        gc.setFill(GRID_COLOR);
        for (double gx = GRID_SPACING; gx < w; gx += GRID_SPACING) {
            for (double gy = GRID_SPACING; gy < h; gy += GRID_SPACING) {
                gc.fillOval(gx - 1, gy - 1, 2, 2);
            }
        }

        // 3. Draw horizontal center line
        gc.setStroke(CENTER_LINE_COLOR);
        gc.setLineWidth(1.0);
        gc.strokeLine(0, h / 2.0, w, h / 2.0);

        if (buffer == null || sampleCount == 0) {
            return;
        }

        // Determine actual number of samples to plot
        int count = Math.min(sampleCount, buffer.length);
        if (count < 2) {
            return;
        }

        // 4. Compute min/max for auto-scaling
        double minVal = Double.MAX_VALUE;
        double maxVal = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            double v = buffer[i];
            if (v < minVal) minVal = v;
            if (v > maxVal) maxVal = v;
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

        // 5. Draw the waveform as connected line segments
        gc.setStroke(WAVEFORM_COLOR);
        gc.setLineWidth(WAVEFORM_LINE_WIDTH);
        gc.beginPath();

        double xStep = w / (count - 1);
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

        // 6. Draw Y-axis labels
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Consolas", 9));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.format("%.2f", maxVal), 2, 10);
        gc.fillText(String.format("%.2f", (maxVal + minVal) / 2.0), 2, h / 2.0 - 3);
        gc.fillText(String.format("%.2f", minVal), 2, h - 3);

        // 7. Draw current value in top-right corner
        double currentValue = buffer[count - 1];
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Consolas", 11));
        gc.setFill(WAVEFORM_COLOR);
        gc.fillText(String.format("%.4f", currentValue), w - 4, 14);
    }

    private void clearCanvas() {
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw grid
        gc.setFill(GRID_COLOR);
        for (double gx = GRID_SPACING; gx < CANVAS_WIDTH; gx += GRID_SPACING) {
            for (double gy = GRID_SPACING; gy < CANVAS_HEIGHT; gy += GRID_SPACING) {
                gc.fillOval(gx - 1, gy - 1, 2, 2);
            }
        }

        // Center line
        gc.setStroke(CENTER_LINE_COLOR);
        gc.setLineWidth(1.0);
        gc.strokeLine(0, CANVAS_HEIGHT / 2.0, CANVAS_WIDTH, CANVAS_HEIGHT / 2.0);
    }
}
