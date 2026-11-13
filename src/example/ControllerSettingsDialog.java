package controller;

import arc.input.KeyCode;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.ui.dialogs.BaseDialog;

public class ControllerSettingsDialog extends BaseDialog {

    public ControllerSettingsDialog() {
        super("Controller & Gyro Settings");
        addCloseButton();
        shown(this::rebuild);
    }

    public void rebuild() {
        cont.clear();
        cont.defaults().pad(6f).left();

        Table table = new Table();
        table.defaults().pad(5f).left();

        // 1. Master Enable Toggle
        table.check("Enable Controller Support", ControllerConfig.enabled, val -> {
            ControllerConfig.enabled = val;
            ControllerConfig.save();
        }).colspan(2).row();

        // 2. Optional Gyro Toggle
        table.check("Enable Gyro / Motion Aiming", ControllerConfig.gyroEnabled, val -> {
            ControllerConfig.gyroEnabled = val;
            ControllerConfig.save();
        }).colspan(2).row();

        // 3. Sensitivity & Deadzone Sliders
        table.add("Cursor Speed: ");
        table.slider(2f, 30f, 1f, ControllerConfig.cursorSpeed, val -> {
            ControllerConfig.cursorSpeed = val;
            ControllerConfig.save();
        }).width(200f).row();

        table.add("Gyro Sensitivity: ");
        table.slider(1f, 25f, 1f, ControllerConfig.gyroSensitivity, val -> {
            ControllerConfig.gyroSensitivity = val;
            ControllerConfig.save();
        }).width(200f).row();

        table.add("Stick Deadzone: ");
        table.slider(0.05f, 0.45f, 0.05f, ControllerConfig.deadzone, val -> {
            ControllerConfig.deadzone = val;
            ControllerConfig.save();
        }).width(200f).row();

        // 4. Keybind Section
        table.add("[accent]Button Bindings (Tap to Rebind)[]").padTop(12f).colspan(2).row();

        addBindRow(table, "Select / Place / Shoot:", ControllerConfig.bindAction, code -> ControllerConfig.bindAction = code);
        addBindRow(table, "Break Block / Cancel:", ControllerConfig.bindBreak, code -> ControllerConfig.bindBreak = code);
        addBindRow(table, "Rotate Building:", ControllerConfig.bindRotate, code -> ControllerConfig.bindRotate = code);
        addBindRow(table, "Toggle Build Menu:", ControllerConfig.bindToggleBuild, code -> ControllerConfig.bindToggleBuild = code);
        addBindRow(table, "Pause Game:", ControllerConfig.bindPause, code -> ControllerConfig.bindPause = code);

        cont.pane(table).grow();
    }

    private void addBindRow(Table table, String label, KeyCode currentKey, KeySetter setter) {
        table.add(label);
        TextButton[] btnHolder = new TextButton[1];
        btnHolder[0] = table.button(currentKey.name(), () -> {
            btnHolder[0].setText("[accent]Press Controller Button...[]");
            ControllerHandler.activeRebindListener = newCode -> {
                setter.set(newCode);
                ControllerConfig.save();
                rebuild();
            };
        }).width(220f).get();
        table.row();
    }

    private interface KeySetter {
        void set(KeyCode code);
    }
}
