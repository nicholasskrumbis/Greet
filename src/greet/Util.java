package greet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public static final String DB_URL = "jdbc:mysql://localhost:3306/greet";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "root";

    public static final int QR_SIZE = 128;

    public static Connection getConnection()
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static Map<String, String> queryToMap(String query) {
        String[] pairs = query.split("&");
        Map<String, String> map = new HashMap<>();

        for (String pair : pairs) {
            int i = pair.indexOf('=');
            assert i != -1 : "malformed query string";
            map.put(pair.substring(0, i), pair.substring(i + 1));
        }

        return map;
    }

}
