package com.fightinggame.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fightinggame.fighters.Fighter;
import com.fightinggame.fighters.FighterState;
import com.fightinggame.stages.Stage;
import com.fightinggame.ui.HUD;

/**
 * The GameWorld manages the entire match state and coordinates all game systems.
 * It handles:
 * - Round system (best of 3)
 * - Match flow (start, fight, round end, match end)
 * - Update loop for all game objects
 * - Rendering pipeline
 */
public class GameWorld {

    // Game objects
    private Fighter fighter1;
    private Fighter fighter2;
    private Stage stage;
    private HUD hud;

    // Game systems
    private CollisionSystem collisionSystem;
    private InputManager inputManager;

    // Round system
    private int player1Rounds;
    private int player2Rounds;
    private int roundsToWin;
    private RoundState roundState;
    private float roundStateTimer;

    // Round timer
    private float roundTimeRemaining;
    private static final float ROUND_TIME = 99f; // seconds

    // Match result
    private int winnerPlayerNumber; // 0 = none, 1 or 2

    public enum RoundState {
        PRE_ROUND,      // "Round 1, Fight!" announcement
        FIGHTING,       // Active gameplay
        ROUND_END,      // "Player X wins!" announcement
        MATCH_END       // Final victory
    }

    public GameWorld(Fighter fighter1, Fighter fighter2, Stage stage, HUD hud) {
        this.fighter1 = fighter1;
        this.fighter2 = fighter2;
        this.stage = stage;
        this.hud = hud;

        // Initialize systems
        this.collisionSystem = new CollisionSystem();
        this.inputManager = new InputManager(fighter1, fighter2);

        // Initialize round system
        this.player1Rounds = 0;
        this.player2Rounds = 0;
        this.roundsToWin = 2; // Best of 3
        this.roundState = RoundState.PRE_ROUND;
        this.roundStateTimer = 2.0f; // 2 second pre-round
        this.roundTimeRemaining = ROUND_TIME;
        this.winnerPlayerNumber = 0;

        Gdx.app.log("GameWorld", "Match started!");
    }

    /**
     * Main update loop - called every frame.
     */
    public void update(float delta) {
        switch (roundState) {
            case PRE_ROUND:
                updatePreRound(delta);
                break;

            case FIGHTING:
                updateFighting(delta);
                break;

            case ROUND_END:
                updateRoundEnd(delta);
                break;

            case MATCH_END:
                // Match is over, wait for player to return to menu
                break;
        }

        // Always update stage parallax
        stage.update(delta, getCameraX());
    }

    /**
     * Pre-round state: countdown before fight starts.
     */
    private void updatePreRound(float delta) {
        roundStateTimer -= delta;
        if (roundStateTimer <= 0) {
            roundState = RoundState.FIGHTING;
            Gdx.app.log("GameWorld", "FIGHT!");
        }
    }

    /**
     * Fighting state: active gameplay.
     */
    private void updateFighting(float delta) {
        // Update timer
        roundTimeRemaining -= delta;
        if (roundTimeRemaining <= 0) {
            roundTimeRemaining = 0;
            endRoundByTimeout();
            return;
        }

        // Process input (player 1 keyboard + AI for player 2)
        inputManager.update(delta);

        // Update fighters
        fighter1.update(delta);
        fighter2.update(delta);

        // Auto-face opponent (fighters always face each other)
        updateFighterFacing();

        // Check collisions
        collisionSystem.checkCollisions(fighter1, fighter2);

        // Check for KO
        if (fighter1.getCurrentState() == FighterState.KO) {
            endRound(2);
        } else if (fighter2.getCurrentState() == FighterState.KO) {
            endRound(1);
        }
    }

    /**
     * Make fighters face each other automatically.
     */
    private void updateFighterFacing() {
        float f1X = fighter1.getPosition().x;
        float f2X = fighter2.getPosition().x;

        fighter1.setFacingRight(f1X < f2X);
        fighter2.setFacingRight(f2X < f1X);
    }

    /**
     * Round end state: show winner announcement.
     */
    private void updateRoundEnd(float delta) {
        roundStateTimer -= delta;
        if (roundStateTimer <= 0) {
            // Check if match is over
            if (player1Rounds >= roundsToWin || player2Rounds >= roundsToWin) {
                roundState = RoundState.MATCH_END;
                winnerPlayerNumber = (player1Rounds >= roundsToWin) ? 1 : 2;
                Gdx.app.log("GameWorld", "MATCH END! Player " + winnerPlayerNumber + " wins!");
            } else {
                // Start next round
                startNewRound();
            }
        }
    }

    /**
     * End round due to timeout (player with more health wins).
     */
    private void endRoundByTimeout() {
        if (fighter1.getHealth() > fighter2.getHealth()) {
            endRound(1);
        } else if (fighter2.getHealth() > fighter1.getHealth()) {
            endRound(2);
        } else {
            // Draw - no one gets a round win
            Gdx.app.log("GameWorld", "Round draw!");
            roundState = RoundState.ROUND_END;
            roundStateTimer = 2.0f;
        }
    }

    /**
     * End the current round.
     */
    private void endRound(int winnerPlayer) {
        if (winnerPlayer == 1) {
            player1Rounds++;
        } else {
            player2Rounds++;
        }

        Gdx.app.log("GameWorld", "Round over! Player " + winnerPlayer + " wins!");
        Gdx.app.log("GameWorld", "Score: P1=" + player1Rounds + " P2=" + player2Rounds);

        roundState = RoundState.ROUND_END;
        roundStateTimer = 2.0f; // 2 second announcement
    }

    /**
     * Start a new round.
     */
    private void startNewRound() {
        // Reset fighters (centered around middle of screen)
        fighter1.reset(400);
        fighter2.reset(800);

        // Reset timer
        roundTimeRemaining = ROUND_TIME;

        // Reset state
        roundState = RoundState.PRE_ROUND;
        roundStateTimer = 2.0f;

        Gdx.app.log("GameWorld", "New round starting!");
    }

    /**
     * Render all game objects.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Render stage background
        stage.render(batch);

        // Render fighters
        fighter1.render(shapeRenderer);
        fighter2.render(shapeRenderer);
    }

    /**
     * Render the HUD (should be called separately with proper batch management).
     */
    public void renderHUD(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        hud.render(batch, shapeRenderer, fighter1, fighter2, roundTimeRemaining,
                player1Rounds, player2Rounds, roundState);
    }

    /**
     * Get camera focus X position (center between fighters).
     */
    public float getCameraX() {
        float f1X = fighter1.getPosition().x + Fighter.getFighterWidth() / 2;
        float f2X = fighter2.getPosition().x + Fighter.getFighterWidth() / 2;
        return (f1X + f2X) / 2f;
    }

    /**
     * Get distance between fighters (for camera zoom).
     */
    public float getFighterDistance() {
        float f1X = fighter1.getPosition().x;
        float f2X = fighter2.getPosition().x;
        return Math.abs(f2X - f1X);
    }

    // Getters
    public boolean isMatchOver() {
        return roundState == RoundState.MATCH_END;
    }

    public int getWinnerPlayerNumber() {
        return winnerPlayerNumber;
    }

    public Fighter getFighter1() {
        return fighter1;
    }

    public Fighter getFighter2() {
        return fighter2;
    }
}
