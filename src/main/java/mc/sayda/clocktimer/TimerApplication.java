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
                return;
            }

            try {
                String input = timeField.getText().trim();

                // If the user typed "12:22", turn it into "12:22:00"
                if (input.length() == 5 && input.contains(":")) {
                    input += ":00";
                }

                // Standardize the format for the parser
                LocalTime targetTime = LocalTime.parse(input, DateTimeFormatter.ofPattern("HH:mm:ss"));

                statusLabel.setText("Waiting for " + input);
                startButton.setText("Stop");

                timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    // Check if current time (ignoring nanoseconds) matches target
                    if (LocalTime.now().withNano(0).equals(targetTime)) {
                        executeAction(keyRadio.isSelected(), keyField.getText(), mouseCombo.getValue());
                        timeline.stop();
                        startButton.setText("Start Timer");
                        statusLabel.setText("Done!");
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