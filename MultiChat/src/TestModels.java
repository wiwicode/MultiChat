import model.Message;
import model.User;

public class TestModels {

    public static void main(String[] args) {

        User user = new User("Manish", "abc123");

        System.out.println("Username: " + user.getUsername());
        System.out.println("Online: " + user.isOnline());

        Message message = new Message(
            "Manish",
            "Rahul",
            "Hey bro!"
        );

        System.out.println(message);
    }
}