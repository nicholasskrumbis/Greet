package greet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/UpdateGuestStatusServlet")
public class UpdateGuestStatusServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 6L;

    // important: only call this servlet if the user is coming
    // if they're not, don't call this. it's fine since the default
    // status is no anyway
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.keySet().containsAll(Arrays.asList(
                "event_id", "email", "fname", "lname"))) {
            result = "{\"error\": \"request didn't include " +
                     "event_id, email, fname, or lname\"}";
        } else if (!updateGuest(Integer.parseInt(params.get("event_id")),
                params.get("email"), params.get("fname"), params.get("lname"))) {
            result = "{\"error\": \"could not update guest status\"}";
        } else {
            result = "{}";
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns true if successful
    private boolean updateGuest(int eventId, String email,
                                String fname, String lname) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Util.getConnection();

            int qrId = createQR(eventId, email);

            ps = conn.prepareStatement(
                    "UPDATE attendance " +
                    "SET qr_id = ?, fname = ?, lname = ?, status = 1 " +
                    "WHERE event_id = ? AND email = ?");
            ps.setInt(1, qrId);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setInt(4, eventId);
            ps.setString(5, email);
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException |
                WriterException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private static int createQR(int eventId, String email)
            throws WriterException, IOException {
        // feel free to change this link, not sure what it should actually be
        String link = "http://localhost:8080/greet/checkin?eventId=%d&email=%s";
        String data = String.format(link, eventId, email);

        int qrId = (eventId + email).hashCode();

        BitMatrix matrix = new MultiFormatWriter().encode(data,
                BarcodeFormat.QR_CODE, Util.QR_SIZE, Util.QR_SIZE);

        MatrixToImageWriter.writeToPath(matrix, "png",
                new File("qrs/" + qrId + ".png").toPath());

        return qrId;
    }
}
