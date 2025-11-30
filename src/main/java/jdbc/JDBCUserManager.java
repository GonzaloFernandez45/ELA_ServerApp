package jdbc;

import interfaces.UserManager;
import pojos.Role;
import pojos.User;

import java.sql.*;

import encryption.Encryption;

public class JDBCUserManager implements UserManager {
    private Connection c;
    private ConnectionManager conMan;

    public JDBCUserManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public void addUser(User user) {
        try {
            String template = "INSERT INTO user (email, passwordHash, passwordSalt, role, patient_id, doctor_id, admin_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = c.prepareStatement(template);

            String roleStr = user.getRole().toString();
            String roleName = user.getRole().getName();

            System.out.println("DEBUG ROLE addUser -> roleStr: " + roleStr + ", roleName: " + roleName);

            pstmt.setString(1, user.getEmail());


            byte[] rawPasswordBytes = user.getPassword(); // byte[]
            String plainPassword = new String(rawPasswordBytes);

            byte[] salt = Encryption.generateSalt();
            String saltStr = Encryption.saltToString(salt);
            String hash = Encryption.hashPassword(plainPassword, salt);

            pstmt.setString(2, hash );
            pstmt.setString(3, saltStr );

            pstmt.setString(4, roleStr); // o roleName, como quieras guardar

            // Normalizamos para comparar
            String roleNorm = roleName.toUpperCase();

            if (roleNorm.contains("PATIENT")) {   // <<< CAMBIO IMPORTANTE
                System.out.println("Detected PATIENT role in addUser");
                pstmt.setInt(5, user.getPatient_id());   // patient_id
                pstmt.setNull(6, Types.INTEGER); // doctor_id NULL
                pstmt.setNull(7,Types.INTEGER); // admin_id NULL
            } else if (roleNorm.contains("DOCTOR")) {
                System.out.println("Detected DOCTOR role in addUser");
                pstmt.setNull(5, Types.INTEGER);         // patient_id NULL
                pstmt.setInt(6, user.getDoctor_id());
                pstmt.setNull(7,Types.INTEGER); // admin_id NULL

            } else if (roleNorm.contains("ADMINISTRATOR")) {
                System.out.println("Detected DOCTOR role in addUser");
                pstmt.setNull(5, Types.INTEGER);         // patient_id NULL
                pstmt.setNull(6, user.getDoctor_id());// doctor_id NULL
                pstmt.setInt(7,user.getAdmin_id());
            } else {
                System.out.println("Unknown role, setting all FKs to NULL");
                pstmt.setNull(5, Types.INTEGER);
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }


    @Override
    public boolean checkPassword(String password, String email) {
        String sql = "SELECT id, role, passwordHash, passwordSalt FROM user WHERE email=?";
        PreparedStatement s = null;
        ResultSet rs = null;
        boolean isValid = false;


        try {
            s = conMan.getConnection().prepareStatement(sql);
            s.setString(1, email);

            rs = s.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("passwordHash");
                String storedSaltStr = rs.getString("passwordSalt");

                byte[] storedSalt = Encryption.stringToSalt(storedSaltStr);

                boolean valid = Encryption.validatePassword(password, storedHash, storedSalt);

                if (valid) {
                    isValid = true;
                }
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
