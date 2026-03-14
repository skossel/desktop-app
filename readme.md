# README – Population Simulation Prototype

## Overview

Competitor: *Developer*
Technology: *Java 17, JavaFX 17, Gson 2.10.1*
IDE: *IntelliJ IDEA*
Database: *NONE*

# Installation

1. Ensure Java 17 or newer is installed on the system. You can verify with `java -version`.
2. No additional installation is required. The application is packaged as a self-contained JAR file with all dependencies included.

# Execution

1. Navigate to the `executable` directory found within the delivered archive.
2. Run `run.bat` or alternatively execute `java -jar PopSim.jar` from the command line.
3. The application window will open.

# Usage

1. **Loading a world:** Click "📂 Open" in the toolbar and select a JSON world file (e.g., `world.small.json` or `world.big.json`).
2. **Map display:** The map shows all locations (with icons and names) and all citizens at their current positions. The map maintains a 1:1 aspect ratio and scales with the window.
3. **Simulation controls:**
   - Click "⏭ Step" to advance the simulation by one minute.
   - Click "▶ Play" to run the simulation continuously (~60 steps/sec). Click "⏸ Pause" to stop.
   - The current simulation date and time is displayed in the toolbar.
4. **Citizen editing:** Select a citizen from the list to load them into the editor. Edit icon, name, home location, and save changes.
5. **Schedule editing:** View and edit the daily schedule of the selected citizen. Add, edit, or remove events. Travel time, spend time, and total time are calculated automatically. Conflicting events are highlighted in red.
6. **Location editing:** Select a location from the list. Edit icon, name, and coordinates (0-10000). Location names must be unique.
7. **Saving:** Click "💾 Save" to export the current world state as a JSON file that can be reloaded.

# Comments

- When loading a world state, all citizens start at their home location (as per specification), regardless of the current schedule.
- Simulation time resets to today at 00:00 when a new world is loaded.
- Editing is disabled while the simulation is running (playing). Pause the simulation to edit.
- The application handles both the small and big world files correctly.
- Schedules repeat daily. Events automatically reorder by time when modified.
