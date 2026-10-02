# ParadigmBreach

ParadigmBreach is a JavaFX desktop-game prototype. It opens with an animated splash screen, then presents a desktop where players can launch a terminal and read the in-game guide.

## Features

- Animated splash screen
- Desktop-style launcher
- Terminal window with `help`, `clear`, and `exit` commands
- In-game `HOW_TO_PLAY` tutorial window

## Requirements

- Java 25
- Maven (or the included Maven Wrapper)

## Run

From the project folder, run:

(Case 1, first time running in a coding session, or added/deleted some files)
```powershell
.\mvnw.cmd clean javafx:run
```

(Case 2, normal running, doesn't delete already compiled files or restart the target folder)
```powershell
.\mvnw.cmd javafx:run
```
Note if trying case 2 fails compiling, try to compile first with case 1.

The application starts at the splash screen. Press a key or click to continue, then double-click a desktop icon to open it.

## Project layout

- `src/main/java` - Java source code
- `src/main/resources/fxml_files` - JavaFX layouts
- `src/main/resources/css_files` - interface styles
- `src/main/resources/images` - game artwork and desktop icons

## On branch names with git

When adding a feature:
- feature/{feature-name}

When fixing a bug:
- fix/{bug-fix}

When adding/editing documentation:
- docs/{documentation-edit}

When cleaning up code (no code behaviour change):
- refactor/{code-cleanup}