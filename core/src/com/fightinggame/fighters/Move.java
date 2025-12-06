package com.fightinggame.fighters;

import com.badlogic.gdx.math.Vector2;

/**
 * Represents a single attack move with Tekken-style frame data.
 *
 * Frame data breakdown:
 * - Startup: frames before the attack becomes active (can't hit yet)
 * - Active: frames during which the attack can hit
 * - Recovery: frames after active where fighter can't act
 *
 * Total move duration = startup + active + recovery frames
 */
public class Move {
    private String name;
    private int startupFrames;    // Frames before attack becomes active
    private int activeFrames;     // Frames during which attack can hit
    private int recoveryFrames;   // Frames after attack before returning to neutral
    private int damage;           // Damage dealt on hit
    private Vector2 knockback;    // Knockback applied to opponent (x, y)
    private boolean isLauncher;   // If true, causes knockdown state

    // Hitbox dimensions (relative to fighter position)
    private float hitboxOffsetX;
    private float hitboxOffsetY;
    private float hitboxWidth;
    private float hitboxHeight;

    public Move(String name, int startupFrames, int activeFrames, int recoveryFrames,
                int damage, float knockbackX, float knockbackY,
                float hitboxOffsetX, float hitboxOffsetY,
                float hitboxWidth, float hitboxHeight) {
        this.name = name;
        this.startupFrames = startupFrames;
        this.activeFrames = activeFrames;
        this.recoveryFrames = recoveryFrames;
        this.damage = damage;
        this.knockback = new Vector2(knockbackX, knockbackY);
        this.hitboxOffsetX = hitboxOffsetX;
        this.hitboxOffsetY = hitboxOffsetY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.isLauncher = false;
    }

    // Getters
    public String getName() { return name; }
    public int getStartupFrames() { return startupFrames; }
    public int getActiveFrames() { return activeFrames; }
    public int getRecoveryFrames() { return recoveryFrames; }
    public int getTotalFrames() { return startupFrames + activeFrames + recoveryFrames; }
    public int damage() { return damage; }
    public Vector2 getKnockback() { return knockback; }
    public float getHitboxOffsetX() { return hitboxOffsetX; }
    public float getHitboxOffsetY() { return hitboxOffsetY; }
    public float getHitboxWidth() { return hitboxWidth; }
    public float getHitboxHeight() { return hitboxHeight; }
    public boolean isLauncher() { return isLauncher; }

    public void setLauncher(boolean launcher) {
        this.isLauncher = launcher;
    }

    /**
     * Check if the move is in its active frames (can hit opponent).
     */
    public boolean isActive(int currentFrame) {
        return currentFrame >= startupFrames && currentFrame < (startupFrames + activeFrames);
    }

    /**
     * Check if the move has completed all frames.
     */
    public boolean isComplete(int currentFrame) {
        return currentFrame >= getTotalFrames();
    }
}
