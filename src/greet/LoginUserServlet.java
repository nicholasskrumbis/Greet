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

@WebServlet("/LoginUserServlet")
public class LoginUserServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 3L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(s);
        String result;

        if (!(params.containsKey("email") && params.containsKey("password"))) {
            result = "{\"error\": \"request didn't include email or password\"}";
        } else {
            User user = loginUser(params.get("email"), params.get("password"));

            if (user.getUID() == -1) {
                result = "{\"error\": \"could not register\"}";
            } else {
                result = "{\"email\": \"" + params.get("email") + "\", " +
                         "\"UID\": " + user.getUID() + ", " +
                		 "\"fname\": \"" + user.getFname() + "\", " +
                         "\"lname\": \"" + user.getLname() + "\"}";
            }
        }

        resp.setContentType("application/json");
        resp.getWriter().println(result);
    }

    // returns id if successful, else -1
    private User loginUser(String email, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = Util.getConnection();
            ps = conn.prepareStatement("SELECT * FROM user WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();

            if (rs.next() && rs.getString("password").equals(password))
                return new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"));
            else
                return new User();
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

        return new User();
    }
}

class User {
	private int UID;
	private String fname;
	private String lname;
	
	User() {
		UID = -1;
	}
	User(int id, String f, String l) {
		UID = id;
		fname = f;
		lname = l;
	}
	
	public int getUID() {
		return UID;
	}
	public String getFname() {
		return fname;
	}
	public String getLname() {
		return lname;
	}
}
