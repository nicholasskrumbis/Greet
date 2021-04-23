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

@WebServlet("/CreateEventServlet")
public class CreateEventServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 9L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.keySet().containsAll(Arrays.asList(
                "UID", "date", "time", "location", "description"))) {
            result = "{\"error\": \"request didn't include proper event information\"}";
        } else if (!createEvent(Integer.parseInt(params.get("UID")),
                params.get("date"), params.get("time"),
                params.get("location"), params.get("description"))){
            result = "{\"error\": \"could not create event\"}";
        } else {
            result = "{}";
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns true if successful
    private boolean createEvent(int hostId, String date, String time,
                               String location, String description) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO event " +
                    "(host_id, date, time, location, description) " +
                    "VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, hostId);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setString(4, location);
            ps.setString(5, description);
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
