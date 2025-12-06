package com.fightinggame.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Visual health bar component.
 * Displays current health as a colored bar.
 */
public class HealthBar {

    private float x, y;
    private float maxWidth;
    private float height;
    private boolean flipDirection; // If true, bar drains from right to left

    private static final Color HEALTH_COLOR = new Color(0, 1, 0, 1);      // Green
    private static final Color DAMAGE_COLOR = new Color(1, 0, 0, 1);      // Red
    private static final Color BORDER_COLOR = new Color(1, 1, 1, 1);      // White

    public HealthBar(float x, float y, float maxWidth, float height, boolean flipDirection) {
        this.x = x;
        this.y = y;
        this.maxWidth = maxWidth;
        this.height = height;
        this.flipDirection = flipDirection;
    }

    /**
     * Render the health bar.
     */
    public void render(ShapeRenderer shapeRenderer, int currentHealth, int maxHealth) {
        float healthPercent = (float) currentHealth / maxHealth;
        healthPercent = Math.max(0, Math.min(1, healthPercent)); // Clamp 0-1

        float currentWidth = maxWidth * healthPercent;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background (red - damage taken)
        shapeRenderer.setColor(DAMAGE_COLOR);
        shapeRenderer.rect(x, y, maxWidth, height);

        // Current health (green)
        shapeRenderer.setColor(HEALTH_COLOR);
        if (flipDirection) {
            // Drain from right to left (for Player 2)
            shapeRenderer.rect(x + (maxWidth - currentWidth), y, currentWidth, height);
        } else {
            // Drain from left to right (for Player 1)
            shapeRenderer.rect(x, y, currentWidth, height);
        }

        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(x, y, maxWidth, height);
        shapeRenderer.end();
    }
}
