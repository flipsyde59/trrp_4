package entities;

public class Response {
    private String from;
    private int id_client;
    private String message;

    public String getFrom() {
        return from;
    }

    public int getId_client() {
        return id_client;
    }

    public String getMessage() {
        return message;
    }

    public Response() {
    }

    public Response(String from, int id_client, String message) {
        this.from = from;
        this.id_client = id_client;
        this.message = message;
    }
}
