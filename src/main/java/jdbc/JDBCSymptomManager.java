package jdbc;

import interfaces.SymptomManager;
import pojos.Symptom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class    JDBCSymptomManager implements SymptomManager {

    private static Connection c;
    private ConnectionManager conMan;
    public JDBCSymptomManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }
    public JDBCSymptomManager() {}// necesario para la clase server

    @Override
    public void addSymptom(Symptom s) {

    }

    @Override
    public List<Symptom> listSymptoms() {
        return List.of();
    }

    @Override
    public Symptom getSymptom(Symptom s) {
        return null;
    }

    @Override
    public Symptom getSymptomById(int symptomId) {
        Symptom symptom = null;
        String query = "SELECT * FROM symptoms WHERE id = ?";

        try (PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setInt(1, symptomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                symptom = new Symptom(rs.getInt("id"), rs.getString("name"));
            }
        }catch (SQLException e) {
            System.err.println("Error fetching symptom by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return symptom;
    }
}
