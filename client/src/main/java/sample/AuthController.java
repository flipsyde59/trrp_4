package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.UserAction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

//закрыть сокет

public class AuthController {

    @FXML
    private TextField tf_login;

    @FXML
    private PasswordField pf_password;

    @FXML
    private Button bt_auth;

    @FXML
    private Button bt_reg;

    Stage primStage;

    private Socket socket;
    private String host;
    private int port;
    private DataOutputStream outStream;
    private DataInputStream inStream;

    public AuthController(Stage primStage){
        this.primStage = primStage;
    }

    @FXML
    void initialize() {

        try {
            FileInputStream fis;
            Properties property = new Properties();
            fis = new FileInputStream("client/src/main/resources/config.properties");
            property.load(fis);
            host = property.getProperty("host");
            port = Integer.parseInt(property.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        bt_auth.setOnAction(e->{
            String s;
            UserAction us = new UserAction("auth", tf_login.getText(), pf_password.getText());
            Gson gson = new Gson();
            String request = gson.toJson(us);
            connect();
            sendRequest(request);

            listener listen = new listener();
            listen.run();
                s = listen.getServerMsg();
                if (s != "") {
                    try {
                        int userId = Integer.parseInt(s);
                        primStage.close();
                        AnchorPane pane;
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"));
                            loader.setController(new MainController(primStage, userId));
                            pane = loader.load();

                            Scene scene = new Scene(pane);
                            primStage.setTitle("ToDoshki - Ваши задачи");
                            primStage.setScene(scene);
                            primStage.show();
                            outStream.close();
                            inStream.close();
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception exep) {
                        showDialog("Не найдена такая пара логина и пароля.");
                    }

                }
        });

        bt_reg.setOnAction(e->{
            String s;

                UserAction us = new UserAction("reg",tf_login.getText(), pf_password.getText());
                Gson gson = new Gson();
                String request = gson.toJson(us);
                connect();
                sendRequest(request);

                listener listen = new listener();
                listen.run();
                s = listen.getServerMsg();
                if (s != "") {
                    try {
                        System.out.println(s);
                        int userId = Integer.parseInt(s);
                        System.out.println("int: "+userId);
                        primStage.close();
                        AnchorPane pane;
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainWindow.fxml"));
                            loader.setController(new MainController(primStage, userId));
                            pane = loader.load();

                            Scene scene = new Scene(pane);
                            primStage.setTitle("ToDoshki - Ваши задачи");
                            primStage.setScene(scene);
                            primStage.show();
                            outStream.close();
                            inStream.close();
                            socket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception exep) {
                        showDialog("Пользователь с таким логином уже существует.");
                    }
                }
        });

    }

    private void sendRequest(String request){
        try {
            outStream.writeUTF(request);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect(){
        try {
            socket = new Socket(host, port);
            outStream = new DataOutputStream(socket.getOutputStream());
            inStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class listener implements Runnable {
        private String serverMsg;
        @Override
        public void run() {
            byte[] buf = new byte[2];
            boolean flag = true;
            while (flag) {
                try {
                    inStream.read(buf);
                    serverMsg = new String(buf, "UTF-8");
                    flag = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("[-] RESEIVED UserId: " + serverMsg);
        }

        public synchronized String getServerMsg() {
            return serverMsg;
        }
    }

    void showDialog(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
