package controller;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.Dialog;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Player;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.MobileInput;
import mindustry.world.Tile;

public class ControllerHandler {
    // Virtual cursor position in screen coordinates
    public static float cursorX, cursorY;
    private static boolean initializedCursor = false;

    // Optional listener used when the user is rebinding a key in the settings window
    public static KeyBindListener activeRebindListener = null;

    public interface KeyBindListener {
        void onKeyCaptured(KeyCode code);
    }

    public static void init() {
        // Run update logic every frame during gameplay
        Events.run(Trigger.update, ControllerHandler::update);

        // Draw our custom crosshair cursor on top of the game world
        Events.run(Trigger.draw, ControllerHandler::drawCursor);
    }

    private static void update() {
        // 1. Check if we are currently waiting for a button press to rebind a control
        if (activeRebindListener != null) {
            for (KeyCode code : KeyCode.values()) {
                if (code.name().startsWith("controller") && Core.input.keyTap(code)) {
                    activeRebindListener.onKeyCaptured(code);
                    activeRebindListener = null;
                    return;
                }
            }
            return; // Pause normal controller actions while rebinding
        }

        if (!ControllerConfig.enabled || !Vars.state.isGame()) return;

        if (!initializedCursor) {
            cursorX = Core.graphics.getWidth() / 2f;
            cursorY = Core.graphics.getHeight() / 2f;
            initializedCursor = true;
        }

        Player player = Vars.player;
        if (player == null || player.dead()) return;

        // 2. LEFT STICK: Unit Movement
        float moveX = applyDeadzone(Core.input.axis(KeyCode.controllerLStickX));
        float moveY = applyDeadzone(-Core.input.axis(KeyCode.controllerLStickY)); // Invert Y for screen space
        if (Math.abs(moveX) > 0 || Math.abs(moveY) > 0) {
            Vec2 moveVec = new Vec2(moveX, moveY).limit(1f).scl(player.unit().speed());
            player.unit().movePref(moveVec);
        }

        // 3. RIGHT STICK: Virtual Cursor Aiming
        float aimX = applyDeadzone(Core.input.axis(KeyCode.controllerRStickX));
        float aimY = applyDeadzone(-Core.input.axis(KeyCode.controllerRStickY));
        float delta = Core.graphics.getDeltaTime() * 60f;

        cursorX += aimX * ControllerConfig.cursorSpeed * delta;
        cursorY += aimY * ControllerConfig.cursorSpeed * delta;

        // 4. OPTIONAL GYRO: Motion-assisted fine aiming
        if (ControllerConfig.gyroEnabled) {
            // Reads device tilt/rotation rates
            float gyroX = Core.input.getAccelerometerY(); // Landscape horizontal tilt
            float gyroY = -Core.input.getAccelerometerX(); // Landscape vertical tilt
            if (Math.abs(gyroX) > 0.2f) cursorX += gyroX * ControllerConfig.gyroSensitivity * 0.15f * delta;
            if (Math.abs(gyroY) > 0.2f) cursorY += gyroY * ControllerConfig.gyroSensitivity * 0.15f * delta;
        }

        // 5. PS4 TOUCHPAD / EXTERNAL MOUSE SYNC
        // On Android, PS4 touchpad moves the hardware mouse pointer. Sync if moved!
        if (Core.input.mouseX() != 0 && Core.input.mouseY() != 0 && Core.input.isTouched()) {
            cursorX = Core.input.mouseX();
            cursorY = Core.input.mouseY();
        }

        // Clamp virtual cursor inside screen bounds
        cursorX = Mathf.clamp(cursorX, 0, Core.graphics.getWidth());
        cursorY = Mathf.clamp(cursorY, 0, Core.graphics.getHeight());

        // Convert screen cursor coordinates to Mindustry world coordinates
        Vec2 worldPos = Core.camera.unproject(cursorX, cursorY);
        player.mouseX = worldPos.x;
        player.mouseY = worldPos.y;

        // 6. TRIGGERS & BUTTONS: Shooting, Building, Selecting, Rotating
        float rightTrigger = Core.input.axis(KeyCode.controllerRTrigger);
        float leftTrigger = Core.input.axis(KeyCode.controllerLTrigger);

        // Shooting / Primary Action (R2 Trigger or Action Button)
        boolean isFiring = rightTrigger > 0.3f || Core.input.keyDown(ControllerConfig.bindAction);
        player.shooting = isFiring;

        // Tile Selection & Block Placement at Virtual Cursor
        Tile hoveredTile = Vars.world.tileWorld(worldPos.x, worldPos.y);
        if (hoveredTile != null) {
            // Tap Action Button to select block/unit or place schematic
            if (Core.input.keyTap(ControllerConfig.bindAction)) {
                if (Vars.control.input instanceof MobileInput) {
                    // Simulate a screen tap at the virtual cursor coordinates
                    Vars.control.input.touchDown(cursorX, cursorY, 0, KeyCode.mouseLeft);
                    Vars.control.input.touchUp(cursorX, cursorY, 0, KeyCode.mouseLeft);
                }
            }

            // Break Block (L2 Trigger or Break Button)
            if (leftTrigger > 0.3f || Core.input.keyDown(ControllerConfig.bindBreak)) {
                if (hoveredTile.build != null && hoveredTile.team() == player.team()) {
                    Vars.control.input.tryBreakBlock(hoveredTile.x, hoveredTile.y);
                }
            }
        }

        // Rotate Building
        if (Core.input.keyTap(ControllerConfig.bindRotate)) {
            Vars.control.input.rotation = (Vars.control.input.rotation + 1) % 4;
        }

        // Pause / Resume Game
        if (Core.input.keyTap(ControllerConfig.bindPause)) {
            if (Vars.state.isPaused()) {
                Vars.state.set(mindustry.core.GameState.State.playing);
            } else {
                Vars.state.set(mindustry.core.GameState.State.paused);
            }
        }
    }

    /** Draws a visible crosshair where the controller cursor is pointing. */
    private static void drawCursor() {
        if (!ControllerConfig.enabled || !Vars.state.isGame()) return;

        Vec2 worldPos = Core.camera.unproject(cursorX, cursorY);
        Draw.z(Layer.overlayUI);
        Draw.color(ControllerConfig.gyroEnabled ? Pal.accent : Color.white);
        Lines.stroke(2f);
        Lines.circle(worldPos.x, worldPos.y, 6f);
        Lines.lineAngle(worldPos.x, worldPos.y, 0, 4f);
        Lines.lineAngle(worldPos.x, worldPos.y, 90, 4f);
        Lines.lineAngle(worldPos.x, worldPos.y, 180, 4f);
        Lines.lineAngle(worldPos.x, worldPos.y, 270, 4f);
        Draw.reset();
    }

    private static float applyDeadzone(float value) {
        return Math.abs(value) < ControllerConfig.deadzone ? 0f : value;
    }
}
