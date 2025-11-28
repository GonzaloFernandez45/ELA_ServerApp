package pojos;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Signal {
    private TypeSignal type;         // ECG or EMG
    private int clientId;            // Patient
    private int recordId;            // Used only by the server
    private List<Integer> values;
    private Date date;               // Fecha de registro// Signal samples
    private String signalFilename;   // Nombre del archivo .txt (Ej: Signal_1_EMG_....txt)

    public Signal() {
        this.values = new ArrayList<>();
    }

    public Signal(TypeSignal type, int clientId) {
        this.type = type;
        this.clientId = clientId;
        this.values = new ArrayList<>();
    }

    // 3. Constructor completo (usado al recuperar de la Base de Datos)
    public Signal(int recordId, TypeSignal type, int clientId, Date date, String signalFilename, List<Integer> values) {
        this.recordId = recordId;
        this.type = type;
        this.clientId = clientId;
        this.date = date;
        this.signalFilename = signalFilename;
        this.values = values;
    }

    // 4. Constructor para recepción en red (usado en ReceiveDataViaNetwork)
    public Signal(List<Integer> values, String signalFilename, TypeSignal type) {
        this.values = values;
        this.signalFilename = signalFilename;
        this.type = type;
    }

    public TypeSignal getType() {
        return type;
    }

    public void setType(TypeSignal type) {
        this.type = type;
    }
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public void addSample(int sample) {
        values.add(sample);
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getSignalFilename() {
        return signalFilename;
    }

    public void setSignalFilename(String signalFilename) {
        this.signalFilename = signalFilename;
    }

    public byte[] toByteArray() {
        byte[] data = new byte[values.size() * 2];
        int pos = 0;

        for (int v : values) {
            short s = (short) v;
            data[pos++] = (byte) (s >> 8);   // high byte
            data[pos++] = (byte) s;          // low byte
        }
        return data;
    }

    @Override
    public String toString() {
        return "Signal{" + "type=" + type + ", clientId=" + clientId + ", date=" + date + ", file=" + signalFilename + '}';
    }
}
