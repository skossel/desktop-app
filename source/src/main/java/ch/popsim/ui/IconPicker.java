package ch.popsim.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import java.util.Optional;

public class IconPicker {

    public static Optional<String> showCitizenIcons(String current) {
        String[] icons = {"👦","👧","👨","👩","👴","👵"};
        return showPicker("Select Citizen Icon", icons, current);
    }

    public static Optional<String> showLocationIcons(String current) {
        // Include 🏠 and 🏢 (from JSON data) plus all from location-symbols.txt
        String[] icons = {"🏠","🏢","🏥","🏫","🏋️","🎬","🛒","☕","📚","🚓","🚒","⛪","🏨","🏭","🛎"};
        return showPicker("Select Location Icon", icons, current);
    }

    private static Optional<String> showPicker(String title, String[] icons, String current) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Choose an icon:");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        FlowPane flow = new FlowPane(8, 8);
        flow.setPadding(new Insets(10));
        flow.setPrefWrapLength(350);
        final String[] result = {null};

        for (String icon : icons) {
            Button btn = new Button(icon);
            btn.setStyle("-fx-font-size: 24px; -fx-min-width: 48px; -fx-min-height: 48px;");
            if (icon.equals(current)) {
                btn.setStyle(btn.getStyle() + "-fx-border-color: blue; -fx-border-width: 2;");
            }
            btn.setOnAction(e -> {
                result[0] = icon;
                dialog.setResult(icon);
                dialog.close();
            });
            flow.getChildren().add(btn);
        }

        dialog.getDialogPane().setContent(flow);
        dialog.showAndWait();
        return Optional.ofNullable(result[0]);
    }
}
