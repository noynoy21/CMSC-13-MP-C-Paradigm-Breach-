# ParadigmBreach

ParadigmBreach is a JavaFX desktop game prototype for learning about programming paradigms. The player explores a simulated desktop, answers timed multiple-choice questions in a terminal, and advances through stages guarded by paradigm-themed wardens.

## Requirements

- JDK 25
- Maven, or the included Maven Wrapper
- JavaFX Controls, FXML, and Media 27, plus SnakeYAML 2.2 (declared in `pom.xml`)

## Run the application

From the project root, run the Maven JavaFX goal. A clean run is useful after changing project files or when a previous build is stale.

Windows PowerShell:

```powershell
.\mvnw.cmd clean javafx:run
```

For a normal run, omit `clean`:

```powershell
.\mvnw.cmd javafx:run
```

macOS or Linux:

```sh
./mvnw clean javafx:run
```

The Maven entry point is `cmsc13.ParadigmBreach.App`.

## Start playing

1. Press a key or click during the animated splash screen.
2. Enter an operator name. If save slots already exist, select a loadable slot or start a new session.
3. Continue through the opening transmission. The desktop opens with the tutorial available.
4. Double-click the Terminal icon to open the game. The Files icon opens the in-game tutorial.

The terminal keeps its game state when its window is hidden and reopened. Use `exit` to hide the terminal; use the main desktop window's close button to leave the application. Once the terminal has been opened, closing the desktop offers save, exit-without-saving, and cancel options.

## Gameplay

The game progresses through these stages:

| Stage | Topic |
| --- | --- |
| 0 | Initialization trial |
| 1 | Introduction to programming paradigms |
| 2 | Procedural programming |
| 3 | Functional programming |
| 4 | Object-oriented programming |
| 5 | Imperative vs. declarative programming |
| 6 | Event-driven programming |
| 7 | Component mappings between programming paradigms |

Question banks currently contain 146 questions across the eight stages. A stage selects and shuffles up to 10 questions from its bank (Stage 0 currently has one). Answer choices are shuffled for display while the correct answer remains associated with its original choice.

Each question has a 30-second timer. A wrong answer or timeout costs one of the player's four lives. A stage advances after its selected questions are resolved while at least one life remains. Pengu has two peek tokens per stage.

### Terminal commands

Commands are case-insensitive.

| Command | Behavior |
| --- | --- |
| `help` | Displays the terminal's command help. |
| `ask` | Starts the next unanswered question. Answer the current question before asking again. |
| `ans <a\|b\|c\|d>` | Submits the displayed choice. |
| `pngu -peek` | Uses one of the two stage tokens to reveal Pengu's thought for the current question. Repeated peeks on that question return the same choice. |
| `pngu -copy` | Lets Pengu submit a separate random guess for the current question without spending a token. |
| `stage <0-7>` | Switches to the selected stage and resets that stage's questions, lives, and Pengu tokens. This is also useful for replaying a stage. |
| `save -s <1-3>` | Saves current progress to the selected slot, replacing its previous contents. |
| `exit` | Hides the terminal window. |

## Save files

The game provides three numbered save slots. Saves are YAML files in the operating system's application-data directory:

- Windows: `%APPDATA%\ParadigmBreach\slots\slot-1.yml` (and `slot-2.yml`, `slot-3.yml`)
- macOS: `~/Library/Application Support/ParadigmBreach/slots/`
- Linux: `$XDG_DATA_HOME/ParadigmBreach/slots/`, or `~/.local/share/ParadigmBreach/slots/` when `XDG_DATA_HOME` is unset

Progress is written only when a slot is explicitly selected, either with `save -s <1-3>` or through the save-before-exit flow. The save records the player, stage, selected question order and outcomes, current question and timer, remaining lives and Pengu tokens, and stage-summary history. Existing per-player saves from the older format are migrated into available slots when possible.

## Project structure

