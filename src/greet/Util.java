package greet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
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

    public static int createQR(int eventId, String email)
            throws WriterException, IOException {
        // feel free to change this link, not sure what it should actually be
        String link = "http://localhost:8080/Greet/checkin.html?eventId=%d&email=%s";
        String data = String.format(link, eventId, email);

        int qrId = (eventId + email).hashCode();

        BitMatrix matrix = new MultiFormatWriter().encode(data,
                BarcodeFormat.QR_CODE, Util.QR_SIZE, Util.QR_SIZE);

        MatrixToImageWriter.writeToPath(matrix, "png",
                new File("qrs/" + qrId + ".png").toPath());

        return qrId;
    }
}
