package com.fightinggame.engine;

import com.badlogic.gdx.math.MathUtils;
import com.fightinggame.fighters.Fighter;
import com.fightinggame.fighters.FighterState;

/**
 * AI controller with attack patterns and combos.
 * Makes decisions based on distance, health, and attack patterns.
 */
public class AIController {

    private Fighter aiFighter;
    private Fighter opponent;

    // AI behavior parameters
    private float decisionTimer;
    private float currentDecisionDelay;
    private static final float MIN_DECISION_DELAY = 0.15f;
    private static final float MAX_DECISION_DELAY = 0.5f;

    // AI difficulty (0.0 to 1.0)
    private float aggressiveness; // How often AI attacks vs defends
    private float reactionSpeed;  // How fast AI makes decisions

    // Attack patterns
    private int currentPattern;
    private int patternStep;
    private float timeSinceLastAttack;
    private static final float COMBO_TIMEOUT = 1.5f;

    // Attack pattern sequences
    private static final String[][] ATTACK_PATTERNS = {
        {"PUNCH", "PUNCH", "KICK"},           // Pattern 0: Quick jabs into kick
        {"PUNCH", "KICK", "BLAST"},           // Pattern 1: Full combo
        {"KICK", "KICK"},                     // Pattern 2: Double kick
        {"PUNCH", "BLAST"},                   // Pattern 3: Punch into special
        {"KICK", "BLAST"},                    // Pattern 4: Heavy finisher
    };

    // Defensive behavior
    private int consecutiveHits;
    private float lastHealthCheck;
    private static final int BLOCK_THRESHOLD = 2;

    public AIController(float aggressiveness, float reactionSpeed) {
        this.aggressiveness = MathUtils.clamp(aggressiveness, 0, 1);
        this.reactionSpeed = MathUtils.clamp(reactionSpeed, 0, 1);
        this.decisionTimer = 0;
        this.currentDecisionDelay = randomDecisionDelay();
        this.currentPattern = -1;
        this.patternStep = 0;
        this.timeSinceLastAttack = 0;
        this.consecutiveHits = 0;
        this.lastHealthCheck = 100;
    }

    /**
     * Update AI behavior.
     * Call this instead of processing player input.
     */
    public void update(float delta, Fighter aiFighter, Fighter opponent) {
        this.aiFighter = aiFighter;
        this.opponent = opponent;

        // Track time since last attack for combo timing
        timeSinceLastAttack += delta;

        // Check if we took damage (for defensive reactions)
        if (aiFighter.getHealth() < lastHealthCheck) {
            consecutiveHits++;
            lastHealthCheck = aiFighter.getHealth();
        }

        // Only make decisions at intervals (simulates human reaction time)
        decisionTimer += delta;
        if (decisionTimer < currentDecisionDelay) {
            return;
        }

        // Make a decision
        makeDecision();

        // Reset timer
        decisionTimer = 0;
        currentDecisionDelay = randomDecisionDelay();
    }