```text
pom.xml                              Maven dependencies and JavaFX run configuration
src/main/java/
  module-info.java                   Java module declarations
  cmsc13/ParadigmBreach/
    App.java                         JavaFX application entry point
    audio/                           Music, sound-effect definitions, and playback
    model/                           Session, stage, question, assistant, and warden logic
      questionBank/                  Question model, YAML loader, and stage bank classes
    persistence/                     Versioned save data and slot file storage
    ui/                              Screen definitions and window/scene management
    util/                            Timers, lives, tokens, output types, and formatting
    view/                            JavaFX controllers and terminal output
src/main/resources/
  audio/                             Music and sound effects
  css_files/                         Screen stylesheets
  fonts/                             Bundled Cascadia Mono font and its license
  fxml_files/                        Screen layouts and tutorial pages
  images/                            Desktop, cutscene, character, and tutorial artwork
  licenses/                          Third-party asset licenses
  yaml_files/                        Active question banks for stages 0–7
backup/                              Older CSV, YAML, and Java question-bank material
```

`src/main/resources/yaml_files/` contains the question data used at runtime. The legacy material under `backup/` is archival and is not loaded by the current game.

## How the application is organized

- `App` initializes the scene and audio managers, opens the splash screen, and starts JavaFX.
- `ui/Screen` maps screens to FXML resources and window settings. `ui/SceneManager` loads layouts, opens windows, and retains single-instance windows such as the terminal.
- FXML files define the interface; controllers in `view/` implement screen interaction. CSS under `css_files/` styles the screens.
- `TerminalController` handles terminal input and coordinates the timer, life display, Pengu reactions, save prompts, and warden responses. `TerminalOutput` formats and routes terminal text and summaries.
- `GameStageManager` loads stage banks, selects questions, checks answers, tracks progression, and coordinates stage transitions. `QuestionBank` reads YAML using SnakeYAML.
- `GameSaveData`, `GameSaveStore`, and `GameSaveRepository` represent and persist save-slot state.
- `AudioManager` plays music and sound effects. `MusicTrack` and `SoundEffect` map enum entries to classpath resources and volumes.

The repository README documents the project. The separate `fxml_files/tutorial-readme-md.fxml` and its tutorial pages provide the guide shown inside the game.

## Add or edit questions

Each active bank is a YAML list in `src/main/resources/yaml_files/`:

```yaml
- id: "q-00144"
  prompt: |
    Put the question text here.
  choices:
    - "First choice"
    - "Second choice"
    - "Third choice"
    - "Fourth choice"
  correctChoiceIndx: 0
```

Use a unique ID in the `q-#####` format within the bank. Provide four choices, and set `correctChoiceIndx` to the zero-based index of the correct choice in the YAML list. The runtime shuffles question order and displayed choices. Keep the key spelling `correctChoiceIndx` as shown; it is the field expected by the loader.

Stage bank classes (`Stage0Questions` through `Stage7Questions`) map stage numbers to YAML paths. `GameStageManager` registers those classes and currently limits each stage to 10 selected questions.

## Extend the application

- **Add a screen:** add its FXML path, title, dimensions, and instance policy in `ui/Screen.java`; create the FXML layout and controller, then open it through `SceneManager`.
- **Add a terminal command:** route it in `view/TerminalController.java`; keep question and progression behavior in `model/GameStageManager.java`. Update the terminal help text in `util/StringsConfig.java` and the in-game tutorial pages when command behavior changes.
- **Add a stage:** create its question-bank class and YAML file, then update the stage sequence and bank mapping in `GameStageManager`. Add stage-specific cutscene and warden content where appropriate.
- **Add an asset:** place it under the matching resources folder and refer to it by classpath path, for example `/images/desktop_icons/command_line.png`. Register audio files in `MusicTrack` or `SoundEffect` so `AudioManager` can resolve them.

## Contribution conventions

Suggested branch prefixes:

- `feature/{feature-name}` for a feature
- `looks/{component-name}` for visual changes
- `fix/{bug-fix}` for a bug fix
- `docs/{documentation-edit}` for documentation
- `refactor/{code-cleanup}` for cleanup without behavior changes

The project instructions request a README update on every tenth commit or merge to `main` (for example, commit 20 or 30).
