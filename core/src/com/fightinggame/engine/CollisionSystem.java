package com.fightinggame.engine;

import com.badlogic.gdx.Gdx;
import com.fightinggame.fighters.Fighter;
import com.fightinggame.fighters.Hitbox;
import com.fightinggame.fighters.Hurtbox;
import com.fightinggame.fighters.Move;

/**
 * Handles all collision detection between fighters.
 * Checks hitboxes vs hurtboxes and applies damage/knockback on successful hits.
 */
public class CollisionSystem {

    // Track which hits have already been processed this frame
    private boolean fighter1HitProcessed;
    private boolean fighter2HitProcessed;

    public CollisionSystem() {
        fighter1HitProcessed = false;
        fighter2HitProcessed = false;
    }

    /**
     * Check for collisions between two fighters and apply damage.
     * Called every frame from GameWorld.
     */
    public void checkCollisions(Fighter fighter1, Fighter fighter2) {
        // Reset hit tracking each frame
        fighter1HitProcessed = false;
        fighter2HitProcessed = false;

        // Check if fighter1's attack hits fighter2
        checkHit(fighter1, fighter2);

        // Check if fighter2's attack hits fighter1
        checkHit(fighter2, fighter1);
    }

    /**
     * Check if attacker's hitbox hits defender's hurtbox.
     */
    private void checkHit(Fighter attacker, Fighter defender) {
        Hitbox attackerHitbox = attacker.getHitbox();
        Hurtbox defenderHurtbox = defender.getHurtbox();

        // Only process if hitbox is active and defender can be hit
        if (!attackerHitbox.isActive() || !defenderHurtbox.isActive()) {
            return;
        }

        // Check for overlap
        if (attackerHitbox.overlaps(defenderHurtbox)) {
            // Prevent multiple hits from the same attack
            if (attacker.getPlayerNumber() == 1 && fighter1HitProcessed) return;
            if (attacker.getPlayerNumber() == 2 && fighter2HitProcessed) return;

            // Get move data
            Move move = attackerHitbox.getAssociatedMove();
            if (move == null) return;

            // Apply damage and knockback
            defender.applyDamage(
                    move.damage(),
                    move.getKnockback(),
                    move.isLauncher()
            );

            // Deactivate hitbox after successful hit (prevent multi-hit)
            attackerHitbox.deactivate();

            // Mark hit as processed
            if (attacker.getPlayerNumber() == 1) {
                fighter1HitProcessed = true;
            } else {
                fighter2HitProcessed = true;
            }

            // Log hit
            Gdx.app.log("CollisionSystem",
                    String.format("%s hit %s with %s for %d damage!",
                            attacker.getName(),
                            defender.getName(),
                            move.getName(),
                            move.damage()));
        }
    }

    /**
     * Check if fighters are overlapping (for push-back logic).
     * Not currently used but useful for preventing fighters from walking through each other.
     */
    public boolean checkOverlap(Fighter fighter1, Fighter fighter2) {
        return fighter1.getHurtbox().getBounds().overlaps(fighter2.getHurtbox().getBounds());
    }
}
