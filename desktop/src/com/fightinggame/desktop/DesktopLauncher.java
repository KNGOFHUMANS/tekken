package com.fightinggame.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.fightinggame.FightingGame;

/**
 * Desktop launcher for the fighting game.
 * Configures the window and starts the game.
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // Window configuration
        config.setTitle(FightingGame.GAME_TITLE);
        config.setWindowedMode(FightingGame.SCREEN_WIDTH, FightingGame.SCREEN_HEIGHT);
        config.setForegroundFPS(60);
        config.useVsync(true);

        // Launch the game
        new Lwjgl3Application(new FightingGame(), config);
    }
}
