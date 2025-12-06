# Quick Start Guide

## Prerequisites

- Java 8 or higher
- LibGDX project structure (core + desktop modules)

## Minimal Gradle Setup

### `build.gradle` (root)

```gradle
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        google()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
    }
}

allprojects {
    version = '1.0'
    ext {
        appName = "FightingGame"
        gdxVersion = '1.11.0'
    }

    repositories {
        mavenCentral()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
        maven { url "https://oss.sonatype.org/content/repositories/releases/" }
    }
}

project(":desktop") {
    apply plugin: "java-library"

    dependencies {
        implementation project(":core")
        api "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
        api "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    }
}

project(":core") {
    apply plugin: "java-library"

    dependencies {
        api "com.badlogicgames.gdx:gdx:$gdxVersion"
    }
}
```

### `settings.gradle`

```gradle
include 'desktop', 'core'
```

### `desktop/build.gradle`

```gradle
sourceCompatibility = 1.8
sourceSets.main.java.srcDirs = [ "src/" ]
sourceSets.main.resources.srcDirs = ["../core/assets"]

project.ext.mainClassName = "com.fightinggame.desktop.DesktopLauncher"
project.ext.assetsDir = new File("../core/assets")

task run(dependsOn: classes, type: JavaExec) {
    main = project.mainClassName
    classpath = sourceSets.main.runtimeClasspath
    standardInput = System.in
    workingDir = project.assetsDir
    ignoreExitValue = true
}

task dist(type: Jar) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes 'Main-Class': project.mainClassName
    }
    dependsOn configurations.runtimeClasspath
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    with jar
}
```

## Running the Game

### Option 1: Command Line
```bash
# From project root
./gradlew desktop:run

# On Windows
gradlew.bat desktop:run
```

### Option 2: IDE (IntelliJ IDEA)
1. Import project as Gradle project
2. Wait for dependencies to download
3. Run `DesktopLauncher.java` main method
4. Enjoy!

### Option 3: Create JAR
```bash
./gradlew desktop:dist
# JAR will be in desktop/build/libs/
java -jar desktop/build/libs/desktop-1.0.jar
```

## Project Structure

After setup, your structure should be:
```
project/
├── build.gradle
├── settings.gradle
├── core/
│   ├── build.gradle
│   ├── src/com/fightinggame/
│   │   ├── FightingGame.java
│   │   ├── screens/
│   │   ├── engine/
│   │   ├── fighters/
│   │   ├── ui/
│   │   └── stages/
│   └── assets/ (optional - for sprites, sounds, etc.)
└── desktop/
    ├── build.gradle
    └── src/com/fightinggame/desktop/
        └── DesktopLauncher.java
```

## Controls Quick Reference

### Player 1
- WASD = Move
- J/K/L = Light/Heavy/Special
- Left Shift = Block

### Player 2
- Arrow Keys = Move
- Numpad 1/2/3 = Light/Heavy/Special
- Numpad 0 = Block

## First Run

When you first run the game:

1. **Main Menu** appears
   - Use UP/DOWN to select "Start Game"
   - Press ENTER

2. **Character Select** appears
   - P1: Use W/S to browse, J to confirm
   - P2: Use UP/DOWN to browse, Numpad 1 to confirm
   - Both players ready? Press ENTER

3. **Fight!**
   - Round timer counts down from 99
   - First to 2 rounds wins
   - Health bars at top of screen

4. **Victory Screen**
   - Shows winner
   - Press ENTER to return to menu

## Troubleshooting

### "Could not find LibGDX dependencies"
Run: `./gradlew --refresh-dependencies`

### "Main class not found"
Check `desktop/build.gradle` has:
```gradle
project.ext.mainClassName = "com.fightinggame.desktop.DesktopLauncher"
```

### "No assets directory"
Create `core/assets/` folder (can be empty for now)

### Game window doesn't appear
Check console for errors. Common issues:
- Java version < 8
- LWJGL3 natives missing (should auto-download via Gradle)

### Fighters not visible
They're rendered as colored rectangles. Look for:
- Red rectangle (Player 1)
- Orange/Blue rectangle (Player 2)
If still not visible, check camera position in `FightScreen.java`

### Input not working
- Try clicking the game window to focus it
- Check keyboard layout (QWERTY assumed)
- Check console logs for "InputManager" messages

## Next Steps

See `GAME_README.md` for:
- Full architecture documentation
- How to add sprite sheets
- How to expand the game
- Performance optimization tips

Happy coding! 🎮
