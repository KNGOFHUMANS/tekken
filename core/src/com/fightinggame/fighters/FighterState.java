package com.fightinggame.fighters;

/**
 * Enum representing all possible states a fighter can be in.
 * The state machine controls which actions are valid at any given time.
 */
public enum FighterState {
    IDLE,           // Standing still, can transition to any action
    WALKING,        // Moving left or right
    JUMPING,        // In the air
    CROUCHING,      // Ducking down
    ATTACKING,      // Performing an attack (locked in animation)
    BLOCKING,       // Defending against attacks
    HITSTUN,        // Stunned after being hit (cannot act)
    KNOCKDOWN,      // On the ground after heavy hit
    KO              // Defeated (health <= 0)
}
