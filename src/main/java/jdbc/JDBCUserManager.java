package jdbc;

import interfaces.UserManager;
import pojos.Role;
import pojos.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUserManager implements UserManager {
    private static Connection c;
    private ConnectionManager conMan;

    public JDBCUserManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public void addUser(User user) {
        try {
            String template = "INSERT INTO user (email, password, role, patient_id) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, new String(user.getPassword()));
            pstmt.setString(3, user.getRole().toString());
            pstmt.setInt(4, user.getPatient_id());
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkPassword(String password, String email) {
        String sql = "SELECT id, role FROM User WHERE email=? AND password=?";
        PreparedStatement s = null;
        ResultSet rs = null;
        boolean isValid = false;


        try {
            s = conMan.getConnection().prepareStatement(sql);
            s.setString(1, email);
            s.setString(2, password);

            rs = s.executeQuery();

            if (rs.next()) {
                isValid = true;
            }

        } catch (SQLException e) {
            System.out.println("Error during login process.");
            return isValid;
        } finally {
            try {
                if (rs != null) rs.close();
                if (s != null) s.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isValid;
    }

}
