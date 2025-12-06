package com.fightinggame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.fightinggame.FightingGame;

/**
 * Main menu screen with Start and Quit options.
 */
public class MainMenuScreen implements Screen {

    private FightingGame game;
    private GlyphLayout layout;

    private int selectedOption;
    private String[] menuOptions = {"Start Game", "Quit"};

    public MainMenuScreen(FightingGame game) {
        this.game = game;
        this.layout = new GlyphLayout();
        this.selectedOption = 0;
    }

    @Override
    public void show() {
        Gdx.app.log("MainMenuScreen", "Main menu loaded");
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle input
        handleInput();

        // Render
        game.batch.begin();

        // Title
        String title = "FIGHTING GAME";
        layout.setText(game.font, title);
        game.font.getData().setScale(3.0f);
        game.font.setColor(Color.GOLD);
        layout.setText(game.font, title);
        game.font.draw(game.batch, title,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT - 150);
        game.font.getData().setScale(2.0f); // Reset

        // Menu options
        float startY = FightingGame.SCREEN_HEIGHT / 2 + 50;
        float spacing = 60;

        for (int i = 0; i < menuOptions.length; i++) {
            if (i == selectedOption) {
                game.font.setColor(Color.YELLOW);
                game.font.draw(game.batch, "> " + menuOptions[i] + " <",
                        FightingGame.SCREEN_WIDTH / 2 - 100,
                        startY - (i * spacing));
            } else {
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, menuOptions[i],
                        FightingGame.SCREEN_WIDTH / 2 - 80,
                        startY - (i * spacing));
            }
        }

        // Controls info
        game.font.setColor(Color.GRAY);
        game.font.getData().setScale(1.0f);
        game.font.draw(game.batch, "Use UP/DOWN to select, ENTER to confirm",
                FightingGame.SCREEN_WIDTH / 2 - 250, 100);
        game.font.getData().setScale(2.0f);

        game.batch.end();
    }

    private void handleInput() {
        // Menu navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption--;
            if (selectedOption < 0) selectedOption = menuOptions.length - 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption++;
            if (selectedOption >= menuOptions.length) selectedOption = 0;
        }

        // Confirm selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            selectOption();
        }
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0: // Start Game
                game.setScreen(new CharacterSelectScreen(game));
                break;
            case 1: // Quit
                Gdx.app.exit();
                break;
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
