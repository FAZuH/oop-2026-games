# OOP 2026 — one runnable game per lecture session

**Object Oriented Programming (ENCE603010)**
Computer Engineering, Department of Electrical Engineering, Universitas Indonesia

Six small, self-contained JavaFX games. Each one exists to make a specific pair
of OOP ideas **visible** — you run it, you press a key, and you watch the design
decision matter.

There is no shared game engine and no dependency between sessions: if you missed
Session 2 you can still run and understand Session 4.

---

## Quick start

You need **one** thing: a **JDK 21**. Everything else, including Gradle itself,
is downloaded by the wrapper on first run.

```bash
git clone <this repository>
cd oop-2026-games

./gradlew runS01        # Windows: gradlew.bat runS01
```

> **Starting from a machine with nothing installed?**
> Follow **[docs/setup.md](docs/setup.md)** — step-by-step instructions with
> direct download links for every platform, a verification checklist, and fixes
> for the eight failure modes people actually hit.

Verify your setup without playing anything:

```bash
./gradlew test          # the Session 6 unit tests
./gradlew shotAll       # renders every game to build/shots/*.png and exits
```

If `./gradlew` fails with *"JAVA_HOME is not set"*, install a JDK 21 and open a
**new terminal** — a PATH change does not reach a shell that is already open.
Other common failures are covered in [docs/setup.md](docs/setup.md#troubleshooting).

---

## The six games

**[📖 docs/games.md](docs/games.md) — what each game looks like, with screenshots,
and what it is for.** The short version:

| Run | Game | Patterns it makes visible | The thing to actually try |
|---|---|---|---|
| `./gradlew runS01` | **Pong** | Game Loop, delta time, **Singleton** | Press `N` then `F`. Naive movement ties the ball's speed to the frame rate; the dt-driven version does not. |
| `./gradlew runS02` | **Shooter** | **Factory Method**, **Object Pool** | Press `P`. With pooling on, only ~40 `Bullet` objects are ever built. With it off, watch the allocation counter climb without limit. |
| `./gradlew runS03` | **Tower Defense** | **Command**, **Observer** | Place towers, sell one, then `Ctrl+Z`. Press `L` to add a new listener at runtime — the battle log grows without `World` or `Enemy` being touched. |
| `./gradlew runS04` | **Chase** | **Strategy**, **State** | Press `1`, `2`, `3` mid-run. Every hunter changes how it thinks, with no new class. `P` pauses — note that `Space` means something different in each state. |
| `./gradlew runS05` | **Breakout** | **Template Method**, **Facade** | Press `N`. Two levels, built by the *same* `final load()` algorithm; only the steps and the optional hook differ. |
| `./gradlew runS06` | **Snake** | File I/O, **exception design**, **unit testing** | Press `S` to save. Then hand-edit `build/scores/snake-highscores.csv`, break a line, and restart — it degrades gracefully instead of crashing. |

---

## What each session's code is for

```
src/main/java/id/ac/ui/oop/
├── s01pong/          ScoreManager (Singleton), Paddle, Ball, PongGame
├── s02shooter/       Pooling (ObjectPool/Poolable/Factory), Enemies (+factories), Bullet
├── s03towerdefense/  Commands (+CommandHistory), Events (EventBus), World, Tower, Enemy
├── s04chase/         ChaseStrategy (3 variants), ChaseGame (4 nested GameStates)
├── s05breakout/      LevelLoader (template method), Levels, GameEngine (facade), Brick
├── s06snake/         SnakeLogic (no JavaFX — testable), HighScoreRepository
└── devtools/         Launch, Screenshot — TEST SCAFFOLDING, not course material
```

Two deliberate design choices worth noticing as you read:

**`SnakeLogic` contains no JavaFX at all.** That is why `SnakeLogicTest` can test
the game rules without opening a window. Testability is a consequence of keeping
rules separate from drawing — you cannot add it afterwards.

**`devtools/Launch`** exists for a specific reason. Launching a class that
extends `Application` directly makes the JVM demand a `--module-path` and fail
with *"JavaFX runtime components are missing"*. Starting from a class that does
**not** extend `Application` sidesteps that, so every run task works with no
module flags anywhere in the project.

---

## Reading order for each session

1. Run the game and play with the key listed in the table above.
2. Read the class that names the pattern (e.g. `Commands.java` for Session 3).
   Every one opens with a comment explaining *why* the pattern is there and what
   it costs.
3. Read the game class last — it is mostly drawing and input forwarding.

---

## Requirements

| | |
|---|---|
| JDK | 21 (tested: Temurin 21.0.12.1) — [download links](docs/setup.md#1-install-jdk-21-eclipse-temurin) |
| Build | Gradle 8.10.2 via the committed wrapper — **do not install Gradle** |
| UI | JavaFX 21.0.4, resolved automatically by `org.openjfx.javafxplugin` — **do not install a JavaFX SDK** |
| Tests | JUnit 5.10.2 |
| Editor | optional; VS Code or IntelliJ IDEA Community — [setup](docs/setup.md#7-set-up-an-editor-recommended) |

All dependencies are free and open source. Nothing needs a network connection
after the first build. Full installation instructions: **[docs/setup.md](docs/setup.md)**.
