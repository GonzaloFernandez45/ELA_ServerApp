package jdbc;
import interfaces.SignalManager;
import pojos.Signal;
import pojos.TypeSignal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

                    // Creamos la señal (sin valores, solo metadatos)
                    Signal s = new Signal(type, clientId);
                    s.setRecordId(rs.getInt("id")); // ID de la BBDD

                    // Opcional: Podrías guardar fecha y filename en el POJO Signal si añades esos atributos
                    // Por ahora solo devolvemos el objeto básico

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

}
