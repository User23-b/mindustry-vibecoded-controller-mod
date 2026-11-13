package controller;

import arc.Core;
import arc.input.KeyCode;

public class ControllerConfig {
    // Toggle and sensitivity settings
    public static boolean enabled = true;
    public static boolean gyroEnabled = false;
    public static float cursorSpeed = 12f;
    public static float gyroSensitivity = 8f;
    public static float deadzone = 0.15f;

    // Default Keybinds (using Arc's cross-platform controller KeyCodes)
    public static KeyCode bindAction = KeyCode.controllerA;      // Place block / Shoot / Select
    public static KeyCode bindBreak = KeyCode.controllerB;       // Break block / Cancel
    public static KeyCode bindRotate = KeyCode.controllerX;      // Rotate block
    public static KeyCode bindToggleBuild = KeyCode.controllerY; // Open/Close build menu
    public static KeyCode bindPause = KeyCode.controllerStart;   // Pause game

    /** Loads saved settings from disk when the mod starts. */
    public static void load() {
        enabled = Core.settings.getBool("ctrl-enabled", true);
        gyroEnabled = Core.settings.getBool("ctrl-gyro", false);
        cursorSpeed = Core.settings.getFloat("ctrl-cursor-speed", 12f);
        gyroSensitivity = Core.settings.getFloat("ctrl-gyro-sens", 8f);
        deadzone = Core.settings.getFloat("ctrl-deadzone", 0.15f);

        bindAction = getKey("ctrl-bind-action", KeyCode.controllerA);
        bindBreak = getKey("ctrl-bind-break", KeyCode.controllerB);
        bindRotate = getKey("ctrl-bind-rotate", KeyCode.controllerX);
        bindToggleBuild = getKey("ctrl-bind-build", KeyCode.controllerY);
        bindPause = getKey("ctrl-bind-pause", KeyCode.controllerStart);
    }

    /** Saves current settings to disk. */
    public static void save() {
        Core.settings.put("ctrl-enabled", enabled);
        Core.settings.put("ctrl-gyro", gyroEnabled);
        Core.settings.put("ctrl-cursor-speed", cursorSpeed);
        Core.settings.put("ctrl-gyro-sens", gyroSensitivity);
        Core.settings.put("ctrl-deadzone", deadzone);

        Core.settings.put("ctrl-bind-action", bindAction.name());
        Core.settings.put("ctrl-bind-break", bindBreak.name());
        Core.settings.put("ctrl-bind-rotate", bindRotate.name());
        Core.settings.put("ctrl-bind-build", bindToggleBuild.name());
        Core.settings.put("ctrl-bind-pause", bindPause.name());
    }

    private static KeyCode getKey(String name, KeyCode def) {
        try {
            return KeyCode.valueOf(Core.settings.getString(name, def.name()));
        } catch (Exception e) {
            return def;
        }
    }
}
