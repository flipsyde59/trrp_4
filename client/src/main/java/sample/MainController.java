package sample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Request;
import entities.Response;
import entities.Task;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jdk.nashorn.internal.parser.JSONParser;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static sample.Main.*;

public class MainController {

    @FXML
    private ContextMenu cm_sort;

    @FXML
    private MenuItem mi_unfinishTask;

    @FXML
    private MenuItem mi_finishTask;

    @FXML
    private MenuItem mi_allTask;

    //листы, добавление
    @FXML
    public ListView<Task> lv_tasks;

    @FXML
    private Button bt_addTask;

    // О задаче
    @FXML
    private TextArea ta_nameTask;

    @FXML
    private TextArea ta_descrTask;

    @FXML
    private RadioButton rbt_completeTask;

    @FXML
    private Button bt_updateTask;

    @FXML
    private Button bt_deleteTask;

    @FXML
    private Button bt_clear;

    Stage primStage;
    int idUser;

    static ObservableList<Task> tasksObservableList = FXCollections.observableArrayList();
    static List<Task> tasksList = new ArrayList<>();
    static List<Task> tasksFromServer = new ArrayList<>();
    static Task selectedTask = null;
    QueueJMS clientQueue;
    String textMsg;

    MainController (Stage primStage, int idUser){
        this.primStage = primStage;
        this.idUser = idUser;
    }

