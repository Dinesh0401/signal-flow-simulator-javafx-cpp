package com.signalflow.view;

import com.signalflow.model.Block;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Visual representation of a SineBlock.
 * Displays sine wave symbol and current output value.
 */
public class SineBlockNode extends BlockNode {

    public SineBlockNode(Block modelBlock) {
        super(modelBlock);
    }

    @Override
    protected Color getBlockColor() {
        return Color.web("#66BB6A");
    }

    @Override
    protected Region createContent() {
        Label iconLabel = new Label("\u223F Sine");
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        iconLabel.setTextFill(Color.WHITE);

        Label outputLabel = new Label("out = 0.000");
        outputLabel.setFont(Font.font("Consolas", 12));
        outputLabel.setTextFill(Color.web("#B0BEC5"));

        VBox box = new VBox(8, iconLabel, outputLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(4));

        return box;
    }

    /**
     * Called externally each frame to refresh the displayed output value.
     */
    public void updateDisplay() {
        double value = getModelBlock().getOutputPort("out").getValue();
        getChildren().stream()
                .filter(n -> n instanceof StackPane)
                .map(n -> (StackPane) n)
                .findFirst()
                .ifPresent(wrapper -> {
                    if (wrapper.getChildren().getFirst() instanceof VBox vbox) {
                        if (vbox.getChildren().size() >= 2
                                && vbox.getChildren().get(1) instanceof Label label) {
                            label.setText(String.format("out = %.3f", value));
                        }
                    }
                });
    }
}
