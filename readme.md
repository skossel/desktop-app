# README – Population Simulation Prototype

## Overview

Competitor: *Sandro Kossel*
Technology: *Java 17, JavaFX 17, Gson 2.10.1*
IDE: *IntelliJ IDEA 2025.3*
Database: *NONE*

# Installation

No installation is required. The application is delivered as a self-contained JAR file with all dependencies included.

**Prerequisite:** Java 17 (or newer) must be installed on the system. You can verify this by opening a terminal and running:

```
java -version
```

If Java is not installed, download and install it from [https://adoptium.net/](https://adoptium.net/) (Temurin JDK 17+).

# Execution

1. Navigate to the `executable` directory found within the delivered archive.
2. Double-click `run.bat` to start the application.
3. Alternatively, open a terminal in the `executable` directory and run:
   ```
   java -jar PopSim.jar
   ```
4. The main application window will open and is ready for use.

# Comments

## How to Use the Application

1. **Load a world state:** Click the **📂 Open** button in the toolbar and select a JSON world file (e.g., `world.small.json` or `world.big.json` from the `instructions` folder).
2. **View the map:** The map displays all locations (with icons and names) and all citizens at their current positions. It maintains a 1:1 aspect ratio and scales with the window.
3. **Run the simulation:**
   - Click **⏭ Step** to advance the simulation by exactly one minute.
   - Click **▶ Play** to run the simulation continuously at approximately 60 steps per second.
   - Click **⏸ Pause** to stop the simulation.
   - The current simulation date and time is displayed in the toolbar.
4. **Edit citizens:** Select a citizen from the list on the right side to load them into the editor. You can change the icon, first name, last name, and home location. Click **Save** to apply changes.
5. **Edit schedules:** The schedule of the selected citizen is shown below the citizen editor. You can add, edit, or remove schedule events. Travel time, spend time, and total time are calculated automatically. Conflicting events are highlighted in red.
6. **Edit locations:** Select a location from the location list. You can change the icon, name, and coordinates (valid range: 0–10000). Location names must be unique. Click **Save** to apply.
7. **Save the world state:** Click **💾 Save** in the toolbar to export the current world state as a JSON file. This file can be loaded again at any time.

## Assumptions and Design Decisions

- When a world state is loaded, all citizens start at their **home location** regardless of the current schedule (as specified in the assignment).
- The simulation time resets to **today at 00:00** each time a new world is loaded.
- Editing citizens, locations, and schedules is **disabled while the simulation is playing**. Pause the simulation first to make edits.
- Schedules repeat daily. Schedule events are automatically reordered by start time when modified.
- Movement between locations follows a **straight line** at exactly **5 km/h**. No pathfinding or roads are used.
- If a citizen reaches their destination during a simulation step, that step still counts as travelling. Spending time begins on the following step.

## Known Limitations

- The application requires Java 17 or newer to run. It has been developed and tested with Java 17.0.8.
- No database is used. All data persistence is handled via JSON files.

## Project Structure

```
skill09-2026-sandro-kossel/
├── source/           Source code (Java/Gradle project)
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       ├── main/java/ch/popsim/   Application source files
│       └── test/java/ch/popsim/   Unit tests
├── executable/       Runnable application
│   ├── PopSim.jar    Self-contained JAR with all dependencies
│   └── run.bat       Startup script for Windows
├── database/         Database files (not used in this project)
├── competitor.json   Competitor information
└── readme.md         This file
```

# Feedback (optional)

The assignment was well-structured and the provided wireframes and JSON test data were very helpful for development and testing.
