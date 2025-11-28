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

        public JDBCSignalManager(ConnectionManager conMan) {
            this.c = conMan.getConnection();
        }

        @Override
        public void addSignal(Signal signal, String fileName, Date date) {
            try {
                String sql = "INSERT INTO signal (patient_id, type, record_date, filename) VALUES (?, ?, ?, ?)";
                PreparedStatement p = c.prepareStatement(sql);

                p.setInt(1, signal.getClientId()); // patient_id
                p.setString(2, signal.getType().toString()); // EMG o ACC
                p.setDate(3, date);
                p.setString(4, fileName); // El nombre del archivo .txt

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
                    // Recuperamos el tipo (String -> Enum)
                    String typeStr = rs.getString("type");
                    TypeSignal type = TypeSignal.valueOf(typeStr);

                    int clientId = rs.getInt("patient_id");
                    long dateMillis = rs.getLong("record_date");
                    Date date = new Date(dateMillis);

                    // Creamos la señal (sin valores, solo metadatos)
                    Signal s = new Signal(type, clientId);
                    s.setRecordId(rs.getInt("id")); // ID de la BBDD
                    s.setRecordId(rs.getInt("id"));
                    s.setDate(date);

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
        String fileName = null;

        // 1. Obtener metadatos de la BBDD
        try {
            String sql = "SELECT * FROM signal WHERE id = ?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setInt(1, signalId);
            ResultSet rs = p.executeQuery();

            if (rs.next()) {
                String typeStr = rs.getString("type");
                TypeSignal type = TypeSignal.valueOf(typeStr);
                int clientId = rs.getInt("patient_id");
                long dateMillis = rs.getLong("record_date");
                Date date = new Date(dateMillis);
                fileName = rs.getString("filename");

                signal = new Signal();
                signal.setRecordId(signalId);
                signal.setType(type);
                signal.setClientId(clientId);
                signal.setDate(date);
                signal.setSignalFilename(fileName);
            }
            rs.close();
            p.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // 2. Leer los valores del archivo físico
        if (fileName != null) {
            File file = new File("ServerSignals/" + fileName); // Asegúrate de que la ruta coincide con donde guardas

            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isDataSection = false;

                    while ((line = br.readLine()) != null) {
                        if (line.contains("BEGIN DATA")) {
                            isDataSection = true;
                            continue;
                        }

                        if (isDataSection) {
                            try {
                                int val = Integer.parseInt(line.trim());
                                signal.addSample(val);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error reading signal file: " + fileName);
                    e.printStackTrace();
                }
            } else {
                System.out.println("File not found: " + fileName);
            }
        }

        return signal;
    }
}
