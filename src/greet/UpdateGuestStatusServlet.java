package greet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                params.get("email"), params.get("fname"), params.get("lname"))){
            result = "{\"error\": \"could not update guest status\"}";
        } else {
            result = "{}";
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns true if successful
    private boolean updateGuest(int eventId, String email, String fname, String lname) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement(
                    "UPDATE attendance SET fname = ?, lname = ?, status = 1 " +
                    "WHERE event_id = ? AND email = ?");
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setInt(3, eventId);
            ps.setString(4, email);
            int rowsAffected = ps.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException | ClassNotFoundException e) {
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
}
