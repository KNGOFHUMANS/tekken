package com.fightinggame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fightinggame.screens.MainMenuScreen;

/**
 * Main game class that manages screens and global resources.
entry point for the LibGDX game.
 */
public class FightingGame extends Game {

    // Global resources shared across all screens
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;

    // Game constants
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final String GAME_TITLE = "Fighting Game";

    @Override
    public void create() {
        // Initialize global rendering resources
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont(); // Uses LibGDX default font
        font.getData().setScale(2.0f); // Make text bigger

        Gdx.app.log("FightingGame", "Game initialized");

        // Start with the main menu
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        // Clean up global resources
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();

        // The current screen will be disposed by LibGDX automatically
    }
}
