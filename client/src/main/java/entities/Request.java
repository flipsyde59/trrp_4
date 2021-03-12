package entities;

public class Request {
    String from = "client";
    int id_client;
    String action;
    Task message;

    public Request(int id_client, String action, Task message) {
        this.id_client = id_client;
        this.action = action;
        this.message = message;
    }

    public void setId_client(int id_client) {
        this.id_client = id_client;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setMessage(Task message) {
        this.message = message;
    }


}
