module org.example.practical_exam {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.practical_exam to javafx.fxml;
    exports org.example.practical_exam;
}