package com.user;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Registration extends HttpServlet{
	private Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = getServletContext().getResourceAsStream("/WEB-INF/jdbc.properties")) {
            if (input == null) {
                throw new IOException("Unable to find jdbc.properties");
            }
            props.load(input);
        }
        return props;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("Name");
        String city = req.getParameter("City");
        String mobile = req.getParameter("Mobile");
        String dob = req.getParameter("Dob");

        String message = "";
        String status = "";

        // Check for empty fields
        if (name == null || name.isEmpty() || city == null || city.isEmpty() || 
            mobile == null || mobile.isEmpty() || dob == null || dob.isEmpty()) {
            message = "All fields are required.";
            status = "error";
        } else if (!mobile.matches("\\d+")) {
            message = "Mobile number must be numeric.";
            status = "error";
        } else {
            // Load database credentials from jdbc.properties
            Properties props = loadDatabaseProperties();
            String url = props.getProperty("jdbc.url");
            String dbUsername = props.getProperty("jdbc.username");
            String dbPassword = props.getProperty("jdbc.password");
            String sql = "INSERT INTO form_details (Name, City, Mobile, Dob) VALUES (?, ?, ?, ?)";

            // Initialize the database connection
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
                     PreparedStatement ps = connection.prepareStatement(sql)) {

                    // Set query parameters
                    ps.setString(1, name);
                    ps.setString(2, city);
                    ps.setString(3, mobile);
                    ps.setString(4, dob);

                    // Execute the update and check the result
                    int count = ps.executeUpdate();
                    if (count > 0) {
                        message = "Your response has been recorded.";
                        status = "success";
                    } else {
                        message = "No records inserted in the Database.";
                        status = "error";
                    }
                } catch (SQLException se) {
                    message = "Database error: " + se.getMessage();
                    status = "error";
                }
            } catch (ClassNotFoundException e) {
                message = "Database driver not found.";
                status = "error";
            }
        }

        // Prepare the HTML response
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Registration Response</title>");
        out.println("<link rel='stylesheet' href='Index.css'>"); // Ensure this path is correct
        out.println("</head>");
        out.println("<body>");
        out.println("<main>");
        out.println("<h1>Registration Response</h1>");
        out.println("<div class='message " + status + "'>" + message + "</div>");
        out.println("<a href='Register.html' class='button-link'>Return to Registration Form</a>");
        out.println("</main>");
        out.println("</body>");
        out.println("</html>");
    }
}
