package com.fightinggame.fighters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Fighter class representing a character in the match.
 * Contains state machine, movement physics, combat logic, and rendering.
 */
public class Fighter {

    // Identity
    private String name;
    private int playerNumber; // 1 or 2
    private Color color;

    // Position and physics
    private Vector2 position;
    private Vector2 velocity;
    private boolean facingRight;
    private boolean onGround;

    // Fighter dimensions
    private static final float FIGHTER_WIDTH = 80f;
    private static final float FIGHTER_HEIGHT = 120f;

    // Movement constants
    private static final float MOVE_SPEED = 200f;
    private static final float JUMP_VELOCITY = 500f;
    private static final float GRAVITY = -1200f;
    private static final float GROUND_Y = 100f;
    private static final float MAX_HEIGHT = 400f; // Maximum height fighters can reach

    // Combat stats
    private int health;
    private int maxHealth;

    // State machine
    private FighterState currentState;

    // Attack system
    private Move currentMove;
    private int moveFrame; // Current frame of the active move
    private Map<String, Move> moveset;

    // Collision boxes
    private Hitbox hitbox;
    private Hurtbox hurtbox;

    // Hitstun/Knockback
    private float hitstunTime;
    private float knockdownTime;
    private static final float HITSTUN_DURATION = 0.3f; // seconds
    private static final float KNOCKDOWN_DURATION = 1.0f;

    // Combo system
    private Move lastMove;
    private float timeSinceLastHit;
    private static final float COMBO_WINDOW = 0.5f; // seconds to chain attacks

    public Fighter(String name, int playerNumber, float startX, Color color) {
        this.name = name;
        this.playerNumber = playerNumber;
        this.color = color;

        // Initialize position
        this.position = new Vector2(startX, GROUND_Y);
        this.velocity = new Vector2(0, 0);
        this.facingRight = (playerNumber == 1); // P1 faces right, P2 faces left

        // Initialize stats
        this.maxHealth = 100;
        this.health = maxHealth;

        // Initialize state
        this.currentState = FighterState.IDLE;
        this.onGround = true;

        // Initialize collision boxes
        this.hitbox = new Hitbox();
        this.hurtbox = new Hurtbox(FIGHTER_WIDTH, FIGHTER_HEIGHT);
        this.hurtbox.updatePosition(position.x, position.y);

        // Initialize moveset
        this.moveset = new HashMap<>();
        initializeMoveset();
    }

    /**
     * Define the fighter's moveset with frame data.
     * This is where you customize each character's attacks.
     */
    private void initializeMoveset() {
        // Punch: Fast, low damage
        // startup=3, active=2, recovery=5 (total 10 frames at 60fps = 0.166s)
        Move punch = new Move(
                "Punch",
                3, 2, 5,        // Frame data
                5,              // Damage
                100f, 50f,      // Knockback (x, y)
                60f, 60f,       // Hitbox offset (x, y)
                40f, 30f        // Hitbox size (w, h)
        );
        moveset.put("PUNCH", punch);

        // Kick: Medium speed, medium damage
        Move kick = new Move(
                "Kick",
                6, 3, 8,        // Frame data
                10,             // Damage
                180f, 80f,      // Knockback
                65f, 50f,       // Hitbox offset
                45f, 35f        // Hitbox size
        );
        moveset.put("KICK", kick);

        // Blast: Slow, high damage projectile/energy attack
        Move blast = new Move(
                "Blast",
                10, 3, 15,      // Frame data
                20,             // Damage
                150f, 400f,     // High vertical knockback
                50f, 60f,       // Hitbox offset
                50f, 60f        // Hitbox size
        );
        blast.setLauncher(true); // Causes knockdown
        moveset.put("BLAST", blast);
    }

