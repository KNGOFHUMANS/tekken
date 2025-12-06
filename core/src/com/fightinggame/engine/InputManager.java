package com.fightinggame.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.fightinggame.fighters.Fighter;

/**
 * Translates keyboard input into fighter actions for Player 1.
 * Uses AI controller for Player 2 (Bot).
 *
 * Controls:
 * Player 1: WASD for movement, J=Punch, K=Kick, L=Blast, Left Shift=Block
 * Player 2: AI-controlled
 */
public class InputManager {

    private Fighter fighter1;
    private Fighter fighter2;
    private AIController aiController; // AI for player 2

    // Track previous frame state to detect button presses (not holds)
    private boolean p1LightPressed;
    private boolean p1HeavyPressed;
    private boolean p1SpecialPressed;

    public InputManager(Fighter fighter1, Fighter fighter2) {
        this.fighter1 = fighter1;
        this.fighter2 = fighter2;
        // Create AI controller with high difficulty (0.85 aggressiveness, 0.8 reaction speed)
        this.aiController = new AIController(0.85f, 0.8f);
    }

    /**
     * Process input for player 1 and AI for player 2.
     * Called every frame.
     */
    public void update(float delta) {
        handlePlayer1Input();
        // Use AI for player 2 instead of keyboard input
        aiController.update(delta, fighter2, fighter1);
    }

    /**
     * Handle Player 1 input (WASD + JKL).
     */
    private void handlePlayer1Input() {
        boolean anyMovement = false;

        // Movement
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            fighter1.moveLeft();
            anyMovement = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            fighter1.moveRight();
            anyMovement = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            fighter1.jump();
            anyMovement = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            fighter1.crouch();
            anyMovement = true;
        }

        // Block
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            fighter1.block();
            anyMovement = true;
        }

        // If no movement keys, return to idle
        if (!anyMovement) {
            fighter1.stopMoving();
        }

        // Attacks (detect button press, not hold)
        boolean punchPressed = Gdx.input.isKeyPressed(Input.Keys.J);
        boolean kickPressed = Gdx.input.isKeyPressed(Input.Keys.K);
        boolean blastPressed = Gdx.input.isKeyPressed(Input.Keys.L);

        if (punchPressed && !p1LightPressed) {
            fighter1.startMove("PUNCH");
        }
        if (kickPressed && !p1HeavyPressed) {
            fighter1.startMove("KICK");
        }
        if (blastPressed && !p1SpecialPressed) {
            fighter1.startMove("BLAST");
        }

        // Update previous state
        p1LightPressed = punchPressed;
        p1HeavyPressed = kickPressed;
        p1SpecialPressed = blastPressed;
    }

    // Player 2 is now AI-controlled, no need for keyboard input

    /**
     * Update fighters (useful if switching to AI).
     */
    public void setFighters(Fighter fighter1, Fighter fighter2) {
        this.fighter1 = fighter1;
        this.fighter2 = fighter2;
    }
}
