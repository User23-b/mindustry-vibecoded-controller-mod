package controller;

import arc.Core;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.Vars;

public class GyroToggleFragment {
    private Table container;
    private TextButton toggleButton;

    /** Builds and adds the floating gyro button to the game HUD. */
    public void build(Table parent) {
        container = new Table();
        container.top().right(); // Position it in the top-right corner of the screen
        container.defaults().pad(10f);

        toggleButton = container.button(getButtonText(), () -> {
            // Toggle gyro state
            ControllerConfig.gyroEnabled = !ControllerConfig.gyroEnabled;
            ControllerConfig.save();

            // Update button appearance instantly
            toggleButton.setText(getButtonText());
        }).size(130f, 45f).get();

        // Style the button slightly smaller so it doesn't block gameplay view
        toggleButton.getLabel().setFontScale(0.85f);

        // Add the container to Mindustry's active HUD layout
        parent.add(container);
    }

    /** Removes the floating button when leaving a match. */
    public void remove() {
        if (container != null) {
            container.remove();
            container = null;
        }
    }

    private String getButtonText() {
        return ControllerConfig.gyroEnabled ? "[accent]GYRO: ON[]" : "[lightgray]GYRO: OFF[]";
    }
}
