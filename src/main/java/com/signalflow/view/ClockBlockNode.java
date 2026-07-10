package com.signalflow.view;

import com.signalflow.model.ClockBlock;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual representation of a ClockBlock.
 * Shows current time and provides a frequency slider.
 */
public class ClockBlockNode extends BlockNode {

    public ClockBlockNode(ClockBlock modelBlock) {
        super(modelBlock);
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#42A5F5");
    }

    @Override
    protected double getBlockHeight() {
        return 150.0;
    }

    @Override
    protected Region createContent() {
        ClockBlock clock = (ClockBlock) getModelBlock();

        Label iconLabel = new Label("\u23F1 Clock");
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        iconLabel.setTextFill(Color.WHITE);

        Label timeLabel = new Label(String.format("t = %.3f", clock.getTime()));
        timeLabel.setFont(Font.font("Consolas", 12));
        timeLabel.setTextFill(Color.web("#B0BEC5"));

        Label freqLabel = new Label("Freq:");
        freqLabel.setFont(Font.font("System", 11));
        freqLabel.setTextFill(Color.web("#B0BEC5"));

        Slider slider = new Slider(0.1, 5.0, clock.getFrequency());
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1.0);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.1);
        slider.setPrefWidth(130);

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                clock.setFrequency(newVal.doubleValue()));

        VBox box = new VBox(4, iconLabel, timeLabel, freqLabel, slider);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(4));

        return box;
    }

    /**
     * Called externally each frame to refresh displayed time and frequency values.
     */
    public void updateDisplay() {
        ClockBlock clock = (ClockBlock) getModelBlock();
        // Walk the children to find the time label for update
        // The content wrapper is child index 3 (body=0, header=1, title=2, content=3)
        // Since we can't hold a final reference from createContent(), we traverse.
        getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .map(n -> (StackPane) n)
                .findFirst()
                .ifPresent(wrapper -> {
                    if (wrapper.getChildren().getFirst() instanceof VBox vbox) {
                        if (vbox.getChildren().size() >= 2
                                && vbox.getChildren().get(1) instanceof Label label) {
                            label.setText(String.format("t = %.3f", clock.getTime()));
                        }
                    }
                });
    }
}
