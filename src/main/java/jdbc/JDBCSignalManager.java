package jdbc;
import interfaces.SignalManager;
import pojos.Signal;
import pojos.TypeSignal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class JDBCSignalManager implements SignalManager {

    private Connection c;
    private ConnectionManager conMan;


    public JDBCSignalManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public void addSignal(Signal signal, String fileName, Date date) {
        try {
            // Guardamos los valores en la columna signal_values
            String sql = "INSERT INTO signal (patient_id, type, record_date, signal_values) VALUES (?, ?, ?, ?)";
            PreparedStatement p = c.prepareStatement(sql);

            p.setInt(1, signal.getClientId());
            p.setString(2, signal.getType().toString());
            p.setLong(3, date.getTime()); // Guardamos fecha como Long

            // CONVERSIÓN: Lista de Enteros -> String "123 456 789"
            String valuesString = signal.getValues().toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", ""); // Quitamos comas, dejamos espacios

            p.setString(4, valuesString);

            p.executeUpdate();
            p.close();
        } catch (SQLException e) {
            System.out.println("Error saving signal into database");
            e.printStackTrace();
        }
    }

    @Override
    public List<Signal> listSignalsByPatientId(int patientId) {
        List<Signal> signals = new ArrayList<>();
        try {
            String sql = "SELECT * FROM signal WHERE patient_id = ?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setInt(1, patientId);
            ResultSet rs = p.executeQuery();

            while (rs.next()) {
                String typeStr = rs.getString("type");
                TypeSignal type = TypeSignal.valueOf(typeStr);
                int clientId = rs.getInt("patient_id");

                // Recuperar fecha
                long dateMillis = rs.getLong("record_date");
                Date date = new Date(dateMillis);

                Signal s = new Signal(type, clientId);
                s.setRecordId(rs.getInt("id"));
                s.setDate(date);
                // No cargamos los valores aquí para no saturar la lista, solo metadatos

                signals.add(s);
            }
            rs.close();
            p.close();
        } catch (SQLException e) {
            System.out.println("Error getting signals");
            e.printStackTrace();
        }
        return signals;
    }

    @Override
    public Signal getSignalWithValues(int signalId) {
        Signal signal = null;
        try {
            String sql = "SELECT * FROM signal WHERE id = ?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setInt(1, signalId);
            ResultSet rs = p.executeQuery();

            if (rs.next()) {
                signal = new Signal();
                signal.setRecordId(signalId);
                signal.setType(TypeSignal.valueOf(rs.getString("type")));
                signal.setClientId(rs.getInt("patient_id"));
                signal.setDate(new Date(rs.getLong("record_date")));

                // CONVERSIÓN INVERSA: String "123 456" -> Lista de Enteros
                String valuesBlob = rs.getString("signal_values");

                if (valuesBlob != null && !valuesBlob.trim().isEmpty()) {
                    // Separamos por espacios (el delimitador por defecto del toString)
                    String[] tokens = valuesBlob.trim().split("\\s+");
                    for (String token : tokens) {
                        try {
                            signal.addSample(Integer.parseInt(token));
                        } catch (NumberFormatException e) {
                            // Ignoramos basura si la hubiera
                        }
                    }
                }
                // Ponemos un nombre ficticio para que el Doctor pueda generar el PNG
                signal.setSignalFilename("Signal_" + signal.getRecordId() + ".txt");
            }
            rs.close();
            p.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return signal;
    }
}