    /**
     * Main update loop - called every frame.
     * Handles state machine transitions and physics.
     */
    public void update(float delta) {
        timeSinceLastHit += delta;

        // Update state machine
        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                break;

            case WALKING:
                // Velocity is set by input handler
                break;

            case JUMPING:
                // Apply gravity
                velocity.y += GRAVITY * delta;
                if (position.y <= GROUND_Y && velocity.y <= 0) {
                    position.y = GROUND_Y;
                    velocity.y = 0;
                    onGround = true;
                    changeState(FighterState.IDLE);
                }
                break;

            case ATTACKING:
                // Lock movement during attack
                velocity.x = 0;
                updateAttack(delta);
                break;

            case HITSTUN:
                // Apply gravity during hitstun
                velocity.y += GRAVITY * delta;
                hitstunTime -= delta;
                // Check if landed on ground
                if (position.y <= GROUND_Y && velocity.y <= 0) {
                    position.y = GROUND_Y;
                    velocity.y = 0;
                    onGround = true;
                }
                if (hitstunTime <= 0) {
                    changeState(FighterState.IDLE);
                }
                break;

            case KNOCKDOWN:
                // Apply gravity during knockdown
                velocity.y += GRAVITY * delta;
                knockdownTime -= delta;
                // Check if landed on ground
                if (position.y <= GROUND_Y && velocity.y <= 0) {
                    position.y = GROUND_Y;
                    velocity.y = 0;
                    onGround = true;
                }
                if (knockdownTime <= 0) {
                    changeState(FighterState.IDLE);
                }
                break;

            case KO:
                velocity.set(0, 0);
                break;

            case BLOCKING:
                // Lock movement during block
                velocity.x = 0;
                break;

            case CROUCHING:
                // Lock movement during crouch
                velocity.x = 0;
                break;
        }

        // Apply velocity to position
        position.add(velocity.x * delta, velocity.y * delta);

        // Clamp to maximum height
        if (position.y > MAX_HEIGHT) {
            position.y = MAX_HEIGHT;
            velocity.y = Math.min(velocity.y, 0); // Stop upward velocity
        }

        // Clamp to ground
        if (position.y < GROUND_Y && currentState != FighterState.KNOCKDOWN) {
            position.y = GROUND_Y;
            onGround = true;
        }

        // Update hurtbox position
        hurtbox.updatePosition(position.x, position.y);

