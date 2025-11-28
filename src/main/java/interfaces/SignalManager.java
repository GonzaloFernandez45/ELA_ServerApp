package interfaces;

import pojos.Signal;

import java.util.List;

public interface SignalManager {
        // Guardamos la señal y el nombre del archivo generado
        void addSignal(Signal signal, String fileName, java.sql.Date date);

        // Para que el doctor recupere la lista de señales de un paciente
        List<Signal> listSignalsByPatientId(int patientId);
        public Signal getSignalWithValues(int signalId);
}
