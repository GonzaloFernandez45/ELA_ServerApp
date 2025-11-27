package jdbc;

import interfaces.UserManager;
import pojos.Role;
import pojos.User;

import java.sql.*;

public class JDBCUserManager implements UserManager {
    private Connection c;
    private ConnectionManager conMan;

    public JDBCUserManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
   // public void addUser(User user) {
        /**try {
            String template = "INSERT INTO user (email, password, role, patient_id, doctor_id) VALUES (?, ?, ?, ?,?)";
            PreparedStatement pstmt;
            pstmt = c.prepareStatement(template);
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, new String(user.getPassword()));
            pstmt.setString(3, user.getRole().toString());
           if (user.getRole().getName().equals("PATIENT")) {
                pstmt.setInt(4, user.getPatient_id());
                pstmt.setNull(5, Types.INTEGER);

            }else{
                pstmt.setNull(4, Types.INTEGER);
                pstmt.setInt(5, user.getDoctor_id());

            }
            System.out.println("---- DEBUG addUser ----");
            System.out.println("email      = " + user.getEmail());
            System.out.println("role       = " + user.getRole());
            System.out.println("role name  = " + user.getRole().getName());
            System.out.println("patient_id = " + user.getPatient_id());
            System.out.println("doctor_id  = " + user.getDoctor_id());
            System.out.println("------------------------");
            pstmt.executeUpdate();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }**/

        public void addUser(User user) {
            try {
                String template = "INSERT INTO user (email, password, role, patient_id, doctor_id, admin_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = c.prepareStatement(template);

                String roleStr = user.getRole().toString();
                String roleName = user.getRole().getName();

                System.out.println("DEBUG ROLE addUser -> roleStr: " + roleStr + ", roleName: " + roleName);

                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, new String(user.getPassword()));
                pstmt.setString(3, roleStr); // o roleName, como quieras guardar

                // Normalizamos para comparar
                String roleNorm = roleName.toUpperCase();

                if (roleNorm.contains("PATIENT")) {   // <<< CAMBIO IMPORTANTE
                    System.out.println("Detected PATIENT role in addUser");
                    pstmt.setInt(4, user.getPatient_id());   // patient_id
                    pstmt.setNull(5, Types.INTEGER); // doctor_id NULL
                    pstmt.setNull(6,Types.INTEGER); // admin_id NULL
                } else if (roleNorm.contains("DOCTOR")) {
                    System.out.println("Detected DOCTOR role in addUser");
                    pstmt.setNull(4, Types.INTEGER);         // patient_id NULL
                    pstmt.setInt(5, user.getDoctor_id());
                    pstmt.setNull(6,Types.INTEGER); // admin_id NULL

                } else if (roleNorm.contains("ADMINISTRATOR")) {
                    System.out.println("Detected DOCTOR role in addUser");
                    pstmt.setNull(4, Types.INTEGER);         // patient_id NULL
                    pstmt.setNull(5, user.getDoctor_id());// doctor_id NULL
                    pstmt.setInt(6,user.getAdmin_id());
                } else {
                    System.out.println("Unknown role, setting both FKs to NULL");
                    pstmt.setNull(4, Types.INTEGER);
                    pstmt.setNull(5, Types.INTEGER);
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
