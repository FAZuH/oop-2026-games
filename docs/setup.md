# Environment setup — step by step

This guide takes a machine with **nothing installed** to a machine that can run
all six games. Follow it in order. Every command shown here has been run on a
real Windows 11 machine, and the outputs quoted are the actual outputs.

**Time needed:** 15–25 minutes, most of it downloading.
**Disk space:** about **1 GB** (JDK ≈ 330 MB, Gradle + JavaFX caches ≈ 610 MB).
**Network:** needed for the first build only. After that the project builds offline.

---

## 0. What you actually need

| Software | Required? | Why |
|---|---|---|
| **JDK 21** | ✅ **Yes** | Compiles and runs the games. This is the *only* mandatory install. |
| **Git** | ✅ Yes (practically) | To clone the repository. You could download a ZIP instead, but Git is used in the course. |
| **VS Code** *or* **IntelliJ IDEA** | ⬜ Recommended | You can build and play with only a terminal, but you cannot comfortably *read* 30 classes without an editor that understands Java. |
| Gradle | ❌ **NO — do not install** | The project ships its own (`gradlew`). Installing Gradle separately causes version mismatches. |
| JavaFX SDK | ❌ **NO — do not install** | Gradle downloads the right JavaFX for your platform automatically. |
| Scene Builder | ❌ No | This project draws on a `Canvas` in code; there are no `.fxml` files. |

> **The student contract is one line:** install a JDK 21, then `./gradlew runS01`.
> If any instruction ever tells you to set `--module-path`, download JavaFX by
> hand, or install Gradle — it is not this project's instruction. Ignore it.

---

## 1. Install JDK 21 (Eclipse Temurin)

Java 21 is a Long Term Support release. **Use 21, not 25 or 17.** The build pins
Java 21 and compiles against that language level.

### Windows

**Option A — direct download (recommended).** Download the `.msi` installer and
double-click it:

