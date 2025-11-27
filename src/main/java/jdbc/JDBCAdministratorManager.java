package jdbc;

import interfaces.AdministratorManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import pojos.Administrator;

public class JDBCAdministratorManager implements AdministratorManager {
    private Connection c;
    private ConnectionManager conMan;
    public JDBCAdministratorManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();

    }

    @Override
    public void insertAdministrator(Administrator administrator) {
        try {
            String sql = "INSERT INTO administrator (email, dni) VALUES (?,?)";
            PreparedStatement prep = c.prepareStatement(sql);
            prep.setString(1, administrator.getEmail());
            prep.setString(2, administrator.getDni());

            prep.executeUpdate();
            prep.close();
        } catch (SQLException e) {
            System.out.println("Error inserting administrator.");
            e.printStackTrace();
        }
    }

    @Override
    public Administrator getAdministratorByEmail(String email) {
        Administrator administrator = null;
        try {
            String sql = "SELECT * FROM Administrator WHERE email = ?";
            PreparedStatement prep = c.prepareStatement(sql);
            prep.setString(1, email);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                administrator = new Administrator(rs.getInt("id"));
                administrator.setEmail(rs.getString("email"));
                administrator.setDni(rs.getString("dni"));
            }
            rs.close();
            prep.close();
        } catch (SQLException e) {
            System.out.println("Error getting administrator by email.");
            e.printStackTrace();
        }
        return administrator;
    }

    public Administrator getAdministratorById(int id) {
        Administrator administrator = null;
        try {
            String sql = "SELECT * FROM Administrator WHERE id = ?";
            PreparedStatement prep = c.prepareStatement(sql);
            prep.setInt(1, id);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                administrator = new Administrator(rs.getInt("id"));
                administrator.setEmail(rs.getString("email"));
                administrator.setDni(rs.getString("dni"));
            }
            rs.close();
            prep.close();
        } catch (SQLException e) {
            System.out.println("Error getting administrator by email.");
            e.printStackTrace();
        }
        return administrator;
    }

}
