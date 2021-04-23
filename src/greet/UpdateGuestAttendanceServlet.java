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

@WebServlet("/UpdateGuestAttendanceServlet")
public class UpdateGuestAttendanceServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 7L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.keySet().containsAll(Arrays.asList("event_id", "email"))) {
            result = "{\"error\": \"request didn't include event_id and email\"}";
        } else if (!updateGuest(Integer.parseInt(params.get("event_id")),
                params.get("email"))){
            result = "{\"error\": \"could not update guest attendance\"}";
        } else {
            result = "{}";
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns true if successful
    private boolean updateGuest(int eventId, String email) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement("UPDATE attendance SET attendance = 1 " +
                                       "WHERE event_id = ? AND email = ?");
            ps.setInt(1, eventId);
            ps.setString(2, email);
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
