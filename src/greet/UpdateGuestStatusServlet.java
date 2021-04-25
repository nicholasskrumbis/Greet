package greet;

import com.google.zxing.WriterException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        ResultSet rs = null;

        try {
            conn = Util.getConnection();

            ps = conn.prepareStatement(
                    "SELECT * FROM attendance WHERE event_id = ? AND email = ?");
            ps.setInt(1, eventId);
            ps.setString(2, email);
            rs = ps.executeQuery();

            if (rs.next()) {
                rs.getString("fname");
                if (!rs.wasNull()) // if already filled in, user already accepted
                    return false;
            } else // user isn't invited
                return false;

            // create qr code image in a different thread
            Thread t = new Thread(() -> {
                try {
                    Util.createQR(eventId, email);
                } catch (WriterException | IOException e) {
                    e.printStackTrace();
                }
            });
            t.start();

            ps.close();
            ps = conn.prepareStatement(
                    "UPDATE attendance " +
                    "SET qr_id = ?, fname = ?, lname = ?, status = 1 " +
                    "WHERE event_id = ? AND email = ?");
            ps.setInt(1, Util.qrID(eventId, email));
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setInt(4, eventId);
            ps.setString(5, email);
            int rowsAffected = ps.executeUpdate();

            // wait for qr to finish being made
            while (t.isAlive())
                Thread.yield();

            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                if (ps != null) ps.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
