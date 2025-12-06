package com.fightinggame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.fightinggame.FightingGame;

/**
 * Result screen shown after a match ends.
 * Displays the winner and allows returning to main menu.
 */
public class ResultScreen implements Screen {

    private FightingGame game;
    private GlyphLayout layout;
    private String winnerName;
    private int winnerPlayerNumber;
    private float stateTime;

    public ResultScreen(FightingGame game, String winnerName, int winnerPlayerNumber) {
        this.game = game;
        this.layout = new GlyphLayout();
        this.winnerName = winnerName;
        this.winnerPlayerNumber = winnerPlayerNumber;
        this.stateTime = 0;
    }

    @Override
    public void show() {
        Gdx.app.log("ResultScreen", winnerName + " (Player " + winnerPlayerNumber + ") wins!");
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // Victory text
        String victoryText = winnerName.toUpperCase() + " WINS!";
        game.font.getData().setScale(4.0f);
        game.font.setColor(Color.GOLD);
        layout.setText(game.font, victoryText);
        game.font.draw(game.batch, victoryText,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT - 200);

        // Player number
        String playerText = "Player " + winnerPlayerNumber + " Victory";
        game.font.getData().setScale(2.0f);
        game.font.setColor(Color.YELLOW);
        layout.setText(game.font, playerText);
        game.font.draw(game.batch, playerText,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT - 300);

        // Flashing prompts
        if (stateTime % 1.0f < 0.5f) {
            game.font.getData().setScale(1.5f);
            game.font.setColor(Color.WHITE);
            String prompt1 = "Press R to Play Again";
            layout.setText(game.font, prompt1);
            game.font.draw(game.batch, prompt1,
                    (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                    250);

            String prompt2 = "Press ENTER to return to menu";
            layout.setText(game.font, prompt2);
            game.font.draw(game.batch, prompt2,
                    (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                    180);
        }

        game.font.getData().setScale(2.0f); // Reset scale

        game.batch.end();

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new CharacterSelectScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MainMenuScreen(game));
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
