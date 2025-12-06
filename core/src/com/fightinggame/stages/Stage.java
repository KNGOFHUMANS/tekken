package com.fightinggame.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fightinggame.FightingGame;

/**
 * Stage with parallax background layers.
 * Simulates depth by moving background layers at different speeds based on camera position.
 */
public class Stage {

    // Floor
    private static final float FLOOR_Y = 100f;

    // Parallax layers
    private ParallaxLayer[] layers;

    private ShapeRenderer shapeRenderer;

    /**
     * Represents a single parallax layer.
     */
    private static class ParallaxLayer {
        float offsetX;           // Current offset based on camera
        float parallaxSpeed;     // Speed multiplier (0 = static, 1 = moves with camera)
        Color color;
        float y;
        float height;

        public ParallaxLayer(float parallaxSpeed, Color color, float y, float height) {
            this.parallaxSpeed = parallaxSpeed;
            this.color = color;
            this.y = y;
            this.height = height;
            this.offsetX = 0;
        }

        public void update(float cameraX) {
            this.offsetX = -cameraX * parallaxSpeed;
        }

        public void render(ShapeRenderer shapeRenderer, float viewWidth) {
            shapeRenderer.setColor(color);

            // Wrap the offset for seamless looping
            float wrappedOffset = offsetX % viewWidth;
            shapeRenderer.rect(wrappedOffset - viewWidth, y, viewWidth, height);
            shapeRenderer.rect(wrappedOffset, y, viewWidth, height);
            shapeRenderer.rect(wrappedOffset + viewWidth, y, viewWidth, height);
        }
    }

    public Stage() {
        this.shapeRenderer = new ShapeRenderer();

        // Create parallax layers (back to front)
        // Each layer has different speed and color to simulate depth
        layers = new ParallaxLayer[]{
                // Far background (mountains) - moves slowest
                new ParallaxLayer(0.1f, new Color(0.2f, 0.2f, 0.4f, 1), 200, 300),

                // Mid background (buildings) - moves medium speed
                new ParallaxLayer(0.3f, new Color(0.3f, 0.3f, 0.5f, 1), 150, 250),

                // Near background (trees) - moves faster
                new ParallaxLayer(0.5f, new Color(0.4f, 0.5f, 0.3f, 1), 100, 200)
        };
    }

    /**
     * Update parallax based on camera position.
     */
    public void update(float delta, float cameraX) {
        for (ParallaxLayer layer : layers) {
            layer.update(cameraX);
        }
    }

    /**
     * Render the stage.
     */
    public void render(SpriteBatch batch) {
        // End batch to use ShapeRenderer
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Sky background
        shapeRenderer.setColor(new Color(0.5f, 0.7f, 1.0f, 1));
        shapeRenderer.rect(0, 0, FightingGame.SCREEN_WIDTH, FightingGame.SCREEN_HEIGHT);

        // Render parallax layers
        for (ParallaxLayer layer : layers) {
            layer.render(shapeRenderer, FightingGame.SCREEN_WIDTH);
        }

        // Floor
        shapeRenderer.setColor(new Color(0.6f, 0.4f, 0.2f, 1)); // Brown
        shapeRenderer.rect(0, 0, FightingGame.SCREEN_WIDTH, FLOOR_Y);

        // Floor surface
        shapeRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 1)); // Gray
        shapeRenderer.rect(0, FLOOR_Y, FightingGame.SCREEN_WIDTH, 5);

        shapeRenderer.end();

        // Resume batch for next rendering
        batch.begin();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public static float getFloorY() {
        return FLOOR_Y;
    }
}
