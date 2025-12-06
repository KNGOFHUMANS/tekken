package com.fightinggame.fighters;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents the vulnerable collision box of a fighter.
 * Always active unless fighter is invincible or KO'd.
 */
public class Hurtbox {
    private Rectangle bounds;
    private boolean active;

    public Hurtbox(float width, float height) {
        this.bounds = new Rectangle(0, 0, width, height);
        this.active = true;
    }

    /**
     * Update the hurtbox position to match the fighter.
     */
    public void updatePosition(float x, float y) {
        bounds.setPosition(x, y);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
