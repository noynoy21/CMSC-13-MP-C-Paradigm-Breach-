# ParadigmBreach

ParadigmBreach is a JavaFX desktop-game prototype. It opens with an animated splash screen, then presents a desktop with a terminal and a tutorial window. The terminal currently contains the game's question-and-answer prototype.

## Requirements

- JDK 25
- Maven, or the included Maven Wrapper
- JavaFX dependencies are managed through `pom.xml`

## Run the application

From the project folder, use PowerShell.

For a clean build, such as the first run or after adding or removing files:

```powershell
.\mvnw.cmd clean javafx:run
```

For a normal run without cleaning compiled files:

```powershell
.\mvnw.cmd javafx:run
```

If the normal run fails to compile, try the clean build command.

Press a key or click during the splash screen to continue. On the desktop, double-click an icon to open it. The tutorial window opens with the desktop; the Terminal icon opens the game terminal.

## Project layout

```text
src/main/java/
├── module-info.java
└── cmsc13/ParadigmBreach/
    ├── App.java
    ├── audio/                 # Music and sound effect assets and playback
    ├── model/                 # Question and game-stage logic
    │   ├── GameStageManager.java
    │   └── questionBank/      # Question model and question banks
    ├── ui/                    # Screen definitions and window management
    ├── util/                  # Shared UI classifications and utilities
    └── view/                  # JavaFX controllers and terminal output

src/main/resources/
├── audio/                     # Music and sound effects
├── css_files/                 # Stylesheets for screens
├── csv_files/                 # Question data files
├── fonts/                     # Bundled Cascadia Mono font
├── fxml_files/                # JavaFX layouts and tutorial pages
├── images/                    # Desktop, splash, and tutorial artwork
└── licenses/                  # Third-party asset licenses
```

## How the application is organized

- **Startup:** `App` initializes the scene and audio managers, then opens the splash screen.
- **Screens and windows:** `Screen` maps screen names to FXML paths, titles, sizes, and single-instance settings. `SceneManager` loads those layouts and opens or focuses their windows.
- **Views:** FXML files define layouts; controllers in `view` handle interaction. CSS files style the layouts.
- **Terminal:** `TerminalController` reads input and routes commands. `TerminalOutput` adds styled text to the terminal and summary panes.
- **Question flow:** `GameStageManager` coordinates question prompts and answers. `Question` stores a prompt, choices, the correct-choice index, and question state. `QuestionBank` stores and shuffles questions.
- **Audio:** `AudioManager` plays music and sound effects. `MusicTrack` and `SoundEffect` associate audio files with resource paths and volumes.

## Current gameplay commands

| Command | Behavior |
| --- | --- |
| `help` | Displays the terminal help text. |
| `ask` | Displays a shuffled question. Use it again after answering to move to the next question. |
| `ans <choice>` | Marks the current question as answered. The supplied choice is not currently checked. |
| `exit` | Closes the terminal window. |

Commands are case-insensitive. `clear` and the `pngu` commands described in the in-game tutorial are not currently handled by the terminal.

## Developing the game

- **Add or change a screen:** Define its FXML path and window settings in `ui/Screen.java`. Add or update its FXML layout and controller. Use `SceneManager` to open it.
- **Add a terminal command:** Handle the command in `view/TerminalController.java` and update the help text there. Keep question-related behavior in `model/GameStageManager.java`.
- **Add questions:** Create or update a `QuestionBank` subclass in `model/questionBank`. `GameStageManager` currently creates `OOPStage`, so a new bank must be connected there to become active. Question banks store the correct-choice index, but the current answer flow does not validate it.
- **Add or change an asset:** Put it under the appropriate `src/main/resources` folder and reference it using a classpath resource path, such as `/images/desktop_icons/command_line.png`. Add audio assets to the relevant enum so `AudioManager` can find them.
- **Keep the tutorial in sync:** Its content is embedded in `fxml_files/readme-md-tutorial.fxml`. Update it when terminal commands or gameplay behavior change.

The `Stage1Questions` and `Stage2Questions` classes are placeholders; `Stage3Questions` is not connected to the active stage. `GameStage` is currently empty, and `csv_files/stage_1_questions.csv` is empty.

## Git branch naming

When adding a feature:
- `feature/{feature-name}`

When editing the aesthetics of a component/s:
- `looks/{component-name}`

When fixing a bug:
- `fix/{bug-fix}`

When adding/editing documentation:
- `docs/{documentation-edit}`

When cleaning up code (no code behaviour change):
- `refactor/{code-cleanup}`

## README.md documentation edits
- Must be updated every git commit/merge to main number that is a multiple of 10. (e.g. commit no. 20, 30, ...)