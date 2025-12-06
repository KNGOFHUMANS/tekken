package com.fightinggame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.fightinggame.FightingGame;
import com.fightinggame.engine.GameWorld;
import com.fightinggame.fighters.Fighter;
import com.fightinggame.stages.Stage;
import com.fightinggame.ui.HUD;

/**
 * The main fight screen where the match takes place.
 * Implements dynamic camera that centers between fighters and zooms based on distance.
 */
public class FightScreen implements Screen {

    private FightingGame game;

    // Game world
    private GameWorld gameWorld;
    private Fighter fighter1;
    private Fighter fighter2;
    private Stage stage;
    private HUD hud;

    // Camera system (Tekken-style dynamic camera)
    private OrthographicCamera camera;
    private float targetCameraX;
    private float targetZoom;
    private static final float CAMERA_Y = 300f; // Fixed Y position
    private static final float CAMERA_LERP_SPEED = 5f;

    public FightScreen(FightingGame game, String p1Name, Color p1Color,
                       String p2Name, Color p2Color) {
        this.game = game;

        // Create fighters (centered around middle of screen)
        fighter1 = new Fighter(p1Name, 1, 400, p1Color);
        fighter2 = new Fighter(p2Name, 2, 800, p2Color);

        // Create stage and HUD
        stage = new Stage();
        hud = new HUD(game.font);

        // Create game world
        gameWorld = new GameWorld(fighter1, fighter2, stage, hud);

        // Setup camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, FightingGame.SCREEN_WIDTH, FightingGame.SCREEN_HEIGHT);
        camera.position.set(FightingGame.SCREEN_WIDTH / 2f, CAMERA_Y, 0);
        targetCameraX = camera.position.x;
        targetZoom = 1.0f;

        Gdx.app.log("FightScreen", "Fight started: " + p1Name + " vs " + p2Name);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update game world
        gameWorld.update(delta);

        // Update camera
        updateCamera(delta);

        // Apply camera transformations
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);

        // Render game world
        game.batch.begin();
        gameWorld.render(game.batch, game.shapeRenderer);
        game.batch.end();

        // Render HUD (using screen coordinates, not world coordinates)
        renderHUD();

        // Check for match end
        if (gameWorld.isMatchOver()) {
            handleMatchEnd();
        }

        // Debug: Press ESC to return to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    /**
     * Update camera position to follow the player (fighter1).
     */
    private void updateCamera(float delta) {
        // Camera follows player (fighter1) with some offset
        targetCameraX = fighter1.getPosition().x + (Fighter.getFighterWidth() / 2f);

        // Fixed zoom - no dynamic zooming
        targetZoom = 1.0f;

        // Smoothly interpolate camera position
        float currentX = camera.position.x;
        float newX = MathUtils.lerp(currentX, targetCameraX, CAMERA_LERP_SPEED * delta);
        camera.position.set(newX, CAMERA_Y, 0);

        // Set camera zoom
        camera.zoom = targetZoom;
    }

    /**
     * Render HUD using screen coordinates (not affected by camera).
     */
    private void renderHUD() {
        // Create a separate camera for UI that doesn't move
        OrthographicCamera uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, FightingGame.SCREEN_WIDTH, FightingGame.SCREEN_HEIGHT);
        uiCamera.update();

        game.batch.setProjectionMatrix(uiCamera.combined);
        game.shapeRenderer.setProjectionMatrix(uiCamera.combined);

        // Render HUD with its own batch management
        gameWorld.renderHUD(game.batch, game.shapeRenderer);
    }

    private void handleMatchEnd() {
        // Wait a moment then transition to result screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            int winner = gameWorld.getWinnerPlayerNumber();
            String winnerName = (winner == 1) ? fighter1.getName() : fighter2.getName();
            game.setScreen(new ResultScreen(game, winnerName, winner));
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
