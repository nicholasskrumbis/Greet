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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/GetEventAttendanceServlet")
public class GetEventAttendanceServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 13L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!params.containsKey("event_id")) {
            result = "{\"error\": \"request didn't include event_id\"}";
        } else {
            result = getAttendance(Integer.parseInt(params.get("event_id")));
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    private String getAttendance(int eventId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement(
                    "SELECT * FROM attendance WHERE event_id = ?");
            ps.setInt(1, eventId);
            rs = ps.executeQuery();

            List<JsonObject> guests = new ArrayList<>();

            while (rs.next()) {
                JsonObject guest = new JsonObject();

                for (String prop : new String[]
                        {"fname", "lname", "email", "status", "attendance"}) {
                    guest.addProperty(prop, rs.getString(prop));

                    if (rs.wasNull())
                        guest.remove(prop);
                }

                guests.add(guest);
            }

            return new Gson().toJson(guests);
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
