package org.example.practical_exam;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ClientController implements Initializable {
    public TextArea textArea;
    public TextField txtMessage;
    public TextField txtUsername;
    public Button btnConnect;
    public Label lblStatus;

    Scanner scanner = new Scanner(System.in);
    DataOutputStream dataOutputStream ;
    DataInputStream dataInputStream;
    Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


    public void btnSendOnAction(ActionEvent actionEvent) {
        try {
            String message = txtMessage.getText();
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
            loadMessage(message,true);
            txtMessage.clear();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblStatus.setText("Disconnected");

    }

    private void loadMessage(String text, boolean isSend) {
        if (isSend) {
            textArea.appendText("Me: " + text + "\n");
        } else {
            textArea.appendText("Other: " + text + "\n");
        }
    }
    private void loadImage(byte[] imageBytes, boolean isSend) {
        Image image = new Image(new ByteArrayInputStream(imageBytes));
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);
        textArea.appendText("[Image Received]\n");

    }


    public void btnImageOnAction(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files ", "*.jpg", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        java.io.File file = fileChooser.showOpenDialog(null);
        System.out.println(file.getAbsolutePath());


        if (file != null) {
            try {

                byte[] imageBytes = Files.readAllBytes(file.toPath());
                dataOutputStream.writeUTF("IMAGE");
                dataOutputStream.writeInt(imageBytes.length);
                dataOutputStream.write(imageBytes);
               dataOutputStream.flush();
                loadImage(imageBytes,true);

            } catch (IOException e){
               throw new RuntimeException(e);
            }

        }
    }
    public void connectToServer(ActionEvent actionEvent) {
        String username = txtUsername.getText().trim();

        if (username.isEmpty()) {
            lblStatus.setText("Please enter your username.");
            return;
        }

        try {
            socket = new Socket("localhost", 5000);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());

            // send username to server
            dataOutputStream.writeUTF("USERNAME:" + username);
            dataOutputStream.flush();

            lblStatus.setText("Connected as " + username);

            // listen thread
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = dataInputStream.readUTF();

                        if (msg.equals("IMAGE")) {
                            int size = dataInputStream.readInt();
                            byte[] imageBytes = new byte[size];
                            dataInputStream.readFully(imageBytes);

                            Platform.runLater(() -> loadImage(imageBytes, false));
                        } else {
                            Platform.runLater(() -> loadMessage(msg, false));
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> lblStatus.setText("Disconnected"));
                }
            }).start();

        } catch (Exception e) {
            lblStatus.setText("Connection failed");
            e.printStackTrace();
        }
    }

    private void listenForUpdates() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
            }
        } catch (IOException e) {
            Platform.runLater(() -> lblStatus.setText(" Disconnected from server"));
        }
    }
}