- 🔗 **[Temurin 21 for Windows x64 — direct `.msi` download](https://api.adoptium.net/v3/installer/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse)**
- Browse all versions instead: <https://adoptium.net/temurin/releases/?version=21>

In the installer, on the "Custom Setup" screen, make sure these two options are
enabled — they are **not** all switched on by default:

- ✅ **Add to PATH**
- ✅ **Set JAVA_HOME variable**

**Option B — one command, if you have winget** (built into Windows 11):

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK
```

### macOS

- 🔗 **[Temurin 21 for Apple Silicon (M1/M2/M3/M4) — direct `.pkg`](https://api.adoptium.net/v3/installer/latest/21/ga/mac/aarch64/jdk/hotspot/normal/eclipse)**
- 🔗 **[Temurin 21 for Intel Macs — direct `.pkg`](https://api.adoptium.net/v3/installer/latest/21/ga/mac/x64/jdk/hotspot/normal/eclipse)**

Or with Homebrew:

```bash
brew install --cask temurin@21
```

### Linux

- 🔗 **[Temurin 21 for Linux x64 — direct `.tar.gz`](https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse)**

Or from your distribution's packages:

```bash
sudo apt install openjdk-21-jdk          # Debian / Ubuntu
sudo dnf install java-21-openjdk-devel   # Fedora / RHEL
```

---

## 2. Verify Java — **in a new terminal**

> ⚠️ **The single most common setup failure.** Installing a JDK changes your
> `PATH`, and a terminal that was already open keeps the *old* `PATH` forever.
> **Close every terminal and open a new one** before running these commands.
> If your editor was open too, restart it.

```bash
java -version
javac -version
```

Expected — the version must start with **21**:

```
openjdk version "21.0.12.1" 2026-08-18 LTS
OpenJDK Runtime Environment Temurin-21.0.12.1+1 (build 21.0.12.1+1-LTS)
javac 21.0.12.1
```

If you get *"command not found"*, or a version that is not 21, see
[Troubleshooting](#troubleshooting) problems 1 and 2.

---

## 3. Install Git

- **Windows:** 🔗 **[Git for Windows — download](https://git-scm.com/download/win)**
  (accept every default; it also gives you Git Bash)
- **macOS:** 🔗 <https://git-scm.com/download/mac> — or `brew install git`, or
  simply run `git --version` once and let macOS offer to install the developer tools
- **Linux:** `sudo apt install git` / `sudo dnf install git`

Verify:

```bash
git --version
```

---

## 4. Get the project

```bash
git clone <repository URL>
cd oop-2026-games
```

You should now see `gradlew`, `gradlew.bat`, `build.gradle`, and `src/`.

---

## 5. First build

This is the step that downloads everything else — Gradle itself, JavaFX for your
platform, and JUnit. **It needs a network connection and takes a few minutes the
first time.** Every later build takes seconds.

```bash
./gradlew test          # macOS / Linux / Git Bash
gradlew.bat test        # Windows PowerShell or cmd
```

Expected ending:

```
BUILD SUCCESSFUL in 1m 4s
```

The first run prints a download progress bar for `gradle-8.10.2-bin.zip`. That
is normal and happens exactly once.

> **You never install Gradle.** `gradlew` is a small script committed to the
> repository that fetches the exact Gradle version this project was built with.
> That is why your build behaves identically to your lecturer's.

---

## 6. Run a game

```bash
./gradlew runS01        # Windows: gradlew.bat runS01
```

A window titled *"Session 1 — Pong: Game Loop & Singleton"* opens. Close the
window to end the program; the terminal then returns to `BUILD SUCCESSFUL`.

All six:

| Command | Game |
|---|---|
| `./gradlew runS01` | Pong — Game Loop & Singleton |
| `./gradlew runS02` | Shooter — Factory Method & Object Pool |
| `./gradlew runS03` | Tower Defense — Command & Observer |
| `./gradlew runS04` | Chase — Strategy & State |
| `./gradlew runS05` | Breakout — Template Method & Facade |
| `./gradlew runS06` | Snake — Persistence, Exceptions & Testing |

Check every game at once without playing anything — each renders, saves a PNG to
`build/shots/`, and exits by itself:

```bash
./gradlew shotAll
```

> **Expected, harmless message.** Every run prints:
> `WARNING: Unsupported JavaFX configuration: classes were loaded from 'unnamed module'`
> This is **not** an error and nothing is broken. It is the deliberate
> consequence of how this project launches JavaFX without module flags — see
> `devtools/Launch.java`. Ignore it.

---

## 7. Set up an editor (recommended)

### Option A — Visual Studio Code

1. Install VS Code:
   - 🔗 **[VS Code for Windows — direct `.exe` (User Installer)](https://code.visualstudio.com/sha/download?build=stable&os=win32-x64-user)**
   - 🔗 **[VS Code for Linux — direct `.deb`](https://code.visualstudio.com/sha/download?build=stable&os=linux-deb-x64)**
   - 🔗 All platforms, including macOS: <https://code.visualstudio.com/download>

2. Install the Java tooling — **one extension pack that pulls in six others**
   (language support, debugger, test runner, project explorer, Maven, Gradle):

   ```bash
   code --install-extension vscjava.vscode-java-pack
   ```

   Or from the Marketplace:
   🔗 <https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack>

3. Open the `oop-2026-games` folder. The first time, VS Code spends a minute
   importing the Gradle project — wait for the status bar to settle before
   expecting code navigation to work.

> Install **one** Java language server, not two. If you also have Oracle's
> "Java" extension installed alongside the Red Hat one from this pack, disable
> one of them — running both produces duplicated errors and slow project loading.

### Option B — IntelliJ IDEA Community Edition (free)

- 🔗 **[IntelliJ IDEA Community — direct download for Windows](https://download.jetbrains.com/product?code=IIC&latest&distribution=windows)**
- 🔗 All platforms: <https://www.jetbrains.com/idea/download/>
  (scroll to **Community Edition** — Ultimate is paid and not needed here)

Open the folder and choose "Trust project". IDEA reads `build.gradle` itself; do
not create a new project.

---

## 8. Verification checklist

You are ready for the first lecture when all five of these pass:

```bash
java -version           # 1. reports 21.x
git --version           # 2. reports any version
./gradlew test          # 3. BUILD SUCCESSFUL
./gradlew shotAll       # 4. BUILD SUCCESSFUL, build/shots/ holds S01..S06 .png
./gradlew runS01        # 5. a Pong window opens; closing it ends the build
```

---

## Troubleshooting

### Problem 1 — `ERROR: JAVA_HOME is not set and no 'java' command could be found`

The JDK is installed but this terminal cannot see it.

**Fix:** close the terminal and open a **new** one. A `PATH` change never reaches
an already-running shell. Restart your editor too, since it inherits the same
stale environment.

If a new terminal still fails, the installer did not add Java to `PATH`. Set it
for the current session:

```powershell
# Windows PowerShell — adjust to your actual JDK folder
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

```bash
# macOS / Linux
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
```

Then re-run the installer with **Add to PATH** and **Set JAVA_HOME** enabled to
make it permanent.

### Problem 2 — `java -version` reports 17, 25, or anything that is not 21

You have **more than one JDK installed**, and the wrong one comes first on your
`PATH`. This is common and easy to miss — it is exactly what happened on the
machine this project was developed on, where JDK 25 sat ahead of JDK 21.

**Good news:** the build usually survives it. `build.gradle` pins
`languageVersion = 21`, so Gradle searches your machine for a real JDK 21 and
uses it for compilation regardless of what `java` on the `PATH` reports. Check
what Gradle can see:

```bash
./gradlew -q javaToolchains
```

If an entry with `Language Version: 21` is listed, you are fine. If not, install
JDK 21 (step 1).

To fix the `PATH` order properly on Windows: *Settings → System → About →
Advanced system settings → Environment Variables*, then move the JDK 21 `bin`
entry above the other JDK entries in `Path`.

### Problem 3 — `Error: JavaFX runtime components are missing`

You are launching a game class directly instead of through Gradle.

**Fix:** always start games with `./gradlew runS01` … `runS06`. Do **not** press
"Run" on `PongGame.java` in your IDE, and do not run `java PongGame`. Classes
that extend `Application` demand module flags when launched directly; the
`runSNN` tasks route around this via `devtools/Launch`.

### Problem 4 — the build fails after copying the project between computers

Symptoms: JavaFX classes not found, or an error mentioning `win`, `mac`, `linux`
or `aarch64`.

**Cause:** JavaFX jars are platform-specific, and a copied `build/` folder or
Gradle cache carries the *other* machine's platform.

**Fix:**

```bash
./gradlew --stop
rm -rf build .gradle
./gradlew runS01
```

```powershell
# Windows PowerShell equivalent
.\gradlew.bat --stop
Remove-Item -Recurse -Force build, .gradle
.\gradlew.bat runS01
```

Better: `git clone` the project on each machine rather than copying a folder.

### Problem 5 — `./gradlew clean` fails on Windows, or files are "in use"

A Gradle daemon is still holding the `build/` folder.

```powershell
.\gradlew.bat --stop
Remove-Item -Recurse -Force build
```

### Problem 6 — `./gradlew: permission denied` (macOS / Linux)

```bash
chmod +x gradlew
```

### Problem 7 — the first build hangs or times out

You are behind a proxy or a slow link while Gradle fetches
`gradle-8.10.2-bin.zip`. Let it run — it is a large one-time download. If it
fails outright, re-run the same command; Gradle resumes rather than starting
over.

### Problem 8 — `WARNING: Unsupported JavaFX configuration: ... unnamed module`

Not a problem. See the note at the end of [step 6](#6-run-a-game). Expected on
every run, on every platform.

---

## Working offline (lab machines, no network)

After one successful build on a networked machine, everything lives in your
Gradle home (`~/.gradle` on macOS/Linux, `%USERPROFILE%\.gradle` on Windows).
You can then build with:

```bash
./gradlew --offline runS01
```

For a lab room with no internet at all, copy `~/.gradle` from a prepared machine
to each lab machine **running the same operating system** — JavaFX jars are
platform-specific (see Problem 4).

---

## Pinned versions

These are the exact versions this project is built and tested against. They are
declared in `build.gradle` and `gradle/wrapper/gradle-wrapper.properties`; you do
not install any of them by hand except the JDK.

| Component | Version | Where it comes from |
|---|---|---|
| JDK | Temurin **21.0.12.1+1 LTS** | you install it (step 1) |
| Gradle | **8.10.2** | `gradlew` downloads it |
| JavaFX / OpenJFX | **21.0.4** | Gradle downloads it |
| `org.openjfx.javafxplugin` | **0.1.0** | Gradle downloads it |
| JUnit | **5.10.2** | Gradle downloads it |

---

## Appendix — instructor / maintainer notes

Students never need this section.

**Regenerating the Gradle wrapper.** The wrapper is committed, so this is needed
only when deliberately upgrading Gradle. It requires a Gradle installation,
which is a one-time local bootstrap — download and unzip:

🔗 <https://services.gradle.org/distributions/gradle-8.10.2-bin.zip>

```bash
/path/to/gradle-8.10.2/bin/gradle wrapper --gradle-version 8.10.2
```

> Note: `winget install Gradle.Gradle` fails on some Windows machines — the
> package is not always present in the catalogue. The ZIP above is the reliable
> route, and it is another reason students are never asked to install Gradle.

**Full instructor toolchain, from scratch on Windows:**

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK
winget install --id Git.Git
winget install --id Microsoft.VisualStudioCode
code --install-extension vscjava.vscode-java-pack
# then open a NEW terminal
```

**Confirming a release is sound** before a lecture:

```bash
./gradlew --stop
./gradlew clean test shotAll
```

All six PNGs should be regenerated in `build/shots/`, and the Session 6 tests
should pass.
