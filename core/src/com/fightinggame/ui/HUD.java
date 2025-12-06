package com.fightinggame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fightinggame.FightingGame;
import com.fightinggame.engine.GameWorld;
import com.fightinggame.fighters.Fighter;

/**
 * Heads-Up Display showing health bars, timer, round count, and match state.
 */
public class HUD {

    private BitmapFont font;
    private HealthBar player1HealthBar;
    private HealthBar player2HealthBar;
    private GlyphLayout layout; // For text measurement

    // HUD layout constants
    private static final float HEALTH_BAR_WIDTH = 400f;
    private static final float HEALTH_BAR_HEIGHT = 30f;
    private static final float HEALTH_BAR_Y = FightingGame.SCREEN_HEIGHT - 80f;
    private static final float HEALTH_BAR_MARGIN = 50f;

    public HUD(BitmapFont font) {
        this.font = font;
        this.layout = new GlyphLayout();

        // Create health bars
        player1HealthBar = new HealthBar(
                HEALTH_BAR_MARGIN,
                HEALTH_BAR_Y,
                HEALTH_BAR_WIDTH,
                HEALTH_BAR_HEIGHT,
                false // Drains left to right
        );

        player2HealthBar = new HealthBar(
                FightingGame.SCREEN_WIDTH - HEALTH_BAR_MARGIN - HEALTH_BAR_WIDTH,
                HEALTH_BAR_Y,
                HEALTH_BAR_WIDTH,
                HEALTH_BAR_HEIGHT,
                true // Drains right to left
        );
    }

    /**
     * Render the HUD.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer,
                       Fighter fighter1, Fighter fighter2,
                       float roundTimeRemaining,
                       int player1Rounds, int player2Rounds,
                       GameWorld.RoundState roundState) {

        // Render health bars
        player1HealthBar.render(shapeRenderer, fighter1.getHealth(), fighter1.getMaxHealth());
        player2HealthBar.render(shapeRenderer, fighter2.getHealth(), fighter2.getMaxHealth());

        // Render text elements
        batch.begin();

        // Player names
        font.setColor(Color.WHITE);
        font.draw(batch, fighter1.getName(), HEALTH_BAR_MARGIN, HEALTH_BAR_Y + HEALTH_BAR_HEIGHT + 25);

        String p2Name = fighter2.getName();
        layout.setText(font, p2Name);
        font.draw(batch, p2Name,
                FightingGame.SCREEN_WIDTH - HEALTH_BAR_MARGIN - layout.width,
                HEALTH_BAR_Y + HEALTH_BAR_HEIGHT + 25);

        // Round timer (center top)
        int timeInt = (int) Math.ceil(roundTimeRemaining);
        String timeStr = String.valueOf(timeInt);
        layout.setText(font, timeStr);
        font.setColor(timeInt <= 10 ? Color.RED : Color.YELLOW);
        font.draw(batch, timeStr,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT - 20);

        // Round indicators (wins)
        renderRoundIndicators(batch, shapeRenderer, player1Rounds, player2Rounds);

        // Round state text (FIGHT!, ROUND 1, etc.)
        renderRoundStateText(batch, roundState);

        batch.end();
    }

    /**
     * Render round win indicators (circles).
     */
    private void renderRoundIndicators(SpriteBatch batch, ShapeRenderer shapeRenderer,
                                        int player1Rounds, int player2Rounds) {
        batch.end(); // End batch to use ShapeRenderer

        float indicatorRadius = 15f;
        float indicatorSpacing = 40f;
        float centerX = FightingGame.SCREEN_WIDTH / 2f;
        float centerY = HEALTH_BAR_Y - 40f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player 1 indicators (left of center)
        for (int i = 0; i < 2; i++) {
            float x = centerX - 60 - (i * indicatorSpacing);
            if (i < player1Rounds) {
                shapeRenderer.setColor(Color.GOLD);
            } else {
                shapeRenderer.setColor(Color.DARK_GRAY);
            }
            shapeRenderer.circle(x, centerY, indicatorRadius);
        }

        // Player 2 indicators (right of center)
        for (int i = 0; i < 2; i++) {
            float x = centerX + 60 + (i * indicatorSpacing);
            if (i < player2Rounds) {
                shapeRenderer.setColor(Color.GOLD);
            } else {
                shapeRenderer.setColor(Color.DARK_GRAY);
            }
            shapeRenderer.circle(x, centerY, indicatorRadius);
        }

        shapeRenderer.end();
        batch.begin(); // Resume batch
    }

    /**
     * Render round state announcements.
     */
    private void renderRoundStateText(SpriteBatch batch, GameWorld.RoundState roundState) {
        String stateText = "";
        Color textColor = Color.WHITE;

        switch (roundState) {
            case PRE_ROUND:
                stateText = "READY...";
                textColor = Color.YELLOW;
                break;
            case FIGHTING:
                // No text during fight
                return;
            case ROUND_END:
                stateText = "ROUND END";
                textColor = Color.CYAN;
                break;
            case MATCH_END:
                stateText = "K.O.";
                textColor = Color.RED;
                break;
        }

        // Draw centered text
        layout.setText(font, stateText);
        font.setColor(textColor);
        font.getData().setScale(3.0f); // Make it big
        layout.setText(font, stateText);
        font.draw(batch, stateText,
                (FightingGame.SCREEN_WIDTH - layout.width) / 2,
                FightingGame.SCREEN_HEIGHT / 2);
        font.getData().setScale(2.0f); // Reset scale
    }
}
