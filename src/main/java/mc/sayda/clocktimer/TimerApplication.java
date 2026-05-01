package mc.sayda.clocktimer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimerApplication extends Application {

    private Robot robot;
    private Timeline timeline;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Scheduled Auto Action");

        // UI Elements
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm:ss");

        ToggleGroup actionGroup = new ToggleGroup();
        RadioButton keyRadio = new RadioButton("Keyboard Key");
        RadioButton mouseRadio = new RadioButton("Mouse Click");
        keyRadio.setToggleGroup(actionGroup);
        mouseRadio.setToggleGroup(actionGroup);
        keyRadio.setSelected(true);

        TextField keyField = new TextField();
        keyField.setPromptText("Key (e.g. A)");
        keyField.setPrefWidth(100);

        ComboBox<String> mouseCombo = new ComboBox<>();
        mouseCombo.getItems().addAll("Left Click", "Right Click", "Double Click");
        mouseCombo.setValue("Left Click");
        mouseCombo.setDisable(true);

        // Logic to ungray/gray boxes
        actionGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            keyField.setDisable(newV == mouseRadio);
            mouseCombo.setDisable(newV == keyRadio);
        });

        Button startButton = new Button("Start Timer");
        statusLabel = new Label("Status: Ready");

        startButton.setOnAction(e -> {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.stop();
                startButton.setText("Start Timer");
                statusLabel.setText("Status: Stopped");
                return;
            }

            try {
                String input = timeField.getText().trim();

                // 1. Add a leading zero if the hour is a single digit (e.g., "3:21" -> "03:21")
                // If the first colon is at index 1, it means there is only one number before it.
                if (input.indexOf(":") == 1) {
                    input = "0" + input;
                }

                // 2. Add seconds if they are missing (e.g., "03:21" -> "03:21:00")
                if (input.matches("^\\d{2}:\\d{2}$")) {
                    input += ":00";
                }

                // Parse to LocalTime
                LocalTime targetTime = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm:ss"));

                statusLabel.setText("Scheduled for: " + input);
                startButton.setText("Cancel");

                timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    if (LocalTime.now().withNano(0).equals(targetTime)) {
                        executeAction(keyRadio.isSelected(), keyField.getText(), mouseCombo.getValue());
                        timeline.stop();
                        startButton.setText("Start Timer");
                        statusLabel.setText("Action executed!");
                    }
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();

            } catch (DateTimeParseException ex) {
                statusLabel.setText("Error: Use HH:mm or HH:mm:ss");
            }
        });

        VBox root = new VBox(15, new Label("Set Time:"), timeField, keyRadio, keyField, mouseRadio, mouseCombo, startButton, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);

        primaryStage.setScene(new Scene(root, 300, 400));
        primaryStage.show();
    }

    private void executeAction(boolean isKey, String key, String mouseType) {
        if (isKey && !key.isEmpty()) {
            int code = KeyEvent.getExtendedKeyCodeForChar(key.toUpperCase().charAt(0));
            robot.keyPress(code);
            robot.keyRelease(code);
        } else {
            int mask = mouseType.contains("Right") ? InputEvent.BUTTON3_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK;
            robot.mousePress(mask);
            robot.mouseRelease(mask);
            if (mouseType.equals("Double Click")) {
                robot.delay(50);
                robot.mousePress(mask);
                robot.mouseRelease(mask);
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}