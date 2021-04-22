package greet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.*;
import java.util.stream.Collectors;

@WebServlet("/Login")
public class Login extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    private static class LoginResult {
        boolean successful;
        String message;

        public LoginResult(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());

        JsonObject form = JsonParser.parseString(s).getAsJsonObject();
        LoginResult result;

        try {
            switch (form.get("action").getAsString()) {
                case "login":
                    result = signupResult(form.get("email").getAsString(),
                            form.get("username").getAsString(),
                            form.get("password").getAsString());
                    break;
                case "signup":
                    result = loginResult(form.get("username").getAsString(),
                            form.get("password").getAsString());
                    break;
                default:
                    result = new LoginResult(false,
                            "Something went wrong; request didn't " +
                            "include \"action\" attribute!");
                    break;
            }
        } catch (NullPointerException e) {
            result = new LoginResult(false,
                    "Something went wrong; request didn't include " +
                    "\"username\" and \"password\" attributes!");
        }

        resp.setContentType("application/json");
        resp.getWriter().println(new Gson().toJson(result));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doPost(req, resp);
    }

    private LoginResult loginResult(String user, String pass) {
        String msg;
        boolean good = false;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/greet",
                    "root", "root");
            ps = conn.prepareStatement("SELECT password FROM user " +
                                       "WHERE username = ?");
            ps.setString(1, user);
            rs = ps.executeQuery();

            if (rs.next()) {
                if (rs.getString("password").equals(pass)) {
                    msg = "Username and password accepted.";
                    good = true;
                } else {
                    msg = "Incorrect password for user \"" + user + "\"";
                }
            } else {
                msg = "No account associated with username \"" + user + "\"";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            msg = "Something went wrong; the database could not be queried.";
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            msg = "Something went wrong; the JDBC driver could not be found.";
        } finally {
            try {
                if (conn != null) conn.close();
                if (ps != null) ps.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new LoginResult(good, msg);
    }

    private LoginResult signupResult(String email, String user, String pass) {
        String msg;
        boolean good = false;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/greet",
                    "root", "root");
            ps = conn.prepareStatement("INSERT INTO user " +
                                       "(email, username, password) VALUES (?, ?, ?)");
            ps.setString(1, email);
            ps.setString(2, user);
            ps.setString(3, pass);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                msg = "Something went wrong; the database could be updated " +
                      "but was not updated.";
            } else {
                msg = "Successfully registered!";
                good = true;
            }
        } catch (SQLIntegrityConstraintViolationException e) { // duplicate
            msg = "A user already exists with that email or username.";
        } catch (SQLException e) {
            e.printStackTrace();
            msg = "Something went wrong; the database could not be updated.";
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            msg = "Something went wrong; the JDBC driver could not be found.";
        } finally {
            try {
                if (conn != null) conn.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new LoginResult(good, msg);
    }

}
