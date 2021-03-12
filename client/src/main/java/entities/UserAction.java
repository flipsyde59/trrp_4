package entities;

public class UserAction {
    private String action;
    private String user_login;
    private String user_password;


    public UserAction(String action, String login, String password){
        this.action = action;
        this.user_login = login;
        this.user_password = password;
    }
}
