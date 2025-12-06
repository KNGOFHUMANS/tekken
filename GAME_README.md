# 2D Fighting Game - Complete Implementation Guide

A Tekken-style 2D fighting game built with Java and LibGDX, featuring:
- Frame-based combat system with hitboxes/hurtboxes
- Dynamic camera with zoom based on fighter distance
- State machine-driven fighter logic
- Round system (best of 3)
- Parallax background
- AI opponent support

## Project Structure

```
core/src/com/fightinggame/
├── FightingGame.java          # Main game class
├── screens/
│   ├── MainMenuScreen.java    # Main menu
│   ├── CharacterSelectScreen.java # Character selection
│   ├── FightScreen.java       # Main fight screen with dynamic camera
│   └── ResultScreen.java      # Victory screen
├── engine/
│   ├── GameWorld.java         # Match orchestrator & round system
│   ├── CollisionSystem.java   # Hitbox vs hurtbox detection
│   ├── InputManager.java      # Keyboard input handling
│   └── AIController.java      # Simple AI opponent (optional)
├── fighters/
│   ├── Fighter.java           # Fighter class with state machine
│   ├── FighterState.java      # State enum (IDLE, ATTACKING, etc.)
│   ├── Move.java              # Attack move with frame data
│   ├── Hitbox.java            # Offensive collision box
│   └── Hurtbox.java           # Defensive collision box
├── ui/
│   ├── HUD.java               # Health bars, timer, round indicators
│   └── HealthBar.java         # Visual health bar component
└── stages/
    └── Stage.java             # Stage with parallax background

desktop/src/com/fightinggame/desktop/
└── DesktopLauncher.java       # Desktop entry point
```

---

## Controls

### Player 1
- **Movement**: W (up/jump), A (left), S (down/crouch), D (right)
- **Attacks**: J (light punch), K (heavy punch), L (special)
- **Defense**: Left Shift (block)

### Player 2
- **Movement**: Arrow Keys (↑ jump, ← left, ↓ crouch, → right)
- **Attacks**: Numpad 1 (light), Numpad 2 (heavy), Numpad 3 (special)
- **Defense**: Numpad 0 (block)

### Menu Controls
- **Navigate**: Arrow keys or WS
- **Confirm**: Enter or J/Numpad 1
- **Back**: Escape

---

## Architecture Deep Dive

### 1. Fighter State Machine

The `Fighter.java` class implements a finite state machine controlling all fighter behavior:

```
IDLE → WALKING → IDLE
  ↓      ↓         ↓
JUMPING ← → ATTACKING → HITSTUN → KNOCKDOWN → IDLE
  ↓                                              ↓
CROUCHING ← → BLOCKING                          KO
```

**State Transitions**:
- `IDLE/WALKING/CROUCHING`: Fighter can move or attack
- `ATTACKING`: Locked in animation for startup + active + recovery frames
- `HITSTUN`: Stunned after being hit, cannot act
- `KNOCKDOWN`: On ground after launcher move
- `KO`: Health reached 0, match over

See `Fighter.java:update()` for the state machine implementation.

### 2. Frame Data System (Tekken-style)

Each `Move` has three phases:

