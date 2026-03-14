package ch.popsim;

import ch.popsim.io.WorldStateIO;
import ch.popsim.model.*;
import ch.popsim.sim.SimulationEngine;
import ch.popsim.ui.IconPicker;
import ch.popsim.ui.MapCanvas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PopSimApp extends Application {

    private SimulationEngine engine = new SimulationEngine();
    private MapCanvas mapCanvas;
    private boolean simRunning = false;
    private AnimationTimer simTimer;

    // UI components
    private Label timeLabel;
    private Button stepBtn, playBtn;
    private ListView<Citizen> citizenList;
    private ListView<Location> locationList;
    private Label statusLabel;

    // Citizen editor fields
    private Button citizenIconBtn;
    private TextField firstnameField, lastnameField;
    private ComboBox<Location> homeCombo;
    private Button saveCitizenBtn;

    // Schedule table
    private TableView<ScheduleEvent> scheduleTable;
    private Citizen selectedCitizen;
    private Location selectedLocation;

    // Location editor fields
    private Button locationIconBtn;
    private TextField locNameField, locXField, locYField;
    private Button saveLocationBtn;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Population Simulation Prototype");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setTop(createToolbar());
        root.setCenter(createMainContent());

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            confirmExit();
        });
        stage.show();

        setupSimTimer();
    }

    private ToolBar createToolbar() {
        Button openBtn = new Button("📂 Open");
        openBtn.setOnAction(e -> loadWorld());
        Button saveBtn = new Button("💾 Save");
        saveBtn.setOnAction(e -> saveWorld());
        Button exitBtn = new Button("🚪 Exit");
        exitBtn.setOnAction(e -> confirmExit());

        Separator sep1 = new Separator();

        timeLabel = new Label("--/--/---- --:--");
        timeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        stepBtn = new Button("⏭ Step");
        stepBtn.setOnAction(e -> doStep());

        playBtn = new Button("▶ Play");
        playBtn.setOnAction(e -> togglePlay());

        ToolBar toolbar = new ToolBar(openBtn, saveBtn, sep1, timeLabel,
                new Separator(), stepBtn, playBtn, new Separator(), exitBtn);
        return toolbar;
    }

    private SplitPane createMainContent() {
        // Left: Map
        mapCanvas = new MapCanvas();
        StackPane mapContainer = new StackPane(mapCanvas);
        mapContainer.setStyle("-fx-background-color: #e0e0e0;");
        mapCanvas.widthProperty().bind(mapContainer.widthProperty());
        mapCanvas.heightProperty().bind(mapContainer.heightProperty());

        // Right: Lists and editors
        VBox rightPanel = new VBox(5);
        rightPanel.setPadding(new Insets(5));
        rightPanel.setMinWidth(380);

        // Citizen list
        TitledPane citizenPane = new TitledPane("Citizens", createCitizenListPanel());
        citizenPane.setCollapsible(false);
        citizenPane.setMaxHeight(200);

        // Citizen editor
        TitledPane editorPane = new TitledPane("Citizen Editor", createCitizenEditor());
        editorPane.setCollapsible(false);

        // Location list
        TitledPane locListPane = new TitledPane("Locations", createLocationListPanel());
        locListPane.setCollapsible(false);
        locListPane.setMaxHeight(180);

        // Location editor
        TitledPane locEditorPane = new TitledPane("Location Editor", createLocationEditor());
        locEditorPane.setCollapsible(false);

        ScrollPane scrollRight = new ScrollPane(new VBox(5, citizenPane, editorPane, locListPane, locEditorPane));
        scrollRight.setFitToWidth(true);

        SplitPane splitPane = new SplitPane(mapContainer, scrollRight);
        splitPane.setDividerPositions(0.6);
        return splitPane;
    }

    private VBox createCitizenListPanel() {
        citizenList = new ListView<>();
        citizenList.setPrefHeight(150);
        citizenList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedCitizen = val;
            loadCitizenToEditor(val);
            mapCanvas.setSelectedCitizen(val);
        });
        return new VBox(citizenList);
    }

    private VBox createCitizenEditor() {
        citizenIconBtn = new Button("👨");
        citizenIconBtn.setStyle("-fx-font-size: 20px;");
        citizenIconBtn.setOnAction(e -> {
            IconPicker.showCitizenIcons(citizenIconBtn.getText()).ifPresent(citizenIconBtn::setText);
        });

        firstnameField = new TextField();
        firstnameField.setPromptText("First name");
        lastnameField = new TextField();
        lastnameField.setPromptText("Last name");

        homeCombo = new ComboBox<>();
        homeCombo.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label("Status: -");
        statusLabel.setStyle("-fx-font-style: italic;");

        saveCitizenBtn = new Button("💾 Save Citizen");
        saveCitizenBtn.setOnAction(e -> saveCitizen());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(5);
        grid.add(new Label("Icon:"), 0, 0);
        grid.add(citizenIconBtn, 1, 0);
        grid.add(new Label("First name:"), 0, 1);
        grid.add(firstnameField, 1, 1);
        grid.add(new Label("Last name:"), 0, 2);
        grid.add(lastnameField, 1, 2);
        grid.add(new Label("Home:"), 0, 3);
        grid.add(homeCombo, 1, 3);
        grid.add(statusLabel, 0, 4, 2, 1);
        grid.add(saveCitizenBtn, 1, 5);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.NEVER);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(cc, cc2);

        // Schedule editor
        TitledPane schedPane = new TitledPane("Schedule", createScheduleEditor());
        schedPane.setCollapsible(false);

        return new VBox(5, grid, schedPane);
    }

    @SuppressWarnings("unchecked")
    private VBox createScheduleEditor() {
        scheduleTable = new TableView<>();
        scheduleTable.setPrefHeight(150);
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ScheduleEvent, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cd -> {
            LocalTime t = cd.getValue().getTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t.format(DateTimeFormatter.ofPattern("HH:mm")));
        });
        timeCol.setPrefWidth(55);

        TableColumn<ScheduleEvent, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(cd -> {
            Location loc = cd.getValue().getTargetLocation();
            return new javafx.beans.property.SimpleStringProperty(
                    loc != null ? loc.toString() : "?");
        });
        locCol.setPrefWidth(100);

        TableColumn<ScheduleEvent, String> actCol = new TableColumn<>("Activity");
        actCol.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getActivity()));
        actCol.setPrefWidth(80);

        TableColumn<ScheduleEvent, String> travelCol = new TableColumn<>("Travel");
        travelCol.setCellValueFactory(cd -> {
            ScheduleEvent ev = cd.getValue();
            int travel = computeTravelTime(ev);
            return new javafx.beans.property.SimpleStringProperty(ScheduleEvent.formatDuration(travel));
        });
        travelCol.setPrefWidth(55);

        TableColumn<ScheduleEvent, String> spendCol = new TableColumn<>("Spend");
        spendCol.setCellValueFactory(cd -> {
            ScheduleEvent ev = cd.getValue();
            int spend = computeSpendTime(ev);
            return new javafx.beans.property.SimpleStringProperty(ScheduleEvent.formatDuration(spend));
        });
        spendCol.setPrefWidth(55);

        TableColumn<ScheduleEvent, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cd -> {
            ScheduleEvent ev = cd.getValue();
            int total = computeTotalTime(ev);
            return new javafx.beans.property.SimpleStringProperty(ScheduleEvent.formatDuration(total));
        });
        totalCol.setPrefWidth(55);

        scheduleTable.getColumns().addAll(timeCol, locCol, actCol, travelCol, spendCol, totalCol);

        // Highlight conflicts
        scheduleTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ScheduleEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isConflicting(item)) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        Button addBtn = new Button("➕ Add");
        addBtn.setOnAction(e -> addScheduleEvent());
        Button editBtn = new Button("✏ Edit");
        editBtn.setOnAction(e -> editScheduleEvent());
        Button removeBtn = new Button("🗑 Remove");
        removeBtn.setOnAction(e -> removeScheduleEvent());

        HBox btnBar = new HBox(5, addBtn, editBtn, removeBtn);
        return new VBox(5, scheduleTable, btnBar);
    }

    private VBox createLocationListPanel() {
        locationList = new ListView<>();
        locationList.setPrefHeight(130);
        locationList.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selectedLocation = val;
            loadLocationToEditor(val);
            mapCanvas.setSelectedLocation(val);
        });
        return new VBox(locationList);
    }

    private VBox createLocationEditor() {
        locationIconBtn = new Button("🏠");
        locationIconBtn.setStyle("-fx-font-size: 20px;");
        locationIconBtn.setOnAction(e -> {
            IconPicker.showLocationIcons(locationIconBtn.getText()).ifPresent(locationIconBtn::setText);
        });

        locNameField = new TextField();
        locNameField.setPromptText("Location name");
        locXField = new TextField();
        locXField.setPromptText("X (0-10000)");
        locYField = new TextField();
        locYField.setPromptText("Y (0-10000)");

        saveLocationBtn = new Button("💾 Save Location");
        saveLocationBtn.setOnAction(e -> saveLocation());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(5);
        grid.add(new Label("Icon:"), 0, 0);
        grid.add(locationIconBtn, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(locNameField, 1, 1);
        grid.add(new Label("X:"), 0, 2);
        grid.add(locXField, 1, 2);
        grid.add(new Label("Y:"), 0, 3);
        grid.add(locYField, 1, 3);
        grid.add(saveLocationBtn, 1, 4);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.NEVER);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(cc, cc2);

        return new VBox(5, grid);
    }

    // --- Actions ---

    private void loadWorld() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open World State");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fc.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                WorldState world = WorldStateIO.load(file);
                engine.setWorld(world);
                refreshAll();
            } catch (Exception ex) {
                showError("Failed to load world: " + ex.getMessage());
            }
        }
    }

    private void saveWorld() {
        if (engine.getWorld() == null) {
            showError("No world loaded.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save World State");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fc.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                WorldStateIO.save(engine.getWorld(), file);
            } catch (Exception ex) {
                showError("Failed to save: " + ex.getMessage());
            }
        }
    }

    private void confirmExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Exit");
        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) Platform.exit();
        });
    }

    private void doStep() {
        engine.step();
        updateTimeLabel();
        updateStatusLabel();
        mapCanvas.draw();
        scheduleTable.refresh();
    }

    private void togglePlay() {
        simRunning = !simRunning;
        playBtn.setText(simRunning ? "⏸ Pause" : "▶ Play");
        stepBtn.setDisable(simRunning);
        setEditingDisabled(simRunning);
    }

    private void setEditingDisabled(boolean disabled) {
        citizenIconBtn.setDisable(disabled);
        firstnameField.setDisable(disabled);
        lastnameField.setDisable(disabled);
        homeCombo.setDisable(disabled);
        saveCitizenBtn.setDisable(disabled);
        locationIconBtn.setDisable(disabled);
        locNameField.setDisable(disabled);
        locXField.setDisable(disabled);
        locYField.setDisable(disabled);
        saveLocationBtn.setDisable(disabled);
    }

    private void setupSimTimer() {
        final long[] lastStep = {0};
        simTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!simRunning || engine.getWorld() == null) return;
                // Target: 60 steps/sec = ~16.67ms per step
                if (now - lastStep[0] >= 16_666_666L) {
                    lastStep[0] = now;
                    engine.step();
                    updateTimeLabel();
                    updateStatusLabel();
                    mapCanvas.draw();
                }
            }
        };
        simTimer.start();
    }

    private void refreshAll() {
        WorldState w = engine.getWorld();
        if (w == null) return;
        citizenList.setItems(FXCollections.observableArrayList(w.getCitizens()));
        locationList.setItems(FXCollections.observableArrayList(w.getLocations()));
        homeCombo.setItems(FXCollections.observableArrayList(w.getLocations()));
        mapCanvas.setWorld(w);
        updateTimeLabel();
        selectedCitizen = null;
        selectedLocation = null;
    }

    private void updateTimeLabel() {
        if (engine.getCurrentTime() != null) {
            timeLabel.setText(engine.getCurrentTime().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        }
    }

    private void updateStatusLabel() {
        if (selectedCitizen != null) {
            statusLabel.setText("Status: " + selectedCitizen.getStatusText());
        }
    }

    private void loadCitizenToEditor(Citizen c) {
        if (c == null) return;
        citizenIconBtn.setText(c.getIcon());
        firstnameField.setText(c.getFirstname());
        lastnameField.setText(c.getLastname());
        if (engine.getWorld() != null) {
            homeCombo.setItems(FXCollections.observableArrayList(engine.getWorld().getLocations()));
        }
        homeCombo.setValue(c.getHome());
        statusLabel.setText("Status: " + c.getStatusText());
        scheduleTable.setItems(FXCollections.observableArrayList(c.getSchedule()));
    }

    private void saveCitizen() {
        if (selectedCitizen == null) return;
        String fn = firstnameField.getText().trim();
        String ln = lastnameField.getText().trim();
        if (fn.isEmpty() || ln.isEmpty()) {
            showError("First name and last name are required.");
            return;
        }
        selectedCitizen.setIcon(citizenIconBtn.getText());
        selectedCitizen.setFirstname(fn);
        selectedCitizen.setLastname(ln);
        selectedCitizen.setHome(homeCombo.getValue());
        citizenList.refresh();
        mapCanvas.draw();
    }

    private void loadLocationToEditor(Location loc) {
        if (loc == null) return;
        locationIconBtn.setText(loc.getIcon());
        locNameField.setText(loc.getName());
        locXField.setText(String.valueOf((int) loc.getCoordinate().getX()));
        locYField.setText(String.valueOf((int) loc.getCoordinate().getY()));
    }

    private void saveLocation() {
        if (selectedLocation == null) return;
        String name = locNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Location name is required.");
            return;
        }
        if (!engine.getWorld().isLocationNameUnique(name, selectedLocation)) {
            showError("Location name must be unique.");
            return;
        }
        double x, y;
        try {
            x = Double.parseDouble(locXField.getText().trim());
            y = Double.parseDouble(locYField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Coordinates must be numbers.");
            return;
        }
        if (x < 0 || x > 10000 || y < 0 || y > 10000) {
            showError("Coordinates must be between 0 and 10000.");
            return;
        }
        selectedLocation.setIcon(locationIconBtn.getText());
        selectedLocation.setName(name);
        selectedLocation.getCoordinate().setX(x);
        selectedLocation.getCoordinate().setY(y);
        locationList.refresh();
        citizenList.refresh();
        mapCanvas.draw();
    }

    // --- Schedule event helpers ---

    private int computeTravelTime(ScheduleEvent ev) {
        if (selectedCitizen == null) return 0;
        List<ScheduleEvent> sched = selectedCitizen.getSchedule();
        int idx = sched.indexOf(ev);
        Location departure = getDepartureForEvent(idx);
        return ev.calculateTravelTime(departure);
    }

    private int computeSpendTime(ScheduleEvent ev) {
        if (selectedCitizen == null) return 0;
        List<ScheduleEvent> sched = selectedCitizen.getSchedule();
        int idx = sched.indexOf(ev);
        int nextIdx = (idx + 1) % sched.size();
        Location departure = getDepartureForEvent(idx);
        return ev.calculateSpendTime(sched.get(nextIdx).getTimeInMinutes(), departure);
    }

    private int computeTotalTime(ScheduleEvent ev) {
        if (selectedCitizen == null) return 0;
        List<ScheduleEvent> sched = selectedCitizen.getSchedule();
        int idx = sched.indexOf(ev);
        int nextIdx = (idx + 1) % sched.size();
        return ev.calculateTotalTime(sched.get(nextIdx).getTimeInMinutes());
    }

    private Location getDepartureForEvent(int eventIndex) {
        if (selectedCitizen == null) return null;
        List<ScheduleEvent> sched = selectedCitizen.getSchedule();
        if (eventIndex == 0) {
            // First event: depart from last event's target (wrap) or home
            if (sched.size() > 1) {
                return sched.get(sched.size() - 1).getTargetLocation();
            }
            return selectedCitizen.getHome();
        }
        return sched.get(eventIndex - 1).getTargetLocation();
    }

    private boolean isConflicting(ScheduleEvent ev) {
        if (selectedCitizen == null) return false;
        int travel = computeTravelTime(ev);
        int total = computeTotalTime(ev);
        return travel > total;
    }

    private void addScheduleEvent() {
        if (selectedCitizen == null || engine.getWorld() == null) return;
        ScheduleEvent newEv = showScheduleEventDialog(null);
        if (newEv != null) {
            selectedCitizen.getSchedule().add(newEv);
            selectedCitizen.sortSchedule();
            scheduleTable.setItems(FXCollections.observableArrayList(selectedCitizen.getSchedule()));
        }
    }

    private void editScheduleEvent() {
        ScheduleEvent sel = scheduleTable.getSelectionModel().getSelectedItem();
        if (sel == null || selectedCitizen == null) return;
        ScheduleEvent edited = showScheduleEventDialog(sel);
        if (edited != null) {
            sel.setTime(edited.getTime());
            sel.setTargetLocation(edited.getTargetLocation());
            sel.setActivity(edited.getActivity());
            selectedCitizen.sortSchedule();
            scheduleTable.setItems(FXCollections.observableArrayList(selectedCitizen.getSchedule()));
        }
    }

    private void removeScheduleEvent() {
        ScheduleEvent sel = scheduleTable.getSelectionModel().getSelectedItem();
        if (sel == null || selectedCitizen == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove this schedule event?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                selectedCitizen.getSchedule().remove(sel);
                scheduleTable.setItems(FXCollections.observableArrayList(selectedCitizen.getSchedule()));
            }
        });
    }

    private ScheduleEvent showScheduleEventDialog(ScheduleEvent existing) {
        Dialog<ScheduleEvent> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Schedule Event" : "Edit Schedule Event");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField timeField = new TextField(existing != null ?
                existing.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "08:00");
        timeField.setPromptText("HH:MM");

        ComboBox<Location> locCombo = new ComboBox<>();
        if (engine.getWorld() != null) {
            locCombo.setItems(FXCollections.observableArrayList(engine.getWorld().getLocations()));
        }
        if (existing != null) locCombo.setValue(existing.getTargetLocation());

        List<String> suggestions = Arrays.asList("Work", "Return Home", "Shopping", "Leisure",
                "Sport", "Dining", "School", "Church", "Sleep");
        ComboBox<String> actCombo = new ComboBox<>(FXCollections.observableArrayList(suggestions));
        actCombo.setEditable(true);
        if (existing != null) actCombo.setValue(existing.getActivity());

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(8);
        gp.add(new Label("Time (HH:MM):"), 0, 0);
        gp.add(timeField, 1, 0);
        gp.add(new Label("Location:"), 0, 1);
        gp.add(locCombo, 1, 1);
        gp.add(new Label("Activity:"), 0, 2);
        gp.add(actCombo, 1, 2);
        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    LocalTime time = LocalTime.parse(timeField.getText().trim(),
                            DateTimeFormatter.ofPattern("HH:mm"));
                    Location loc = locCombo.getValue();
                    String act = actCombo.getValue() != null ? actCombo.getValue() : "";
                    if (loc == null) {
                        showError("Please select a location.");
                        return null;
                    }
                    // Check reachability
                    return new ScheduleEvent(time, loc, act);
                } catch (Exception ex) {
                    showError("Invalid time format. Use HH:MM.");
                    return null;
                }
            }
            return null;
        });

        Optional<ScheduleEvent> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
