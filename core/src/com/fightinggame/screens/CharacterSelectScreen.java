package com.fightinggame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.fightinggame.FightingGame;

/**
 * Character selection screen.
 * Players choose their fighters before the match.
 */
public class CharacterSelectScreen implements Screen {

    private FightingGame game;
    private GlyphLayout layout;

    // Character data
    private static class CharacterData {
        String name;
        Color color;

        CharacterData(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }

    private CharacterData[] characters;
    private int player1Selection;
    private int player2Selection;
    private boolean player1Ready;
    private boolean player2Ready;

    public CharacterSelectScreen(FightingGame game) {
        this.game = game;
        this.layout = new GlyphLayout();

        // Define available characters
        characters = new CharacterData[]{
                new CharacterData("Ryu", Color.RED),
                new CharacterData("Ken", Color.ORANGE),
                new CharacterData("Chun-Li", Color.BLUE),
                new CharacterData("Guile", Color.GREEN)
        };

        player1Selection = 0;
        player2Selection = 1;
        player1Ready = false;
        player2Ready = false;
    }

    @Override
    public void show() {
        Gdx.app.log("CharacterSelectScreen", "Character select loaded");
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle input
        handleInput();

        // Render
        game.batch.begin();

        // Title
        String title = "CHARACTER SELECT";
        game.font.getData().setScale(2.5f);
        game.font.setColor(Color.WHITE);
        layout.setText(game.font, title);
        game.font.draw(game.batch, title,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT - 100);
        game.font.getData().setScale(2.0f);

        // Player 1 selection area (left)
        renderPlayerSelection(1, 200, player1Selection, player1Ready);

        // Player 2 selection area (right)
        renderPlayerSelection(2, FightingGame.SCREEN_WIDTH - 400, player2Selection, player2Ready);

        // Instructions
        game.font.setColor(Color.GRAY);
        game.font.getData().setScale(1.2f);
        game.font.draw(game.batch, "P1: W/S to select, J to confirm",
                50, 100);
        game.font.draw(game.batch, "P2: UP/DOWN to select, NUMPAD 1 to confirm",
                FightingGame.SCREEN_WIDTH - 500, 100);

        if (player1Ready && player2Ready) {
            game.font.setColor(Color.YELLOW);
            game.font.getData().setScale(1.5f);
            String readyText = "Press ENTER to start!";
            layout.setText(game.font, readyText);
            game.font.draw(game.batch, readyText,
                    (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                    150);
        }

        game.font.getData().setScale(2.0f);
        game.batch.end();
    }

    private void renderPlayerSelection(int playerNum, float x, int selection, boolean ready) {
        float y = FightingGame.SCREEN_HEIGHT / 2 + 100;
        float spacing = 60;

        // Player label
        String label = "PLAYER " + playerNum + (ready ? " - READY!" : "");
        game.font.setColor(ready ? Color.GREEN : Color.CYAN);
        game.font.draw(game.batch, label, x, y + 100);

        // Character list
        for (int i = 0; i < characters.length; i++) {
            CharacterData character = characters[i];

            if (i == selection) {
                game.font.setColor(character.color);
                game.font.draw(game.batch, "> " + character.name, x, y - (i * spacing));
            } else {
                game.font.setColor(Color.GRAY);
                game.font.draw(game.batch, character.name, x + 20, y - (i * spacing));
            }
        }
    }

    private void handleInput() {
        // Player 1 selection (W/S)
        if (!player1Ready) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                player1Selection--;
                if (player1Selection < 0) player1Selection = characters.length - 1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                player1Selection++;
                if (player1Selection >= characters.length) player1Selection = 0;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                player1Ready = true;
            }
        } else {
            // Allow to go back
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                player1Ready = false;
            }
        }

        // Player 2 selection (Arrow keys)
        if (!player2Ready) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                player2Selection--;
                if (player2Selection < 0) player2Selection = characters.length - 1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                player2Selection++;
                if (player2Selection >= characters.length) player2Selection = 0;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                player2Ready = true;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                player2Ready = false;
            }
        }

        // Start fight when both ready
        if (player1Ready && player2Ready && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startFight();
        }

        // Back to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void startFight() {
        CharacterData p1Char = characters[player1Selection];
        CharacterData p2Char = characters[player2Selection];

        game.setScreen(new FightScreen(game, p1Char.name, p1Char.color,
                p2Char.name, p2Char.color));
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