1. **Startup Frames**: Frames before attack becomes active (can't hit yet)
2. **Active Frames**: Frames during which the hitbox is active and can hit
3. **Recovery Frames**: Frames after attack before returning to neutral

Example from `Fighter.java:initializeMoveset()`:
```java
// Light Punch: startup=3, active=2, recovery=5 (10 total frames)
Move lightPunch = new Move("Light Punch", 3, 2, 5, 5, 100f, 50f, ...);
```

At 60fps:
- Frame 0-2: Startup (can't hit)
- Frame 3-4: Active (hitbox active, can hit opponent)
- Frame 5-9: Recovery (can't act)
- Frame 10: Return to IDLE

### 3. Collision System

`CollisionSystem.java` checks every frame:

1. Get attacker's `Hitbox` and defender's `Hurtbox`
2. If hitbox is active (during active frames) AND overlaps hurtbox:
   - Apply damage from `Move.damage()`
   - Apply knockback from `Move.getKnockback()`
   - Put defender in HITSTUN or KNOCKDOWN state
   - Deactivate hitbox (prevents multi-hit)

See `CollisionSystem.java:checkHit()` for implementation.

### 4. Dynamic Camera System

`FightScreen.java:updateCamera()` implements Tekken-style camera:

1. **Center Position**: Camera X = midpoint between both fighters
2. **Dynamic Zoom**:
   - Close distance (< 200px) → Zoom IN (0.8x)
   - Far distance (> 800px) → Zoom OUT (1.5x)
   - Smooth interpolation between
3. **Smooth Movement**: Uses `MathUtils.lerp()` for smooth camera transitions

This creates cinematic zooming during close combat and pulls back when fighters are far apart.

### 5. Round System

`GameWorld.java` manages match flow:

- **RoundState.PRE_ROUND**: 2-second countdown before fight starts
- **RoundState.FIGHTING**: Active gameplay, checks for KO or timeout
- **RoundState.ROUND_END**: 2-second "Player X wins!" announcement
- **RoundState.MATCH_END**: One player won 2 rounds (best of 3)

After each round, fighters reset to full health and starting positions.

---

## How to Expand the Game

### Adding Real Sprite Animations

Currently, fighters are rendered as colored rectangles. To add sprite sheets:

1. **Prepare Sprite Sheets**:
   - Create sprite sheets for each state (idle, walk, attack, etc.)
   - Example: `ryu_idle.png` (6 frames), `ryu_punch.png` (8 frames)

2. **Load in Fighter Constructor**:
```java
// In Fighter.java constructor
TextureAtlas atlas = new TextureAtlas("fighters/ryu/ryu.atlas");
Animation<TextureRegion> idleAnim = new Animation<>(0.1f,
    atlas.findRegions("idle"), Animation.PlayMode.LOOP);
// Store in a Map<FighterState, Animation>
```

3. **Update Render Method**:
```java
// In Fighter.java:render()
TextureRegion currentFrame = getCurrentAnimation().getKeyFrame(stateTime);
batch.draw(currentFrame, position.x, position.y);
```

4. **Sync Hitboxes with Animation**:
   - Define hitbox positions per frame in Move class
   - Update hitbox position based on current animation frame

### Adding More Moves

In `Fighter.java:initializeMoveset()`:

```java
// Add a new move
Move spinKick = new Move(
    "Spin Kick",        // Name
    6, 3, 10,           // Startup, Active, Recovery
    12,                 // Damage
    200f, 150f,         // Knockback X, Y
    60f, 80f,           // Hitbox offset X, Y
    50f, 40f            // Hitbox size W, H
);
moveset.put("SPIN_KICK", spinKick);

// Trigger in InputManager.java
if (Gdx.input.isKeyPressed(Input.Keys.U)) {
    fighter1.startMove("SPIN_KICK");
}
```

### Adding Command Inputs (Quarter-Circle, etc.)

Create an `InputBuffer` class:

```java
public class InputBuffer {
    private Queue<Input> recentInputs;

    public void update(Input newInput) {
        recentInputs.add(newInput);
        checkForCommands();
    }

    private void checkForCommands() {
        // Check for ↓ ↘ → + Punch (quarter-circle forward)
        if (matchesPattern(new Input[]{DOWN, DOWN_RIGHT, RIGHT, PUNCH})) {
            fighter.startMove("HADOUKEN");
        }
    }
}
```

### Adding Block Stun & Frame Advantage

In `Fighter.java:applyDamage()`:

```java
public void applyDamage(int damage, Vector2 knockback, boolean blocked) {
    if (currentState == FighterState.BLOCKING && blocked) {
        // Reduce damage
        damage *= 0.2f;
        // Enter block stun instead of hitstun
        blockstunTime = 0.2f;
        changeState(FighterState.BLOCKSTUN);
    } else {
        // Normal hit
        health -= damage;
        changeState(FighterState.HITSTUN);
    }
}
```

### Adding Sound Effects

```java
// In FightingGame.java:create()
Sound hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hit.wav"));

// In CollisionSystem.java:checkHit()
if (attackerHitbox.overlaps(defenderHurtbox)) {
    hitSound.play();
    // ... apply damage
}
```

### Using AI for Single Player

In `InputManager.java`, replace Player 2 input:

```java
public class InputManager {
    private AIController aiController;
    private boolean player2IsAI;

    public InputManager(Fighter f1, Fighter f2, boolean p2IsAI) {
        this.fighter1 = f1;
        this.fighter2 = f2;
        this.player2IsAI = p2IsAI;

        if (p2IsAI) {
            aiController = new AIController(0.6f, 0.7f); // Medium difficulty
        }
    }

    public void update(float delta) {
        handlePlayer1Input();

        if (player2IsAI) {
            aiController.update(delta, fighter2, fighter1);
        } else {
            handlePlayer2Input();
        }
    }
}
```

### Adding Special Effects (Hit Sparks, etc.)

Create `ParticleEffect` or simple sprite effects:

```java
// In CollisionSystem.java:checkHit()
if (hit detected) {
    Vector2 hitPosition = new Vector2(hitbox center);
    effectManager.spawnHitSpark(hitPosition);
}

// EffectManager class
public class EffectManager {
    private Array<Effect> activeEffects;

    public void spawnHitSpark(Vector2 position) {
        Effect spark = new HitSpark(position);
        activeEffects.add(spark);
    }

    public void update(float delta) {
        for (Effect effect : activeEffects) {
            effect.update(delta);
            if (effect.isFinished()) {
                activeEffects.removeValue(effect, true);
            }
        }
    }
}
```

---

## Key Classes Reference

### Fighter.java
- **Main Methods**:
  - `update(delta)`: State machine update
  - `render(shapeRenderer)`: Draw fighter
  - `startMove(moveKey)`: Execute an attack
  - `applyDamage(damage, knockback, launcher)`: Take damage and knockback
  - `moveLeft/Right/jump/crouch/block()`: Movement actions

- **State Machine**: See `update()` switch statement (lines 120-160)

### GameWorld.java
- **Round System**: `updateFighting()`, `endRound()`, `startNewRound()`
- **Camera Helpers**: `getCameraX()`, `getFighterDistance()`
- **Match Flow**: `RoundState` enum controls pre-round, fighting, round-end, match-end

### CollisionSystem.java
- **Main Method**: `checkCollisions(fighter1, fighter2)` - called every frame
- **Hit Detection**: `checkHit(attacker, defender)` - overlap test + damage application

### FightScreen.java
- **Camera System**: `updateCamera(delta)` - dynamic zoom and centering
- **Rendering Pipeline**: Stage → Fighters → HUD (in render order)

---

## Performance Notes

### Current Simplifications

1. **No Sprite Sheets**: Using colored rectangles instead
   - Replace with `TextureRegion` and `Animation` for production

2. **Simple Parallax**: Using repeating colored rectangles
   - Replace with tiled textures or sprite backgrounds

3. **No Particle Effects**: Hit detection has no visual feedback
   - Add `ParticleEffect` or sprite-based effects

4. **Basic AI**: Random decision-making
   - Implement state machines, pattern recognition, difficulty levels

### Optimization Tips

- **Object Pooling**: Pool `Hitbox` and `Hurtbox` objects instead of recreating
- **Spatial Partitioning**: Only check collisions for nearby fighters (not needed for 1v1)
- **Animation Caching**: Cache `TextureRegion` lookups
- **Batch Rendering**: Minimize `batch.begin()/end()` calls

---

## Common Issues & Solutions

### Issue: Fighters pass through each other
**Solution**: Add pushback logic in `GameWorld.java`:
```java
if (collisionSystem.checkOverlap(fighter1, fighter2)) {
    // Push fighters apart
    float overlap = (f1.x + f1.width) - f2.x;
    fighter1.position.x -= overlap / 2;
    fighter2.position.x += overlap / 2;
}
```

### Issue: Attacks hit multiple times
**Solution**: Already handled in `CollisionSystem.java` - hitbox deactivates after first hit

### Issue: Camera shows too much empty space
**Solution**: Adjust zoom constants in `FightScreen.java`:
```java
private static final float MIN_ZOOM = 1.0f; // Less zoom in
private static final float MAX_ZOOM = 1.2f; // Less zoom out
```

### Issue: Fighters can't perform combos
**Solution**: Reduce recovery frames or add "cancel windows":
```java
// In Fighter.java:updateAttack()
if (moveFrame >= startupFrames + activeFrames &&
    moveFrame < totalFrames - CANCEL_WINDOW) {
    // Allow canceling into next move
    if (nextMoveRequested) {
        startMove(nextMove);
    }
}
```

---

## Next Steps

1. **Add Sprite Sheets**: Replace colored rectangles with character sprites
2. **Add Audio**: Hit sounds, music, voice lines
3. **More Characters**: Create unique movesets for each character
4. **Stage Variety**: Multiple stages with different parallax backgrounds
5. **Training Mode**: Show hitboxes, frame data, input display
6. **Online Multiplayer**: Use Kryonet or similar for netcode
7. **Replay System**: Record and playback matches
8. **Advanced AI**: Implement learning AI or difficulty levels

---

## Credits

Built with LibGDX framework (https://libgdx.com/)
Architecture inspired by Tekken's frame-based combat system

Enjoy your fighting game! 🥊
