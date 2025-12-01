package interfaces;

import pojos.Signal;

import java.util.List;

/**
 * Manages storage and retrieval of physiological signals (ECG, EMG, etc.)
 * captured from patients. Implementations handle DB + file persistence.
 */
public interface SignalManager {
        /**
         * Stores a new signal record along with the file generated for that signal.
         *
         * @param signal   signal metadata (type, patient, etc.).
         * @param fileName name of the stored file.
         * @param date     capture date.
         */
        void addSignal(Signal signal, String fileName, java.sql.Date date);

        /**
         * Returns all signals belonging to a specific patient.
         *
         * @param patientId patient ID.
         * @return list of signals for that patient.
         */
        List<Signal> listSignalsByPatientId(int patientId);

        /**
         * Retrieves a signal and loads its values (typically from file or DB).
         *
         * @param signalId ID of the signal.
         * @return signal with its value data.
         */
        public Signal getSignalWithValues(int signalId);
}
