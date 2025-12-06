package com.fightinggame.fighters;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents the offensive collision box of an attack.
 * Only active during the active frames of a move.
 */
public class Hitbox {
    private Rectangle bounds;
    private Move associatedMove;
    private boolean active;

    public Hitbox() {
        this.bounds = new Rectangle();
        this.active = false;
    }

    /**
     * Activate the hitbox at a specific position with dimensions from the move.
     */
    public void activate(float x, float y, Move move, boolean facingRight) {
        this.associatedMove = move;
        this.active = true;

        // Position the hitbox based on fighter direction
        float offsetX = move.getHitboxOffsetX();
        if (!facingRight) {
            offsetX = -offsetX - move.getHitboxWidth(); // Flip for left-facing
        }

        bounds.set(
                x + offsetX,
                y + move.getHitboxOffsetY(),
                move.getHitboxWidth(),
                move.getHitboxHeight()
        );
    }

    /**
     * Deactivate the hitbox.
     */
    public void deactivate() {
        this.active = false;
        this.associatedMove = null;
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Move getAssociatedMove() {
        return associatedMove;
    }

    /**
     * Check if this hitbox overlaps with a hurtbox.
     */
    public boolean overlaps(Hurtbox hurtbox) {
        if (!active || !hurtbox.isActive()) {
            return false;
        }
        return bounds.overlaps(hurtbox.getBounds());
    }
}
