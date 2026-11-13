package controller;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.PlayLoadEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

public class ControllerMod extends Mod {
    public static ControllerSettingsDialog settingsDialog;
    public static GyroToggleFragment gyroFragment;

    @Override
    public void init() {
        ControllerConfig.load();
        ControllerHandler.init();
        gyroFragment = new GyroToggleFragment();

        Events.on(ClientLoadEvent.class, e -> {
            settingsDialog = new ControllerSettingsDialog();

            // Add our controller settings category to Mindustry's main settings menu
            Vars.ui.settings.addCategory("Controller", Icon.play, table -> {
                table.button("Open Controller & Gyro Settings", () -> settingsDialog.show()).size(280f, 54f);
            });
        });

        // Show floating gyro button when loading into a game match
        Events.on(WorldLoadEvent.class, e -> {
            if (gyroFragment != null) {
                gyroFragment.remove(); // Clean up old instance if any
            }
            gyroFragment.build(Vars.ui.hudGroup);
        });
    }
}
