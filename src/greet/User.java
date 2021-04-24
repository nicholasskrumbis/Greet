package greet;

public class User {
    int UID;
    String fname;
    String lname;
    String email;

    public User() {
        UID = -1;
    }

    public User(int id, String f, String l, String e) {
        UID = id;
        fname = f;
        lname = l;
        email = e;
    }
}
