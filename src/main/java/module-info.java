module mc.sayda.clocktimer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens mc.sayda.clocktimer to javafx.fxml;
    exports mc.sayda.clocktimer;
}