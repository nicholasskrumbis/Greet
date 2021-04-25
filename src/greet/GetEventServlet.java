package greet;

import com.google.gson.Gson;

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

@WebServlet("/GetEventServlet")
public class GetEventServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 11L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.containsKey("event_id")) {
            result = "{\"error\": \"request didn't include event_id\"}";
        } else {
            Event e = getEvent(Integer.parseInt(params.get("event_id")));

            if (e == null)
                result = "{\"error\": \"invitation could not be found " +
                         "for this user for this event\"}";
            else
                result = new Gson().toJson(e);
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
}