    /**
     * Main AI decision logic with attack patterns.
     */
    private void makeDecision() {
        // Can't act if in hitstun, knockdown, or KO
        FighterState state = aiFighter.getCurrentState();
        if (state == FighterState.HITSTUN ||
                state == FighterState.KNOCKDOWN ||
                state == FighterState.KO) {
            // Reset pattern when interrupted
            currentPattern = -1;
            patternStep = 0;
            return;
        }

        // Defensive behavior - block if getting hit repeatedly
        if (consecutiveHits >= BLOCK_THRESHOLD && MathUtils.random() < 0.7f) {
            aiFighter.block();
            consecutiveHits = 0; // Reset after blocking
            return;
        }

        float distance = getDistanceToOpponent();

        // Check if opponent is attacking - block or dodge
        if (isOpponentAttacking() && distance < 150) {
            float reaction = MathUtils.random();
            if (reaction < reactionSpeed * 0.6f) {
                aiFighter.block();
                return;
            } else if (reaction < reactionSpeed * 0.8f) {
                moveAwayFromOpponent();
                return;
            }
        }

        // Continue attack pattern if active
        if (currentPattern >= 0 && timeSinceLastAttack < COMBO_TIMEOUT) {
            if (distance < 180) {
                executePatternStep();
                return;
            } else {
                // Too far, move closer
                moveTowardOpponent();
                return;
            }
        }

        // Pattern timed out, reset
        if (timeSinceLastAttack >= COMBO_TIMEOUT) {
            currentPattern = -1;
            patternStep = 0;
        }

        // Decision tree based on distance
        if (distance > 300) {
            // Far away - move toward opponent aggressively
            moveTowardOpponent();
        } else if (distance > 150) {
            // Medium range - start attack pattern or move closer
            if (MathUtils.random() < aggressiveness * 0.8f) {
                // Start new attack pattern
                startNewPattern();
            } else {
                // Move closer
                moveTowardOpponent();
            }
        } else {
            // Close range - very aggressive
            float action = MathUtils.random();

            if (action < aggressiveness * 0.85f) {
                // Start new attack pattern
                startNewPattern();
            } else if (action < aggressiveness * 0.9f) {
                // Block
                aiFighter.block();
                consecutiveHits = 0;
            } else {
                // Back off slightly
                moveAwayFromOpponent();
            }
        }

        // Occasional jump attacks
        if (MathUtils.random() < 0.08f && distance < 200) {
            aiFighter.jump();
        }
    }

    /**
     * Move toward the opponent.
     */
    private void moveTowardOpponent() {
        float aiX = aiFighter.getPosition().x;
        float opponentX = opponent.getPosition().x;

        if (aiX < opponentX) {
            aiFighter.moveRight();
        } else {
            aiFighter.moveLeft();
        }
    }

    /**
     * Move away from the opponent.
     */
    private void moveAwayFromOpponent() {
        float aiX = aiFighter.getPosition().x;
        float opponentX = opponent.getPosition().x;

        if (aiX < opponentX) {
            aiFighter.moveLeft();
        } else {
            aiFighter.moveRight();
        }
    }

    /**
     * Start a new attack pattern.
     */
    private void startNewPattern() {
        // Choose a random attack pattern
        currentPattern = MathUtils.random(ATTACK_PATTERNS.length - 1);
        patternStep = 0;
        executePatternStep();
    }

    /**
     * Execute the current step of the active attack pattern.
     */
    private void executePatternStep() {
        if (currentPattern < 0 || currentPattern >= ATTACK_PATTERNS.length) {
            return;
        }

        String[] pattern = ATTACK_PATTERNS[currentPattern];
        if (patternStep >= pattern.length) {
            // Pattern complete, reset
            currentPattern = -1;
            patternStep = 0;
            return;
        }

        // Execute the attack for this step
        String attack = pattern[patternStep];
        aiFighter.startMove(attack);
        timeSinceLastAttack = 0;

        // Move to next step
        patternStep++;

        // If pattern is complete, reset for next time
        if (patternStep >= pattern.length) {
            currentPattern = -1;
            patternStep = 0;
        }
    }

    /**
     * Check if opponent is currently attacking.
     */
    private boolean isOpponentAttacking() {
        FighterState opponentState = opponent.getCurrentState();
        return opponentState == FighterState.ATTACKING;
    }

    /**
     * Calculate distance to opponent.
     */
    private float getDistanceToOpponent() {
        float aiX = aiFighter.getPosition().x;
        float opponentX = opponent.getPosition().x;
        return Math.abs(opponentX - aiX);
    }

    /**
     * Get a random decision delay based on reaction speed.
     */
    private float randomDecisionDelay() {
        // Higher reaction speed = shorter delays
        float baseDelay = MathUtils.random(MIN_DECISION_DELAY, MAX_DECISION_DELAY);
        return baseDelay * (1.0f - reactionSpeed * 0.5f);
    }
}