    @FXML
    void initialize() {

        try{

            Gson gson = new Gson();
            clientQueue = new QueueJMS();
            clientQueue.createConnect();

            textMsg = adaptResponse();
            System.out.println(textMsg);
            if (!textMsg.equals("pusto")) {
                Type dataType = new TypeToken<ArrayList<Task>>() {
                }.getType();
                tasksList = gson.fromJson(textMsg, dataType);
            } else { tasksList = new ArrayList<>();}
            tasksList.stream().forEach(System.out::println);
            tasksObservableList.addAll(tasksList);
            lv_tasks.setItems(tasksObservableList);

            // получаем модель выбора элементов
            MultipleSelectionModel<Task> tasksSelectionModel = lv_tasks.getSelectionModel();
            // устанавливаем слушатель для отслеживания изменений
            tasksSelectionModel.selectedItemProperty().addListener(new ChangeListener<Task>() {
                public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
                    Optional<Task> newTask = Optional.ofNullable(newValue);
                    newTask.ifPresent(x-> {
                        selectedTask = x;

                        ta_nameTask.setText(x.getName());
                        ta_descrTask.setText(x.getDescription());
                        rbt_completeTask.setSelected(x.isDone());
                    });
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
        //Событие при нажатии кнопки ДОБАВИТЬ
        bt_addTask.setOnAction(e->{
            if (ta_nameTask.getText().isEmpty()){
                informDialog("Введите название задачи!");
            }
            else{
                Task newTask = new Task(ta_nameTask.getText(), ta_descrTask.getText(), false);
                //Формирую запрос
                Request req = new Request(idUser, "create", newTask);
                Gson gson = new Gson();
                //Отправить на сервер
                try {
                    Message message = clientQueue.getSession().createTextMessage(gson.toJson(req));
                    clientQueue.getProducer().send(message);
                    Thread.sleep(3000);
                    //Получить ответ id
                    String r = adaptResponse();
                    try{
                        //Если положительный ответ
                        int id = Integer.parseInt(r);
                        System.out.println(id);
                        newTask.setId(id);
                        tasksList.add(newTask);
                        tasksObservableList.add(newTask);
                        lv_tasks.refresh();
                    } catch (Exception exep){
                        //Если отрицательный
                        errorDialog("Упс! Что-то пошло не так...");
                        exep.printStackTrace();
                    }

                } catch (JMSException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        //Событие при нажатии кнопки ИЗМЕНИТЬ
        bt_updateTask.setOnAction(e->{
            if (selectedTask != null) {
                selectedTask.setName(ta_nameTask.getText());
                selectedTask.setDescription(ta_descrTask.getText());
                //Отправить на сервер задачу
                //Формирую запрос
                Request req = new Request(idUser, "update", selectedTask);
                Gson gson = new Gson();
                //Отправить на сервер
                try {
                    Message message = clientQueue.getSession().createTextMessage(gson.toJson(req));
                    clientQueue.getProducer().send(message);
                    Thread.sleep(3000);
                    //Получить ответ
                    String r = adaptResponse();
                        if (r != null)
                            //Если положительный ответ
                            lv_tasks.refresh();
                        else
                            errorDialog("Упс! Что-то пошло не так при изменении");

                } catch (JMSException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else { errorDialog("Выберите задачу!");}
        });

        //Событие при нажатии кнопки УДАЛИТЬ
        bt_deleteTask.setOnAction(e->{
            if (selectedTask != null) {
                //Отправить на сервер задачу
                Request req = new Request(idUser, "delete", selectedTask);
                Gson gson = new Gson();
                //Отправить на сервер
                try {
                    Message message = clientQueue.getSession().createTextMessage(gson.toJson(req));
                    clientQueue.getProducer().send(message);
                    Thread.sleep(3000);
                    //Получить ответ
                    String r = adaptResponse();

                        if (r!=null) {
                            //Если положительный ответ
                            tasksList.remove(selectedTask);
                            tasksObservableList.remove(selectedTask);
                            selectedTask = null;
                            clearTaskField();
                        } else
                            errorDialog("Упс! Что-то пошло не так при изменении");


                } catch (JMSException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else { errorDialog("Выберите задачу!");}
        });

        bt_clear.setOnAction(e->{
            clearTaskField();
        });

        rbt_completeTask.setOnAction(e->{
            if(selectedTask != null) {
                selectedTask.setDone(rbt_completeTask.isSelected());
                //Отправить на сервер задачу
                //Формирую запрос
                Request req = new Request(idUser, "update", selectedTask);
                Gson gson = new Gson();
                //Отправить на сервер
                try {
                    Message message = clientQueue.getSession().createTextMessage(gson.toJson(req));
                    clientQueue.getProducer().send(message);
                    Thread.sleep(3000);
                    //Получить ответ
                    String r = adaptResponse();
                    try {
                        if (Integer.parseInt(r) == 1)
                            //Если положительный ответ
                            lv_tasks.refresh();
                        else
                            errorDialog("Упс! Что-то пошло не так при изменении");
                    } catch (Exception exep) {
                        //Если отрицательный
                        errorDialog("Упс! Что-то пошло не так при изменении (ParseInt)");
                    }

                } catch (JMSException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {errorDialog("Выберите задачу!");}
        });

        primStage.setOnCloseRequest(e->{System.exit(0);});
    }



    public static void informDialog(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Внимание!");
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public static void errorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }

    public void clearTaskField(){
        ta_nameTask.clear();
        ta_descrTask.clear();
        rbt_completeTask.setSelected(false);
    }

    public String adaptResponse() throws JMSException {
        Gson gson = new Gson();
        Response resp = new Response();
        Message receivedMessage;
        clientQueue.getConnection().start();
        do {
            System.out.println("START TO RECEIVE");
            receivedMessage = clientQueue.getConsumer().receive();
            String msg = ((TextMessage) receivedMessage).getText();
            System.out.println("[/] RECEIVED: " + msg);
            try {
                resp = gson.fromJson(msg, Response.class);
                if (!resp.getFrom().equals("server"))
                    resp = null;
            } catch (Exception e){
                resp = null;
            }
        } while (resp == null || resp.getId_client() != idUser);
        if (receivedMessage != null) {
            System.out.println("[/] CONFIRMED: " + ((TextMessage) receivedMessage).getText());
            receivedMessage.acknowledge();
            //System.out.println("Acknowledged: " + message.getJMSMessageID());
        }
        return resp.getMessage();
    }

    public class threadQueue implements Runnable {
        @Override
        public void run() {
            try {
                clientQueue = new QueueJMS();

                clientQueue.createConnect();

                textMsg = adaptResponse();


            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}
