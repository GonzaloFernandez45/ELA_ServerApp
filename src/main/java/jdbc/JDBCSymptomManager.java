package jdbc;

import interfaces.SymptomManager;
import pojos.Symptom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCSymptomManager implements SymptomManager {
    private static Connection c;
    private ConnectionManager conMan;
    public JDBCSymptomManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }
    @Override
    public void addSymptom(Symptom s) {

    }

    @Override
    public List<Symptom> listSymptoms(){
        List<Symptom> symptoms = new ArrayList<>();
        try {
            String sql = "SELECT * FROM symptom";
            PreparedStatement pstmt = c.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Symptom symptom = new Symptom(rs.getInt("id"), rs.getString("description"));
                symptoms.add(symptom);
            }
            rs.close();
            pstmt.close();
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }

        return symptoms;

    }

    @Override
    public Symptom getSymptom(Symptom s) {
        return null;
    }

    @Override
    public Symptom getSymptomById(int symptomId) {
        Symptom symptom = null;
        try {
            String sql = "SELECT * FROM symptom WHERE id = ?";
            PreparedStatement pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, symptomId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                symptom = new Symptom(rs.getInt("id"), rs.getString("description"));
            }
        }catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return symptom;
    }


}
