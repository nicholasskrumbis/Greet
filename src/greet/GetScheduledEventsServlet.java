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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/GetScheduledEventsServlet")
public class GetScheduledEventsServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 5L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.containsKey("UID")) {
            result = "{\"error\": \"request didn't include UID\"}";
        } else {
            result = getScheduledEvents(Integer.parseInt(params.get("UID")));
        }
        
        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }
    
    @SuppressWarnings("unused")
    private static class Event {
        int eventId;
        String date, time, description, location;

        public Event(int eventId, String date, String time,
                     String description, String location) {
            this.eventId = eventId;
            this.date = date;
            this.time = time;
            this.description = description;
            this.location = location;
        }
    }

    private String getScheduledEvents(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement("SELECT * FROM event WHERE host_id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();

            List<Event> events = new ArrayList<>();

            // if there is a result, email exists
            while (rs.next()) {
                Event e = new Event(
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("description"),
                        rs.getString("location")
                );

                events.add(e);
            }

            return new Gson().toJson(events);
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

        return "[]";
    }
}
