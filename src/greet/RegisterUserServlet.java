package greet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.*;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/RegisterUserServlet")
public class RegisterUserServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 2L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!(params.containsKey("email") && params.containsKey("fname") &&
            params.containsKey("lname") && params.containsKey("password"))) {
            result = "{\"error\": \"request didn't include " +
                     "email, fname, lname, or password\"}";
        } else {
            int id = registerUser(params.get("email"), params.get("fname"),
                    params.get("lname"), params.get("password"));

            if (id == -1) {
                result = "{\"error\": \"could not register\"}";
            } else {
                result = "{\"email\": \"" + params.get("email") + "\", " +
                         "\"UID\": " + id + "}";
            }
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns id if successful, else -1
    private int registerUser(String email, String fname,
                                 String lname, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement("SELECT * FROM user " +
                                       "WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();

            // if there is a result, email exists
            if (rs.next())
                return -1;

            ps.close();
            ps = conn.prepareStatement(
                    "INSERT INTO user (email, fname, lname, password) " +
                    "VALUES (?, ?, ?, ?)");
            ps.setString(1, email);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, password);
            ps.executeUpdate();

            ps.close();
            ps = conn.prepareStatement(
                    "SELECT * FROM user WHERE email = ? AND " +
                    "fname = ? AND lname = ? AND password = ?");
            ps.setString(1, email);
            ps.setString(2, fname);
            ps.setString(3, lname);
            ps.setString(4, password);
            rs.close();
            rs = ps.executeQuery();

            // if there is a result, return id
            if (rs.next())
                return rs.getInt("id");
            else
                return -1;
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

        return -1;
    }
}
