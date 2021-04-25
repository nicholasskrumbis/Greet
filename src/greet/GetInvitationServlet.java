package greet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/GetInvitationServlet")
public class GetInvitationServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 11L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.containsKey("event_id") || !params.containsKey("email")) {
            result = "{\"error\": \"request didn't include event_id and email\"}";
        } else {
            int eventId = Integer.parseInt(params.get("event_id"));
            Event e = getEvent(eventId);
            User u = getPerson(eventId, params.get("email"));

            if (e == null || u == null)
                result = "{\"error\": \"invitation could not be found " +
                         "for this user for this event\"}";
            else {
                JsonObject obj = new JsonObject();
                Gson g = new Gson();
                obj.add("event", g.toJsonTree(e));
                obj.add("user", g.toJsonTree(u));
                result = obj.toString();
            }
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    private Event getEvent(int eventId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement("SELECT * FROM event WHERE id = ?");
            ps.setInt(1, eventId);
            rs = ps.executeQuery();

            // if not found, no such event
            if (!rs.next())
                return null;

            return new Event(
                    rs.getInt("id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getString("description"),
                    rs.getString("location")
            );
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

        return null;
    }

    private User getPerson(int eventId, String email) {
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

            // if not found, no invitation for this user
            if (!rs.next())
                return null;

            // id isn't needed
            return new User(-1,
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("email"));
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

        return null;
    }
}
