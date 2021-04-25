package greet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/GetQRServlet")
public class GetQRServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 10L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String s = req.getReader().lines().collect(Collectors.joining());
        Map<String, String> params = Util.queryToMap(java.net.URLDecoder.decode(s, StandardCharsets.UTF_8));
        
        resp.setContentType("image/png");
        OutputStream os = resp.getOutputStream();

        if (!params.keySet().containsAll(Arrays.asList("event_id", "email"))) {
            os.write("request didn't include event_id and email".getBytes());
            return;
        }

        InputStream is = getQR(Integer.parseInt(params.get("event_id")),
                params.get("email"));

        if (is == null) {
            os.write("could not find image".getBytes());
            return;
        }

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
    }

    private InputStream getQR(int eventId, String email) {
        int qrId = (eventId + email).hashCode();

        try {
            return new FileInputStream("qrs/" + qrId + ".png");
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