        // Decay velocity
        if (currentState != FighterState.JUMPING) {
            velocity.x *= 0.8f;
        }
    }

    /**
     * Update attack state - handles frame progression and hitbox activation.
     */
    private void updateAttack(float delta) {
        if (currentMove == null) {
            changeState(FighterState.IDLE);
            return;
        }

        // Increment move frame (at 60fps, delta ≈ 0.0166s per frame)
        moveFrame++;

        // Check if move is in active frames
        if (currentMove.isActive(moveFrame)) {
            // Activate hitbox during active frames
            hitbox.activate(position.x, position.y, currentMove, facingRight);
        } else {
            hitbox.deactivate();
        }

        // Check if move is complete
        if (currentMove.isComplete(moveFrame)) {
            hitbox.deactivate();
            lastMove = currentMove;
            currentMove = null;
            moveFrame = 0;
            changeState(FighterState.IDLE);
        }
    }

    /**
     * Change state.
     */
    private void changeState(FighterState newState) {
        if (currentState != newState) {
            currentState = newState;
        }
    }

    // ========== INPUT ACTIONS ==========

    public void moveLeft() {
        if (!canMove()) return;
        velocity.x = -MOVE_SPEED;
        changeState(FighterState.WALKING);
        facingRight = false;
    }

    public void moveRight() {
        if (!canMove()) return;
        velocity.x = MOVE_SPEED;
        changeState(FighterState.WALKING);
        facingRight = true;
    }

    public void jump() {
        if (!canMove() || !onGround) return;
        velocity.y = JUMP_VELOCITY;
        onGround = false;
        changeState(FighterState.JUMPING);
    }

    public void crouch() {
        if (!canMove()) return;
        changeState(FighterState.CROUCHING);
    }

    public void block() {
        if (!canMove()) return;
        changeState(FighterState.BLOCKING);
    }

    public void stopMoving() {
        if (currentState == FighterState.WALKING || currentState == FighterState.CROUCHING
                || currentState == FighterState.BLOCKING) {
            changeState(FighterState.IDLE);
        }
    }

    /**
     * Execute a move from the moveset.
     */
    public void startMove(String moveKey) {
        if (!canAttack()) return;

        Move move = moveset.get(moveKey);
        if (move == null) return;

        // Check for combo (light -> heavy)
        if (canComboInto(move)) {
            // Combo bonus: reduce startup frames
            // (simplified - in a real game you'd have combo-specific moves)
        }

        this.currentMove = move;
        this.moveFrame = 0;
        changeState(FighterState.ATTACKING);
    }

    /**
     * Check if current move can combo into the next move.
     */
    private boolean canComboInto(Move nextMove) {
        if (lastMove == null) return false;
        if (timeSinceLastHit > COMBO_WINDOW) return false;

        // Simple combo: Punch -> Kick -> Blast
        if (lastMove.getName().equals("Punch") &&
                nextMove.getName().equals("Kick")) {
            return true;
        }
        if (lastMove.getName().equals("Kick") &&
                nextMove.getName().equals("Blast")) {
            return true;
        }

        return false;
    }

    /**
     * Apply damage and knockback from an opponent's attack.
     */
    public void applyDamage(int damage, Vector2 knockback, boolean isLauncher) {
        health -= damage;
        timeSinceLastHit = 0;

        if (health <= 0) {
            health = 0;
            changeState(FighterState.KO);
            return;
        }

        // Apply knockback
        float knockbackDirection = facingRight ? -1 : 1; // Knock away from attacker
        velocity.set(knockback.x * knockbackDirection, knockback.y);

        // Determine hitstun or knockdown
        if (isLauncher || knockback.y > 200) {
            knockdownTime = KNOCKDOWN_DURATION;
            onGround = false;
            changeState(FighterState.KNOCKDOWN);
        } else {
            hitstunTime = HITSTUN_DURATION;
            changeState(FighterState.HITSTUN);
        }

        // Cancel current attack if any
        if (currentMove != null) {
            hitbox.deactivate();
            currentMove = null;
        }
    }

    /**
     * Reset fighter for a new round.
     */
    public void reset(float startX) {
        position.set(startX, GROUND_Y);
        velocity.set(0, 0);
        health = maxHealth;
        changeState(FighterState.IDLE);
        hitbox.deactivate();
        currentMove = null;
        lastMove = null;
        timeSinceLastHit = 0;
        facingRight = (playerNumber == 1);
    }

    // ========== STATE CHECKS ==========

    private boolean canMove() {
        return currentState == FighterState.IDLE ||
                currentState == FighterState.WALKING ||
                currentState == FighterState.CROUCHING;
    }

    private boolean canAttack() {
        return currentState == FighterState.IDLE ||
                currentState == FighterState.WALKING ||
                currentState == FighterState.CROUCHING;
    }

    // ========== RENDERING ==========

    /**
     * Render the fighter as a block robot.
     */
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Robot dimensions
        float headSize = 25f;
        float bodyWidth = 50f;
        float bodyHeight = 45f;
        float armWidth = 15f;
        float armHeight = 35f;
        float legWidth = 18f;
        float legHeight = 30f;

        // Animation offsets for limbs
        float rightArmOffsetX = 0f;
        float leftArmOffsetX = 0f;
        float bothArmsOffsetX = 0f;
        float rightLegOffsetX = 0f;

        // Animate limbs based on current move
        if (currentState == FighterState.ATTACKING && currentMove != null) {
            String moveName = currentMove.getName();
            float attackProgress = (float) moveFrame / currentMove.getTotalFrames();

            if (moveName.equals("Punch")) {
                // Punch: extend right arm forward
                rightArmOffsetX = (facingRight ? 1 : -1) * 20f * attackProgress;
            } else if (moveName.equals("Kick")) {
                // Kick: extend right leg forward
                rightLegOffsetX = (facingRight ? 1 : -1) * 25f * attackProgress;
            } else if (moveName.equals("Blast")) {
                // Blast: extend both arms forward
                bothArmsOffsetX = (facingRight ? 1 : -1) * 25f * attackProgress;
            }
        }

        // Calculate positions
        float centerX = position.x + FIGHTER_WIDTH / 2f;

        // Head (top block)
        float headX = centerX - headSize / 2f;
        float headY = position.y + FIGHTER_HEIGHT - headSize - 5;
        shapeRenderer.setColor(color);
        shapeRenderer.rect(headX, headY, headSize, headSize);

        // Eyes (white squares)
        shapeRenderer.setColor(Color.WHITE);
        float eyeSize = 5f;
        if (facingRight) {
            shapeRenderer.rect(headX + 12, headY + 14, eyeSize, eyeSize);
        } else {
            shapeRenderer.rect(headX + 8, headY + 14, eyeSize, eyeSize);
        }

        // Body (torso block)
        float bodyX = centerX - bodyWidth / 2f;
        float bodyY = position.y + 45;
        shapeRenderer.setColor(color);
        shapeRenderer.rect(bodyX, bodyY, bodyWidth, bodyHeight);

        // Body detail (darker chest plate)
        shapeRenderer.setColor(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f, 1f);
        shapeRenderer.rect(bodyX + 10, bodyY + 10, bodyWidth - 20, bodyHeight - 20);

        // Left Arm
        float leftArmX = bodyX - armWidth - 2 + leftArmOffsetX + bothArmsOffsetX;
        float leftArmY = bodyY + bodyHeight - armHeight - 5;
        shapeRenderer.setColor(color);
        shapeRenderer.rect(leftArmX, leftArmY, armWidth, armHeight);

        // Right Arm
        float rightArmX = bodyX + bodyWidth + 2 + rightArmOffsetX + bothArmsOffsetX;
        float rightArmY = bodyY + bodyHeight - armHeight - 5;
        shapeRenderer.rect(rightArmX, rightArmY, armWidth, armHeight);

        // Left Leg
        float leftLegX = centerX - legWidth - 3;
        float leftLegY = position.y;
        shapeRenderer.setColor(color.r * 0.8f, color.g * 0.8f, color.b * 0.8f, 1f);
        shapeRenderer.rect(leftLegX, leftLegY, legWidth, legHeight);

        // Right Leg (animated for kicks)
        float rightLegX = centerX + 3 + rightLegOffsetX;
        float rightLegY = position.y;
        shapeRenderer.rect(rightLegX, rightLegY, legWidth, legHeight);

        // Feet (darker blocks)
        shapeRenderer.setColor(color.r * 0.5f, color.g * 0.5f, color.b * 0.5f, 1f);
        shapeRenderer.rect(leftLegX - 2, leftLegY, legWidth + 4, 8);
        shapeRenderer.rect(rightLegX - 2, rightLegY, legWidth + 4, 8);

        // Direction indicator arrow on head
        shapeRenderer.setColor(Color.YELLOW);
        float arrowX = facingRight ? headX + headSize : headX;
        float arrowY = headY + headSize - 3;
        if (facingRight) {
            shapeRenderer.triangle(
                    arrowX, arrowY,
                    arrowX + 8, arrowY - 3,
                    arrowX, arrowY - 6
            );
        } else {
            shapeRenderer.triangle(
                    arrowX, arrowY,
                    arrowX - 8, arrowY - 3,
                    arrowX, arrowY - 6
            );
        }

        // Render hitbox if active (red)
        if (hitbox.isActive()) {
            shapeRenderer.setColor(1, 0, 0, 0.5f);
            shapeRenderer.rect(
                    hitbox.getBounds().x,
                    hitbox.getBounds().y,
                    hitbox.getBounds().width,
                    hitbox.getBounds().height
            );
        }

        // Render hurtbox (cyan outline)
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(
                hurtbox.getBounds().x,
                hurtbox.getBounds().y,
                hurtbox.getBounds().width,
                hurtbox.getBounds().height
        );

        shapeRenderer.end();
    }

    // ========== GETTERS ==========

    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public FighterState getCurrentState() { return currentState; }
    public Hitbox getHitbox() { return hitbox; }
    public Hurtbox getHurtbox() { return hurtbox; }
    public String getName() { return name; }
    public int getPlayerNumber() { return playerNumber; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }

    public static float getFighterWidth() { return FIGHTER_WIDTH; }
    public static float getFighterHeight() { return FIGHTER_HEIGHT; }
}
